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


package com.jcwhatever.bukkit.pvs.arenas;

import com.jcwhatever.bukkit.generic.events.manager.GenericsEventHandler;
import com.jcwhatever.bukkit.generic.events.manager.GenericsEventPriority;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.options.JoinRejectReason;
import com.jcwhatever.bukkit.pvs.api.events.ArenaDisabledEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerPreJoinEvent;

import org.bukkit.plugin.Plugin;

@ArenaTypeInfo(
        typeName="arena",
        description="A basic arena.")
public class PVArena extends AbstractArena {

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
    @GenericsEventHandler(priority = GenericsEventPriority.FIRST)
    private void onPlayerPreJoin(PlayerPreJoinEvent event) {

        // check player permission
        if (!event.getPlayer().getHandle().hasPermission(getPermission().getName())) {
            event.rejectJoin(JoinRejectReason.NO_PERMISSION, Lang.get(_JOIN_NO_PERMISSION, getName()));
        }

        // Make sure the player is not already in an arena
        Arena currentArena = event.getPlayer().getArena();
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
        if (getGameManager().isRunning()) {
            event.rejectJoin(JoinRejectReason.ARENA_RUNNING, Lang.get(_ARENA_RUNNING, getName()));
        }
    }

    /*
     * Handle arena disabled
     */
    @GenericsEventHandler
    private void onArenaDisabled(@SuppressWarnings("unused") ArenaDisabledEvent event) {
        getGameManager().end();
    }
}
