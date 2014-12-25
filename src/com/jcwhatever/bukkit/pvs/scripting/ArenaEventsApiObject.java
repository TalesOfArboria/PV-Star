/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.bukkit.pvs.scripting;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.jcwhatever.generic.events.manager.GenericsEventPriority;
import com.jcwhatever.generic.events.manager.IEventHandler;
import com.jcwhatever.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.generic.utils.PreCon;
import com.jcwhatever.generic.utils.text.TextUtils;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;

import java.util.Collection;
import java.util.Set;

/*
 * 
 */
public class ArenaEventsApiObject implements IScriptApiObject {

    private final Multimap<Class<?>, EventWrapper> _registeredHandlers =
            MultimapBuilder.hashKeys(30).hashSetValues(5).build();

    private final Arena _arena;
    private boolean _isDisposed;

    ArenaEventsApiObject(Arena arena) {
        _arena = arena;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    /**
     * Reset api and release resources.
     */
    @Override
    public void dispose() {

        Set<Class<?>> events = _registeredHandlers.keySet();

        for (Class<?> event : events) {

            Collection<EventWrapper> handlers = _registeredHandlers.get(event);
            if (handlers == null)
                continue;

            for (EventWrapper handler : handlers) {
                //noinspection unchecked
                handler.getArena().getEventManager().unregister(event, handler);
            }
        }

        _registeredHandlers.clear();
        _isDisposed = true;
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

        EventWrapper eventHandler = new EventWrapper(_arena, handler) {
            @Override
            public void handle(Object event) {
                handler.call(event);
            }
        };

        Class<?> eventClass;

        try {
            eventClass = Class.forName(eventName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        //noinspection unchecked
        _arena.getEventManager().register(PVStarAPI.getPlugin(), eventClass, eventPriority, ignoreCancelled, eventHandler);

        _registeredHandlers.put(eventClass, eventHandler);
    }

    public static interface ArenaEventHandler {

        public abstract void call(Object event);
    }

    private static class EventWrapper implements IEventHandler {

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
        public void handle(Object event) {
            _handler.call(event);
        }
    }
}
