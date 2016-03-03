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


package com.jcwhatever.pvs.arenas.context;

import com.jcwhatever.nucleus.managed.teleport.TeleportMode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Result;
import com.jcwhatever.pvs.ArenaPlayersCollection;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.collections.IArenaPlayerCollection;
import com.jcwhatever.pvs.api.arena.context.IContextManager;
import com.jcwhatever.pvs.api.arena.options.AddToContextReason;
import com.jcwhatever.pvs.api.arena.options.RemoveFromContextReason;
import com.jcwhatever.pvs.api.arena.settings.IContextSettings;
import com.jcwhatever.pvs.api.events.players.PlayerAddToContextEvent;
import com.jcwhatever.pvs.api.events.players.PlayerArenaSpawnedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerPreAddToContextEvent;
import com.jcwhatever.pvs.api.events.players.PlayerPreRemoveFromContextEvent;
import com.jcwhatever.pvs.api.events.players.PlayerRemovedFromContextEvent;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.pvs.arenas.AbstractArena;
import com.jcwhatever.pvs.players.ArenaPlayer;
import org.bukkit.Location;

import javax.annotation.Nullable;

/**
 * Abstract implementation for player managers
 * (Lobby, Game and Spectator Manager)
 */
public abstract class AbstractContextManager implements IContextManager {

    private final AbstractArena _arena;
    protected final ArenaPlayersCollection _players;

    /*
     * Constructor.
     */
    public AbstractContextManager(AbstractArena arena) {
        PreCon.notNull(arena);

        _arena = arena;
        _players = new ArenaPlayersCollection(arena);
    }

    @Override
    public final AbstractArena getArena() {
        return _arena;
    }

    @Override
    public final void tell(CharSequence message, Object... params) {
        _players.tell(message, params);
    }

    @Override
    public final IArenaPlayerCollection getPlayers() {
        return _players.getPlayers();
    }

    public final boolean addPlayer(IArenaPlayer player, AddToContextReason reason) {
        PreCon.isValid(player instanceof ArenaPlayer);
        PreCon.notNull(reason);

        if (_players.hasPlayer(player))
            return false;

        if (!getArena().getSettings().isEnabled())
            return false;

        if (reason != AddToContextReason.CONTEXT_CHANGE) {

            if (_arena.getEventManager().call(this,
                    new PlayerPreAddToContextEvent(_arena, player, this, reason)).isCancelled())
                return false;
        }

        _players.addPlayer(player);

        ((ArenaPlayer)player).setCurrentArena(_arena);

        Location spawnPoint = onPrePlayerAdd(player, reason);

        PlayerAddToContextEvent contextEvent = new PlayerAddToContextEvent(
                _arena, player, this, getContext(), reason, spawnPoint, null);
        _arena.getEventManager().call(this, contextEvent);

        contextEvent = onPlayerAdded(player, reason, contextEvent);

        TeleportMode teleportMode = getSettings().getTeleportMode();
        Location location = contextEvent.getSpawnLocation();
        // teleport player to spawn location from event
        if (contextEvent.getSpawnLocation() != null
                && player.teleport(location, teleportMode)) {

            PlayerArenaSpawnedEvent spawnEvent = new PlayerArenaSpawnedEvent(
                    _arena, player, this, contextEvent.getSpawnLocation());
            _arena.getEventManager().call(this, spawnEvent);

            // reserve spawnpoint
            if (contextEvent.getSpawnLocation() instanceof Spawnpoint) {
                IContextSettings settings = player.getContextSettings();
                if (settings != null && settings.isPlayerSpawnsReserved()) {

                    getArena().getSpawns().reserve(player, (Spawnpoint) contextEvent.getSpawnLocation());

                }
            }
        }

        // display message from event
        if (contextEvent.getMessage() != null) {
            tell(contextEvent.getMessage());
        }

        return true;
    }

    public final Result<Location> removePlayer(IArenaPlayer player, RemoveFromContextReason reason) {
        PreCon.isValid(player instanceof ArenaPlayer);
        PreCon.notNull(reason);

        // make sure the manager has the player
        if (!_players.hasPlayer(player))
            return new Result<>(false);

        if (reason != RemoveFromContextReason.CONTEXT_CHANGE) {

            // call pre remove event to see if the remove event is cancelled
            if (_arena.getEventManager().call(this,
                    new PlayerPreRemoveFromContextEvent(_arena, player, this, reason))
                    .isCancelled())
                return new Result<>(false);
        }

        onPreRemovePlayer(player, reason);

        getArena().getSpawns().unreserve(player);

        // remove player from collection
        _players.removePlayer(player, reason);

        Location restoreLocation = onRemovePlayer(player, reason);

        // call player removed event

        PlayerRemovedFromContextEvent removedEvent =
                new PlayerRemovedFromContextEvent(_arena, player, this, getContext(), reason);
        _arena.getEventManager().call(this, removedEvent);

        return new Result<>(true, restoreLocation);
    }

    /**
     * Invoked before a player is added.
     *
     * @return  The location the player should be spawned at or null to prevent teleport.
     */
    @Nullable
    protected abstract Location onPrePlayerAdd(IArenaPlayer player, AddToContextReason reason);

    /**
     * Invoked after a player is added.
     *
     * @return  The context event to retrieve modified event values from.
     */
    protected abstract PlayerAddToContextEvent onPlayerAdded(
            IArenaPlayer player, AddToContextReason reason, PlayerAddToContextEvent event);

    /**
     * Invoked before a player is removed.
     */
    protected abstract void onPreRemovePlayer(IArenaPlayer player, RemoveFromContextReason reason);

    /**
     * Invoked after a player is removed.
     *
     * @return  The location the player should be teleported after removed or null to
     * prevent teleport.
     */
    @Nullable
    protected abstract Location onRemovePlayer(IArenaPlayer player, RemoveFromContextReason reason);
}
