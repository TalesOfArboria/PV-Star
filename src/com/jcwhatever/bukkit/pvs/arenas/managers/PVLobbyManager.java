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


package com.jcwhatever.bukkit.pvs.arenas.managers;

import com.jcwhatever.bukkit.generic.events.manager.GenericsEventHandler;
import com.jcwhatever.bukkit.generic.events.manager.IGenericsEventListener;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.managers.GameManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.LobbyManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.ArenaStartReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.settings.LobbyManagerSettings;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerReadyEvent;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.arenas.settings.PVLobbySettings;

import org.bukkit.Location;

import java.util.List;
import javax.annotation.Nullable;

public class PVLobbyManager extends AbstractPlayerManager implements LobbyManager, IGenericsEventListener {

    private LobbyManagerSettings _settings;

    /*
     * Constructor.
     */
    public PVLobbyManager(Arena arena) {
        super(arena);

        _settings = new PVLobbySettings(arena);
        arena.getEventManager().register(this);
    }

    /*
     * Get lobby settings.
     */
    @Override
    public LobbyManagerSettings getSettings() {
        return _settings;
    }

    /*
     * Get the next group of players that are all ready.
     */
    @Override
    @Nullable
    public List<ArenaPlayer> getReadyGroup() {

        return _players.getReadyGroup(getArena().getSettings().getMinPlayers());
    }

    /*
     * Get the next group of players that meat minimum players
     * and minimum auto start players settings.
     */
    @Override
    @Nullable
    public List<ArenaPlayer> getNextGroup() {

        int minSize = Math.max(
                getArena().getSettings().getMinPlayers(),
                getSettings().getMinAutoStartPlayers());

        return _players.getNextGroup(minSize);
    }

    /*
     * Called to get a respawn location.
     */
    @Nullable
    @Override
    protected Location onRespawnPlayer(ArenaPlayer player) {
        return getSpawnLocation(player);
    }

    /*
     * Called to get a spawn location on a player added to the arena.
     */
    @Override
    protected Location onAddPlayer(ArenaPlayer player, AddPlayerReason reason) {

        return getSpawnLocation(player);
    }

    /*
     * Called before a player is removed from the lobby.
     */
    @Override
    protected void onPreRemovePlayer(ArenaPlayer player, RemovePlayerReason reason) {
        // do nothing
    }

    /*
     * Called after a player is removed from the lobby in
     * order to get a location to send the player.
     */
    @Override
    protected Location onRemovePlayer(ArenaPlayer player, RemovePlayerReason reason) {
        return getArena().getSettings().getRemoveLocation();
    }

    /*
     * Attempt to start a game automatically.
     */
    private boolean tryAutoStart() {

        GameManager gameManager = getArena().getGameManager();

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
            List<ArenaPlayer> nextGroup = getNextGroup();

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
        List<ArenaPlayer> ready = getArena().getLobbyManager().getReadyGroup();
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
    private Location getSpawnLocation(ArenaPlayer player) {
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
    @GenericsEventHandler
    private void onPlayerReady(@SuppressWarnings("UnusedParameters") PlayerReadyEvent event) {
        tryReadyStart();
    }
}
