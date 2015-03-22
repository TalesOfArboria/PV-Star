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

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Result;
import com.jcwhatever.pvs.ArenaPlayersCollection;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.pvs.api.arena.managers.PlayerManager;
import com.jcwhatever.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.pvs.api.arena.settings.PlayerManagerSettings;
import com.jcwhatever.pvs.api.events.players.PlayerAddedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerPreAddEvent;
import com.jcwhatever.pvs.api.events.players.PlayerPreRemoveEvent;
import com.jcwhatever.pvs.api.events.players.PlayerRemovedEvent;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;

import org.bukkit.Location;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Abstract implementation for player managers
 * (Lobby, Game and Spectator Manager)
 */
public abstract class AbstractPlayerManager implements PlayerManager {

    private final Arena _arena;
    protected final ArenaPlayersCollection _players;

    /*
     * Constructor.
     */
    public AbstractPlayerManager(Arena arena) {
        PreCon.notNull(arena);

        _arena = arena;
        _players = new ArenaPlayersCollection(arena);
    }

    @Override
    public final Arena getArena() {
        return _arena;
    }

    @Override
    public final void tell(String message, Object... params) {
        _players.tell(message, params);
    }

    @Override
    public final List<ArenaPlayer> getPlayers() {
        return _players.getPlayers();
    }

    @Override
    public final int getPlayerCount() {
        return _players.size();
    }

    @Override
    public final boolean hasPlayer(ArenaPlayer player) {
        return _players.hasPlayer(player);
    }

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

    @Override
    public final boolean addPlayer(ArenaPlayer player, AddPlayerReason reason) {
        PreCon.notNull(player);
        PreCon.notNull(reason);

        if (_players.hasPlayer(player))
            return false;

        if (!getArena().getSettings().isEnabled())
            return false;

        if (reason != AddPlayerReason.ARENA_RELATION_CHANGE) {

            if (_arena.getEventManager().call(this,
                    new PlayerPreAddEvent(_arena, player, this, reason)).isCancelled())
                return false;
        }

        _players.addPlayer(player);

        player.setCurrentArena(_arena);

        Location spawnPoint = onAddPlayer(player, reason);

        PlayerAddedEvent event = new PlayerAddedEvent(_arena, player, this, reason, spawnPoint, null);

        _arena.getEventManager().call(this, event);

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

    @Override
    public final Result<Location> removePlayer(ArenaPlayer player, RemovePlayerReason reason) {
        PreCon.notNull(player);
        PreCon.notNull(reason);

        // make sure the manager has the player
        if (!_players.hasPlayer(player))
            return new Result<>(false);

        if (reason != RemovePlayerReason.ARENA_RELATION_CHANGE) {

            // call pre remove event to see if the remove event is cancelled
            if (_arena.getEventManager().call(this,
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
        _arena.getEventManager().call(this, removedEvent);

        return new Result<>(true, restoreLocation);
    }

    /**
     * Invoked when a player is respawned.
     *
     * @return  The location the player should be respawned at or null to prevent
     * setting respawn location.
     */
    @Nullable
    protected abstract Location onRespawnPlayer(ArenaPlayer player);

    /**
     * Invoked when a player is added.
     *
     * @return  The location the player should be spawned at or null to prevent teleport.
     */
    @Nullable
    protected abstract Location onAddPlayer(ArenaPlayer player, AddPlayerReason reason);

    /**
     * Invoked before a player is removed.
     */
    protected abstract void onPreRemovePlayer(ArenaPlayer player, RemovePlayerReason reason);

    /**
     * Invoked after a player is removed.
     *
     * @return  The location the player should be teleported after removed or null to
     * prevent teleport.
     */
    @Nullable
    protected abstract Location onRemovePlayer(ArenaPlayer player, RemovePlayerReason reason);
}
