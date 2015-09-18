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

package com.jcwhatever.pvs.scripting;

import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.event.EventSubscriberPriority;
import com.jcwhatever.nucleus.utils.observer.event.IEventSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.IScriptEventSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.ScriptEventSubscriber;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Script API to attach events directly to an arena.
 */
public class ArenaEventsApiObject implements IDisposable {

    private final Deque<IEventSubscriber> _subscribers = new ArrayDeque<>(25);
    private final IArena _arena;

    private boolean _isDisposed;

    ArenaEventsApiObject(IArena arena) {
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
     * Register an {@link IScriptEventSubscriber} event handler with the arena.
     *
     * @param eventName  The event type.
     * @param priority   The event priority. "invokeForCancelled" can be specified.
     *                   i.e. "NORMAL:invokeForCancelled". By default, handlers are not
     *                   invoked if the event is cancelled by another handler.
     * @param handler    The event handler.
     */
    public void on(String eventName, String priority, final IScriptEventSubscriber handler) {
        PreCon.notNullOrEmpty(eventName);
        PreCon.notNullOrEmpty(priority);
        PreCon.notNull(handler);

        String[] priorityComp = TextUtils.PATTERN_COLON.split(priority);
        boolean ignoreCancelled = true;

        if (priorityComp.length == 2) {
            if (priorityComp[1].equalsIgnoreCase("invokeForCancelled")) {
                ignoreCancelled = false;
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
        subscriber.setInvokedForCancelled(!ignoreCancelled);

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
