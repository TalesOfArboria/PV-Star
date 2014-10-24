package com.jcwhatever.bukkit.pvs.scripting.api;

import com.jcwhatever.bukkit.generic.collections.MultiValueMap;
import com.jcwhatever.bukkit.generic.events.AbstractGenericsEvent;
import com.jcwhatever.bukkit.generic.events.EventHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventPriority;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.events.AbstractArenaEvent;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provide scripts with ability to register and unregister PV-Star
 * events with the owning arena.
 */
public class EventsApi extends ScriptApi {

    private Map<String, Class<? extends AbstractArenaEvent>> _registeredEvents = new HashMap<>(250);

    @Override
    public String getVariableName() {
        return "events";
    }

    public void registerEventType(String context, Class<? extends AbstractArenaEvent> eventClass) {
        PreCon.notNull(context);
        PreCon.notNull(eventClass);

        _registeredEvents.put(context.toLowerCase() + eventClass.getSimpleName().toLowerCase(), eventClass);
    }

    @Override
    protected IScriptApiObject onCreateApiObject(Arena arena, EvaluatedScript script) {
        PreCon.notNull(arena);

        return new ApiObject(arena);
    }

    public class ApiObject implements IScriptApiObject {

        private final Arena _arena;
        private final MultiValueMap<Class<? extends AbstractGenericsEvent>, EventHandler> _registeredHandlers = new MultiValueMap<>(30);

        ApiObject(Arena arena) {
            _arena = arena;
        }

        /**
         * Reset api and release resources.
         */
        @Override
        public void reset() {

            Set<Class<? extends AbstractGenericsEvent>> events = _registeredHandlers.keySet();

            for (Class<? extends AbstractGenericsEvent> event : events) {

                List<EventHandler> handlers = _registeredHandlers.getValues(event);
                if (handlers == null)
                    continue;

                for (EventHandler handler : handlers) {
                    _arena.getEventManager().unregister(event, handler);
                }
            }

            _registeredHandlers.clear();
        }

        /**
         * Register an {@code AbstractArenaEvent} event handler with the arena.
         *
         * @param eventName  The event type.
         * @param priority   The event priority.
         * @param handler    The event handler.
         */
        public void on(String eventName, String priority, final ArenaEventHandler handler) {
            PreCon.notNullOrEmpty(eventName);
            PreCon.notNullOrEmpty(priority);
            PreCon.notNull(handler);

            GenericsEventPriority eventPriority = GenericsEventPriority.NORMAL;

            try {
                eventPriority = GenericsEventPriority.valueOf(priority.toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
            }

            EventHandler eventHandler = new EventHandler() {
                @Override
                public void call(AbstractGenericsEvent event) {
                    handler.onCall(event);
                }
            };

            Class<? extends AbstractArenaEvent> eventClass = _registeredEvents.get(eventName.toLowerCase());
            PreCon.notNull(eventClass);

            _arena.getEventManager().register(eventClass, eventPriority, eventHandler);

            _registeredHandlers.put(eventClass, eventHandler);
        }
    }

    public static interface ArenaEventHandler {

        public abstract void onCall(Object event);
    }
}
