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

import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.nucleus.scripting.api.IScriptApiObject;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.event.EventSubscriberPriority;
import com.jcwhatever.nucleus.utils.observer.event.IEventSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.IScriptEventSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.ScriptEventSubscriber;
import com.jcwhatever.nucleus.utils.text.TextUtils;

import java.util.LinkedList;

/**
 * Script API to attach events directly to an arena.
 */
public class ArenaEventsApiObject implements IScriptApiObject {

    private final LinkedList<IEventSubscriber> _subscribers = new LinkedList<>();

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

        while (!_subscribers.isEmpty()) {
            _subscribers.remove().dispose();
        }

        _isDisposed = true;
    }

    /**
     * Register an {@code AbstractArenaEvent} event handler with the arena.
     *
     * @param eventName  The event type.
     * @param priority   The event priority.
     * @param handler    The event handler.
     */
    public void on(String eventName, String priority, final IScriptEventSubscriber handler) {
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

        EventSubscriberPriority eventPriority = EventSubscriberPriority.NORMAL;

        try {
            eventPriority = EventSubscriberPriority.valueOf(priority.toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
        }

        @SuppressWarnings("unchecked")
        ScriptEventSubscriber subscriber = new ScriptEventSubscriber(handler);
        subscriber.setPriority(eventPriority);
        subscriber.setCancelIgnored(ignoreCancelled);

        Class<?> eventClass;

        try {
            eventClass = Class.forName(eventName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        //noinspection unchecked
        _arena.getEventManager().register(PVStarAPI.getPlugin(), eventClass, subscriber);

        _subscribers.add(subscriber);
    }
}
