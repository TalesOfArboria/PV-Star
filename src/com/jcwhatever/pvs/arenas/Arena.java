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


package com.jcwhatever.pvs.arenas;

import com.jcwhatever.nucleus.events.manager.EventMethod;
import com.jcwhatever.nucleus.utils.observer.event.EventSubscriberPriority;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.options.JoinRejectReason;
import com.jcwhatever.pvs.api.events.ArenaDisabledEvent;
import com.jcwhatever.pvs.api.events.players.PlayerPreJoinArenaEvent;

import org.bukkit.plugin.Plugin;

@ArenaTypeInfo(
        typeName="arena",
        description="A basic arena.")
public class Arena extends AbstractArena {

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    protected boolean onCanJoin() {
        return true;
    }

    /*
     *  Handle player join event
     */
    @EventMethod(priority = EventSubscriberPriority.FIRST)
    private void onPlayerPreJoin(PlayerPreJoinArenaEvent event) {

        // check player permission
        if (!event.getPlayer().getPlayer().hasPermission(getPermission().getName())) {
            event.rejectJoin(JoinRejectReason.NO_PERMISSION, Lang.get(_JOIN_NO_PERMISSION, getName()));
        }

        // Make sure the player is not already in an arena
        IArena currentArena = event.getPlayer().getArena();
        if (currentArena != null) {
            event.rejectJoin(JoinRejectReason.IN_OTHER_ARENA, Lang.get(_JOIN_LEAVE_CURRENT_FIRST, getName()));
        }

        // make sure arena is enabled
        if (!getSettings().isEnabled()) {
            event.rejectJoin(JoinRejectReason.ARENA_DISABLED, Lang.get(_ARENA_DISABLED, getName()));
        }

        // make sure arena isn't busy
        if (isBusy()) {
            event.rejectJoin(JoinRejectReason.ARENA_BUSY, Lang.get(_ARENA_BUSY, getName()));
        }

        // make sure there are enough join slots available
        if (getAvailableSlots() <= 0) {
            event.rejectJoin(JoinRejectReason.ARENA_BUSY, Lang.get(_JOIN_LIMIT_REACHED, getName()));
        }

        // make sure game isn't already running, placed here
        // so the functionality can be changed/replaced/removed.
        if (getGame().isRunning()) {
            event.rejectJoin(JoinRejectReason.ARENA_RUNNING, Lang.get(_ARENA_RUNNING, getName()));
        }
    }

    /*
     * Handle arena disabled
     */
    @EventMethod
    private void onArenaDisabled(@SuppressWarnings("unused") ArenaDisabledEvent event) {
        getGame().end();
    }
}
