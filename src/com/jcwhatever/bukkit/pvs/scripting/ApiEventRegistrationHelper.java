/* This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
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

import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.events.AbstractArenaEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaCountdownPreStartEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaCountdownStartedEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaDisabledEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaEnabledEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaEndedEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaPreEnableEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaPreStartEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaStartedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.ArenaBlockDamagePreventEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerAddedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerArenaDeathEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerArenaKillEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerArenaRespawnEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerBlockInteractEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerDamagedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerJoinQueryEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerLoseEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerPreAddEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerPreRemoveEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerReadyEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerRemovedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerTeamChangedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerTeamPreChangeEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerWinEvent;
import com.jcwhatever.bukkit.pvs.api.events.region.ArenaRegionPreRestoreEvent;
import com.jcwhatever.bukkit.pvs.api.events.region.ArenaRegionPreSaveEvent;
import com.jcwhatever.bukkit.pvs.api.events.region.ArenaRegionRestoredEvent;
import com.jcwhatever.bukkit.pvs.api.events.region.ArenaRegionSavedEvent;
import com.jcwhatever.bukkit.pvs.api.events.region.PlayerEnterArenaRegionEvent;
import com.jcwhatever.bukkit.pvs.api.events.region.PlayerLeaveArenaRegionEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.AddSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.ClearReservedSpawnsEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.RemoveSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.ReserveSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.UnreserveSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.events.team.TeamLoseEvent;
import com.jcwhatever.bukkit.pvs.api.events.team.TeamWinEvent;

/**
 * Registers PV-Star API events into the scripting manager
 * so they can be used in scripts.
 */
public class ApiEventRegistrationHelper {

    private final PVScriptManager _manager;

    public ApiEventRegistrationHelper(PVScriptManager manager) {
        PreCon.notNull(manager);

        _manager = manager;
    }

    public void register() {
        registerPlayerEvents();
        registerRegionEvents();
        registerSpawnEvents();
        registerTeamEvents();
        registerArenaEvents();
    }

    // player events
    private void registerPlayerEvents() {

        register(ArenaBlockDamagePreventEvent.class);
        register(PlayerAddedEvent.class);
        register(PlayerArenaDeathEvent.class);
        register(PlayerArenaKillEvent.class);
        register(PlayerArenaRespawnEvent.class);
        register(PlayerBlockInteractEvent.class);
        register(PlayerDamagedEvent.class);
        register(PlayerJoinQueryEvent.class);
        register(PlayerLoseEvent.class);
        register(PlayerPreAddEvent.class);
        register(PlayerPreRemoveEvent.class);
        register(PlayerReadyEvent.class);
        register(PlayerRemovedEvent.class);
        register(PlayerTeamChangedEvent.class);
        register(PlayerTeamPreChangeEvent.class);
        register(PlayerWinEvent.class);
    }

    private void registerRegionEvents() {

        register(ArenaRegionPreRestoreEvent.class);
        register(ArenaRegionPreSaveEvent.class);
        register(ArenaRegionRestoredEvent.class);
        register(ArenaRegionSavedEvent.class);
        register(PlayerEnterArenaRegionEvent.class);
        register(PlayerLeaveArenaRegionEvent.class);
    }

    private void registerSpawnEvents() {

        register(AddSpawnEvent.class);
        register(ClearReservedSpawnsEvent.class);
        register(RemoveSpawnEvent.class);
        register(ReserveSpawnEvent.class);
        register(UnreserveSpawnEvent.class);
    }

    private void registerTeamEvents() {

        register(TeamLoseEvent.class);
        register(TeamWinEvent.class);
    }

    private void registerArenaEvents() {

        register(ArenaCountdownPreStartEvent.class);
        register(ArenaCountdownStartedEvent.class);
        register(ArenaDisabledEvent.class);
        register(ArenaEnabledEvent.class);
        register(ArenaEndedEvent.class);
        register(ArenaPreEnableEvent.class);
        register(ArenaPreStartEvent.class);
        register(ArenaStartedEvent.class);
    }

    private void register(Class<? extends AbstractArenaEvent> event) {

        _manager.registerEventType(event);
    }
}
