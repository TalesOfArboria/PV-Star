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
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Rand;
import com.jcwhatever.nucleus.utils.coords.LocationUtils;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.ArenaTeam;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.managers.ISpawnManager;
import com.jcwhatever.pvs.api.arena.options.ArenaContext;
import com.jcwhatever.pvs.api.arena.options.RemoveFromContextReason;
import com.jcwhatever.pvs.api.events.players.PlayerRemovedFromContextEvent;
import com.jcwhatever.pvs.api.events.spawns.ClearReservedSpawnsEvent;
import com.jcwhatever.pvs.api.events.spawns.ReserveSpawnEvent;
import com.jcwhatever.pvs.api.events.spawns.SpawnAddedEvent;
import com.jcwhatever.pvs.api.events.spawns.SpawnPreAddEvent;
import com.jcwhatever.pvs.api.events.spawns.SpawnPreRemoveEvent;
import com.jcwhatever.pvs.api.events.spawns.SpawnRemovedEvent;
import com.jcwhatever.pvs.api.events.spawns.UnreserveSpawnEvent;
import com.jcwhatever.pvs.api.spawns.SpawnType;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.pvs.arenas.AbstractArena;
import com.jcwhatever.pvs.arenas.context.AbstractContextManager;
import com.jcwhatever.pvs.spawns.SpawnpointsCollection;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Spawn manager implementation.
 */
public class SpawnManager extends SpawnpointsCollection implements ISpawnManager, IEventListener {

    @Nullable
    public static Location getRespawnLocation(
            AbstractContextManager manager, ArenaContext context, Location output) {

        PreCon.notNull(manager);
        PreCon.notNull(context);
        PreCon.notNull(output);

        AbstractArena arena = manager.getArena();

        List<Spawnpoint> spawns = arena.getSpawns().getAll(context);

        if (spawns.isEmpty() && context == ArenaContext.LOBBY) {
            spawns = arena.getSpawns().getAll(ArenaContext.GAME);
        }

        if (spawns.isEmpty())
            return null;

        Location respawnLocation = Rand.get(spawns);
        if (respawnLocation == null)
            return null;

        return LocationUtils.copy(respawnLocation, output);
    }

    private final Map<UUID, Spawnpoint> _reserved = new HashMap<>(15); // key is player id
    private final IArena _arena;
    private final IDataNode _dataNode;

    /*
     * Constructor.
     */
    public SpawnManager(IArena arena) {
        _arena = arena;
        _dataNode = arena.getDataNode("spawns");

        loadSettings();

        arena.getEventManager().register(this);
    }

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    public IArena getArena() {
        return _arena;
    }

    @Override
    public boolean hasLobbySpawns() {
        return !getAll(PVStarAPI.getSpawnTypeManager().getLobbySpawnType()).isEmpty();
    }

    @Override
    public boolean hasGameSpawns() {
        return !getAll(PVStarAPI.getSpawnTypeManager().getGameSpawnType()).isEmpty();
    }

    @Override
    public boolean hasSpectatorSpawns() {
        return !getAll(PVStarAPI.getSpawnTypeManager().getSpectatorSpawnType()).isEmpty();
    }

    @Override
    public List<Spawnpoint> getAll(ArenaContext context) {

        switch (context) {
            case LOBBY:
                return getAll(
                        PVStarAPI.getSpawnTypeManager().getLobbySpawnType());

            case GAME:
                return getAll(
                        PVStarAPI.getSpawnTypeManager().getGameSpawnType());

            case SPECTATOR:
                return getAll(
                        PVStarAPI.getSpawnTypeManager().getSpectatorSpawnType());

            default:
                return null;
        }
    }

    @Override
    public List<Spawnpoint> getAll(ArenaTeam team, ArenaContext context) {
        PreCon.notNull(team);
        PreCon.notNull(context);

        SpawnType type;

        switch (context) {
            case LOBBY:
                type = PVStarAPI.getSpawnTypeManager().getLobbySpawnType();
                break;
            case GAME:
                type = PVStarAPI.getSpawnTypeManager().getGameSpawnType();
                break;
            case SPECTATOR:
                type = PVStarAPI.getSpawnTypeManager().getSpectatorSpawnType();
                break;
            default:
                return new ArrayList<>(0);
        }
        return getAll(type, team);
    }

    @Override
    public boolean add(Spawnpoint spawn) {
        PreCon.notNull(spawn);

        SpawnPreAddEvent event = new SpawnPreAddEvent(getArena(), spawn);
        getArena().getEventManager().call(this, event);

        if (event.isCancelled() || !super.add(spawn))
            return false;

        IDataNode node = _dataNode.getNode(spawn.getName());

        node.set("type", spawn.getSpawnType().getName());
        node.set("team", spawn.getTeam());
        node.set("location", spawn);

        node.save();

        getArena().getEventManager().call(this, new SpawnAddedEvent(getArena(), spawn));

        return true;
    }

    @Override
    public void addAll(final Collection<? extends Spawnpoint> spawns) {
        PreCon.notNull(spawns);
        for (Spawnpoint spawn : spawns) {
            add(spawn);
        }
    }

    @Override
    public boolean remove(Spawnpoint spawn) {
        PreCon.notNull(spawn);

        SpawnPreRemoveEvent preEvent = new SpawnPreRemoveEvent(getArena(), spawn);
        getArena().getEventManager().call(this, preEvent);

        if (preEvent.isCancelled() || !super.remove(spawn))
            return false;

        IDataNode node = _dataNode.getNode(spawn.getName());
        node.remove();
        node.save();

        getArena().getEventManager().call(this, new SpawnRemovedEvent(getArena(), spawn));

        return true;
    }

    @Override
    public void removeAll(final Collection<? extends Spawnpoint> spawns) {
        PreCon.notNull(spawns);

        for (Spawnpoint spawn : spawns)
            remove(spawn);
    }

    @Override
    public void reserve(IArenaPlayer player, Spawnpoint spawn) {
        PreCon.notNull(player);
        PreCon.notNull(spawn);

        ReserveSpawnEvent event = new ReserveSpawnEvent(getArena(), player, spawn);
        getArena().getEventManager().call(this, event);

        if (event.isCancelled())
            return;

        // remove spawn to prevent it's use
        super.remove(spawn);
        _reserved.put(player.getUniqueId(), spawn);
    }

    @Override
    public void unreserve(IArenaPlayer player) {
        PreCon.notNull(player);

        Spawnpoint spawn = _reserved.remove(player.getUniqueId());
        if (spawn == null)
            return;

        UnreserveSpawnEvent event = new UnreserveSpawnEvent(getArena(), player, spawn);
        getArena().getEventManager().call(this, event);

        if (event.isCancelled()) {
            _reserved.put(player.getUniqueId(), spawn);
            return;
        }

        super.add(spawn);
    }

    @Override
    public void clearReserved() {

        ClearReservedSpawnsEvent event = new ClearReservedSpawnsEvent(getArena());
        getArena().getEventManager().call(this, event);

        if (event.isCancelled())
            return;

        for (Spawnpoint spawn : _reserved.values()) {
            super.add(spawn);
        }

        _reserved.clear();
    }

    /*
     * Load spawn manager settings.
     */
    private void loadSettings() {

        for (IDataNode spawnNode : _dataNode) {

            String typeName = spawnNode.getString("type");
            Location location = spawnNode.getLocation("location");
            ArenaTeam team = spawnNode.getEnum("team", ArenaTeam.class);

            if (typeName == null || typeName.isEmpty())
                continue;

            if (location == null)
                continue;

            if (team == null)
                continue;

            SpawnType type = PVStarAPI.getSpawnTypeManager().getType(typeName);
            if (type == null)
                continue;

            Spawnpoint spawnpoint = new Spawnpoint(spawnNode.getName(), type, team, location);

            super.add(spawnpoint);
        }
    }

    /*
     * Un-reserve a spawn when a player leaves.
     */
    @EventMethod
    private void onPlayerRemoved(PlayerRemovedFromContextEvent event) {

        if (event.getReason() == RemoveFromContextReason.CONTEXT_CHANGE)
            return;

        unreserve(event.getPlayer());
    }
}
