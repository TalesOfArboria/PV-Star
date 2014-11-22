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


package com.jcwhatever.bukkit.pvs.scripting.api;

import com.jcwhatever.bukkit.generic.collections.MultiValueMap;
import com.jcwhatever.bukkit.generic.events.IEventHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventPriority;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptApi;

import java.util.List;
import java.util.Set;

/**
 * Provide scripts with ability to register and unregister PV-Star
 * events with the owning arena.
 */
public class EventsApi extends ScriptApi {

    @Override
    public String getVariableName() {
        return "arenaEvents";
    }

    @Override
    protected IScriptApiObject onCreateApiObject(Arena arena, EvaluatedScript script) {
        PreCon.notNull(arena);

        return new ApiObject(arena);
    }

    public static class ApiObject implements IScriptApiObject {

        private final Arena _arena;
        private final MultiValueMap<Class<?>, IEventHandler> _registeredHandlers = new MultiValueMap<>(30);

        ApiObject(Arena arena) {
            _arena = arena;
        }

        /**
         * Reset api and release resources.
         */
        @Override
        public void reset() {

            Set<Class<?>> events = _registeredHandlers.keySet();

            for (Class<?> event : events) {

                List<IEventHandler> handlers = _registeredHandlers.getValues(event);
                if (handlers == null)
                    continue;

                for (IEventHandler handler : handlers) {
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

            String[] priorityComp = TextUtils.PATTERN_COLON.split(priority);
            boolean ignoreCancelled = false;

            if (priorityComp.length == 2) {
                if (priorityComp[1].equalsIgnoreCase("ignoreCancelled")) {
                    ignoreCancelled = true;
                    priority = priorityComp[0];
                }
            }

            GenericsEventPriority eventPriority = GenericsEventPriority.NORMAL;

            try {
                eventPriority = GenericsEventPriority.valueOf(priority.toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
            }

            IEventHandler eventHandler = new IEventHandler() {
                @Override
                public void call(Object event) {
                    handler.onCall(event);
                }
            };

            Class<?> eventClass;

            try {
                eventClass = Class.forName(eventName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }

            _arena.getEventManager().register(eventClass, eventPriority, ignoreCancelled, eventHandler);

            _registeredHandlers.put(eventClass, eventHandler);
        }
    }

    public static interface ArenaEventHandler {

        public abstract void onCall(Object event);
    }
}
