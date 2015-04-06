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

import com.jcwhatever.nucleus.events.manager.EventMethod;
import com.jcwhatever.nucleus.events.manager.IEventListener;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.collections.IArenaPlayerCollection;
import com.jcwhatever.pvs.api.arena.managers.IGameManager;
import com.jcwhatever.pvs.api.arena.managers.ILobbyManager;
import com.jcwhatever.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.pvs.api.arena.options.ArenaStartReason;
import com.jcwhatever.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.pvs.api.arena.settings.ILobbySettings;
import com.jcwhatever.pvs.api.events.players.PlayerReadyEvent;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.arenas.settings.PVLobbySettings;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class PVLobbyManager extends AbstractPlayerManager implements ILobbyManager, IEventListener {

    private final ILobbySettings _settings;

    /*
     * Constructor.
     */
    public PVLobbyManager(IArena arena) {
        super(arena);

        _settings = new PVLobbySettings(arena);
        arena.getEventManager().register(this);
    }

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    public ILobbySettings getSettings() {
        return _settings;
    }

    @Override
    @Nullable
    public IArenaPlayerCollection getReadyGroup() {

        return _players.getReadyGroup(getArena().getSettings().getMinPlayers());
    }

    @Override
    @Nullable
    public IArenaPlayerCollection getNextGroup() {

        int minSize = Math.max(
                getArena().getSettings().getMinPlayers(),
                getSettings().getMinAutoStartPlayers());

        return _players.getNextGroup(minSize);
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

    /*
     * Attempt to start a game automatically.
     */
    private boolean tryAutoStart() {

        IGameManager gameManager = getArena().getGameManager();

        // make sure game isn't already running
        if (gameManager.isRunning())
            return false;

        // make sure arena isn't busy
        if (getArena().isBusy())
            return false;

        // make sure arena has auto start enabled
        // and count down isn't already running.
        if (getSettings().hasAutoStart()) {

            // get the next group
            IArenaPlayerCollection nextGroup = getNextGroup();

            // group not found
            if (nextGroup == null || nextGroup.isEmpty())
                return false;

            // make sure group meets the minimum auto start player size setting
            if (nextGroup.size() < getSettings().getMinAutoStartPlayers())
                return false;

            // start
            return getArena().getGameManager().start(ArenaStartReason.AUTO);
        }

        return false;
    }

    /*
     * Attempt to start a game for ready players.
     */
    private boolean tryReadyStart() {

        // make sure game isn't running
        if (getArena().getGameManager().isRunning())
            return false;

        // make sure arena isn't busy
        if (getArena().isBusy())
            return false;

        // check to see if there is a group that is ready
        IArenaPlayerCollection ready = getArena().getLobbyManager().getReadyGroup();
        if (ready == null || ready.isEmpty())
            return false;

        // make sure the size of the group meets the min
        // players requirement
        return ready.size() >= getArena().getSettings().getMinPlayers() &&
                // start
                getArena().getGameManager().start(ArenaStartReason.PLAYERS_READY);

    }

    /*
     * Get a lobby spawn location for a player.
     */
    @Nullable
    private Location getSpawnLocation(IArenaPlayer player) {
        Spawnpoint spawnpoint = getArena().getSpawnManager().getRandomLobbySpawn(player.getTeam());
        if (spawnpoint == null) {

            spawnpoint = getArena().getSpawnManager().getRandomGameSpawn(player.getTeam());
            if (spawnpoint == null) {

                Msg.warning("Failed to find a lobby spawn for a player in arena '{0}'.", getArena().getName());
                return null;
            }
        }

        return spawnpoint;
    }


    // try ready start when a player is ready
    @EventMethod
    private void onPlayerReady(@SuppressWarnings("UnusedParameters") PlayerReadyEvent event) {
        tryReadyStart();
    }
}
