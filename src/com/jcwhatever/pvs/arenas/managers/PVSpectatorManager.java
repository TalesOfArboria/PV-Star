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


package com.jcwhatever.pvs.arenas.managers;

import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.managers.ISpectatorManager;
import com.jcwhatever.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.pvs.api.arena.settings.ISpectatorSettings;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.arenas.settings.PVSpectatorSettings;

import org.bukkit.Location;

import javax.annotation.Nullable;

/**
 * Spectator manager implementation
 */
public class PVSpectatorManager extends AbstractPlayerManager implements ISpectatorManager {

    private final ISpectatorSettings _settings;

    /*
     * Constructor.
     */
    public PVSpectatorManager(IArena arena) {
        super(arena);

        _settings = new PVSpectatorSettings(arena);
    }

    @Override
    public ISpectatorSettings getSettings() {
        return _settings;
    }

    @Nullable
    @Override
    protected Location onRespawnPlayer(IArenaPlayer player) {
        return getSpawnLocation(player);
    }

    @Override
    protected Location onAddPlayer(IArenaPlayer player, AddPlayerReason reason) {
        return getSpawnLocation(player);
    }

    @Override
    protected void onPreRemovePlayer(IArenaPlayer player, RemovePlayerReason reason) {
        // do nothing
    }

    @Override
    protected Location onRemovePlayer(IArenaPlayer player, RemovePlayerReason reason) {
        return getArena().getSettings().getRemoveLocation();
    }

    @Nullable
    private Location getSpawnLocation(IArenaPlayer player) {
        Spawnpoint spawnpoint = getArena().getSpawnManager().getRandomSpectatorSpawn(player.getTeam());
        if (spawnpoint == null) {
            Msg.warning("Failed to find a spectator spawn for a player in arena '{0}'.", getArena().getName());
            return null;
        }

        return spawnpoint;
    }
}
