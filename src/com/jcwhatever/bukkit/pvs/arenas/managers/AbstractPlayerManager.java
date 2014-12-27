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

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Result;
import com.jcwhatever.bukkit.pvs.ArenaPlayersCollection;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.managers.PlayerManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.settings.PlayerManagerSettings;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerAddedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerPreAddEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerPreRemoveEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerRemovedEvent;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;

import org.bukkit.Location;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Abstract implementation for player managers
 * (Lobby, Game and Spectator Manager)
 */
public abstract class AbstractPlayerManager implements PlayerManager {

    private Arena _arena;
    protected final ArenaPlayersCollection _players;

    /*
     * Constructor.
     */
    public AbstractPlayerManager(Arena arena) {
        _arena = arena;
        _players = new ArenaPlayersCollection(arena);
    }

    /*
     * Get the managers owning arena.
     */
    @Override
    public final Arena getArena() {
        return _arena;
    }

    /*
     * Tell all players being managed.
     */
    @Override
    public final void tell(String message, Object... params) {
        _players.tell(message, params);
    }

    /*
     * Get the players being managed.
     */
    @Override
    public final List<ArenaPlayer> getPlayers() {
        return _players.getPlayers();
    }

    /*
     * Get the number of players being managed.
     */
    @Override
    public final int getPlayerCount() {
        return _players.size();
    }

    /*
     * Determine if the manager is managing the specified player.
     */
    @Override
    public final boolean hasPlayer(ArenaPlayer player) {
        return _players.hasPlayer(player);
    }

    /*
     * Respawn the specified player if the player is being managed
     * by the manager instance.
     */
    @Override
    public final boolean respawnPlayer(ArenaPlayer player) {
        if (!_players.hasPlayer(player))
            return false;

        Location respawnLocation = onRespawnPlayer(player);
        if (respawnLocation == null)
            return false;

        player.getPlayer().teleport(respawnLocation);
        return true;
    }

    /*
     * Add a player to the manager instance to be managed.
     */
    @Override
    public final boolean addPlayer(ArenaPlayer player, AddPlayerReason reason) {
        PreCon.notNull(player);
        PreCon.notNull(reason);

        if (_players.hasPlayer(player))
            return false;

        if (!getArena().getSettings().isEnabled())
            return false;

        if (reason != AddPlayerReason.ARENA_RELATION_CHANGE) {

            if (_arena.getEventManager().call(
                    new PlayerPreAddEvent(_arena, player, this, reason)).isCancelled())
                return false;
        }

        _players.addPlayer(player);

        player.setCurrentArena(_arena);

        Location spawnPoint = onAddPlayer(player, reason);

        PlayerAddedEvent event = new PlayerAddedEvent(_arena, player, this, reason, spawnPoint, null);

        _arena.getEventManager().call(event);

        // teleport player to spawn location from event
        if (event.getSpawnLocation() != null) {

            player.getPlayer().teleport(event.getSpawnLocation());

            // reserve spawnpoint
            if (event.getSpawnLocation() instanceof Spawnpoint) {
                PlayerManagerSettings settings = player.getRelatedSettings();
                if (settings != null && settings.isPlayerSpawnsReserved()) {

                    getArena().getSpawnManager().reserveSpawn(player, (Spawnpoint)event.getSpawnLocation());

                }
            }
        }

        // display message from event
        if (event.getMessage() != null) {
            tell(event.getMessage());
        }

        return true;
    }

    /*
     * Remove a player from the manager instance.
     */
    @Override
    public final Result<Location> removePlayer(ArenaPlayer player, RemovePlayerReason reason) {
        PreCon.notNull(player);
        PreCon.notNull(reason);

        // make sure the manager has the player
        if (!_players.hasPlayer(player))
            return new Result<>(false);

        if (reason != RemovePlayerReason.ARENA_RELATION_CHANGE) {

            // call pre remove event to see if the remove event is cancelled
            if (_arena.getEventManager().call(
                    new PlayerPreRemoveEvent(_arena, player, this, reason))
                    .isCancelled())
                return new Result<>(false);
        }

        onPreRemovePlayer(player, reason);

        getArena().getSpawnManager().unreserveSpawn(player);

        // remove player from collection
        _players.removePlayer(player, reason);

        // clear arena if leaving
        if (reason != RemovePlayerReason.ARENA_RELATION_CHANGE) {

            player.clearArena();
        }

        Location restoreLocation = onRemovePlayer(player, reason);

        // call player removed event

        PlayerRemovedEvent removedEvent =
                new PlayerRemovedEvent(_arena, player, this, reason);
        _arena.getEventManager().call(removedEvent);

        return new Result<>(true, restoreLocation);
    }

    /*
     * Called when a player is respawned.
     * Returns the location the player should be respawned at.
     */
    @Nullable
    protected abstract Location onRespawnPlayer(ArenaPlayer player);

    /*
     * Called when a player is added.
     * Returns the location the player should be spawned at.
     */
    @Nullable
    protected abstract Location onAddPlayer(ArenaPlayer player, AddPlayerReason reason);

    /*
     * Called before a player is removed.
     */
    protected abstract void onPreRemovePlayer(ArenaPlayer player, RemovePlayerReason reason);

    /*
     * Called after a player is removed.
     * Returns the location the player should be teleported after removed.
     */
    @Nullable
    protected abstract Location onRemovePlayer(ArenaPlayer player, RemovePlayerReason reason);
}
