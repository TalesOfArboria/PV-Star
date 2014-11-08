/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.jcwhatever.bukkit.pvs.scripting.repo;

import com.jcwhatever.bukkit.generic.collections.MultiValueMap;
import com.jcwhatever.bukkit.generic.events.EventHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventPriority;
import com.jcwhatever.bukkit.generic.scripting.IEvaluatedScript;
import com.jcwhatever.bukkit.generic.scripting.IScriptApiInfo;
import com.jcwhatever.bukkit.generic.scripting.api.GenericsScriptApi;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.options.NameMatchMode;
import com.jcwhatever.bukkit.pvs.api.events.AbstractArenaEvent;

import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;

@IScriptApiInfo(
        variableName = "pvEvents",
        description = "register PV-Star events."
)
public class PVEventsRepoApi extends GenericsScriptApi {

    private static ApiObject _api;

    /**
     * Constructor.
     *
     * @param plugin The owning plugin
     */
    public PVEventsRepoApi(Plugin plugin) {
        super(plugin);

        _api = new ApiObject();
    }

    @Override
    public IScriptApiObject getApiObject(IEvaluatedScript script) {
        return _api;
    }

    @Override
    public void reset() {
        _api.reset();
    }

    public static class ApiObject implements IScriptApiObject {

        private final MultiValueMap<Class<?>, EventWrapper> _registeredHandlers =
                new MultiValueMap<>(30);


        ApiObject() {}

        /**
         * Reset api and release resources.
         */
        @Override
        public void reset() {

            Set<Class<?>> events = _registeredHandlers.keySet();

            for (Class<?> event : events) {

                List<EventWrapper> handlers = _registeredHandlers.getValues(event);
                if (handlers == null)
                    continue;

                for (EventWrapper handler : handlers) {
                   handler.getArena().getEventManager().unregister(event, handler);
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
        public void on(String arenaName, String eventName, String priority, ArenaEventHandler handler) {
            PreCon.notNullOrEmpty(arenaName);
            PreCon.notNullOrEmpty(eventName);
            PreCon.notNullOrEmpty(priority);
            PreCon.notNull(handler);

            GenericsEventPriority eventPriority = GenericsEventPriority.NORMAL;

            try {
                eventPriority = GenericsEventPriority.valueOf(priority.toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Arena> arenas = PVStarAPI.getArenaManager().getArena(arenaName, NameMatchMode.CASE_INSENSITIVE);
            PreCon.isValid(arenas.size() == 1);

            EventWrapper eventHandler = new EventWrapper(arenas.get(0), handler);

            Class<? extends AbstractArenaEvent> eventClass =
                    PVStarAPI.getScriptManager().getEventType(eventName.toLowerCase());

            PreCon.notNull(eventClass);

            arenas.get(0).getEventManager().register(eventClass, eventPriority, eventHandler);

            _registeredHandlers.put(eventClass, eventHandler);
        }
    }

    public static interface ArenaEventHandler {

        public abstract void onCall(Object event);
    }

    private static class EventWrapper implements EventHandler {

        private final Arena _arena;
        private final ArenaEventHandler _handler;

        EventWrapper(Arena arena, ArenaEventHandler handler) {
            _arena = arena;
            _handler = handler;
        }

        public Arena getArena() {
            return _arena;
        }

        @Override
        public void call(Object event) {
            _handler.onCall(event);
        }
    }
}
