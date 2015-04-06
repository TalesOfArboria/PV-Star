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

import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.ArenaTeam;
import com.jcwhatever.pvs.api.arena.managers.ISpawnManager;
import com.jcwhatever.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.pvs.api.events.players.PlayerRemovedEvent;
import com.jcwhatever.pvs.api.events.spawns.ClearReservedSpawnsEvent;
import com.jcwhatever.pvs.api.events.spawns.ReserveSpawnEvent;
import com.jcwhatever.pvs.api.events.spawns.SpawnAddedEvent;
import com.jcwhatever.pvs.api.events.spawns.SpawnPreAddEvent;
import com.jcwhatever.pvs.api.events.spawns.SpawnPreRemoveEvent;
import com.jcwhatever.pvs.api.events.spawns.SpawnRemovedEvent;
import com.jcwhatever.pvs.api.events.spawns.UnreserveSpawnEvent;
import com.jcwhatever.pvs.api.spawns.SpawnType;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.pvs.api.utils.SpawnFilter;
import com.jcwhatever.pvs.spawns.SpawnpointsCollection;
import com.jcwhatever.nucleus.events.manager.IEventListener;
import com.jcwhatever.nucleus.events.manager.EventMethod;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Spawn manager implementation.
 */
public class PVSpawnManager extends SpawnpointsCollection implements ISpawnManager, IEventListener {

    private final Map<UUID, Spawnpoint> _reserved = new HashMap<>(15); // key is player id
    private final IArena _arena;
    private final IDataNode _dataNode;

    /*
     * Constructor.
     */
    public PVSpawnManager(IArena arena) {
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
        return !getSpawns(PVStarAPI.getSpawnTypeManager().getLobbySpawnType()).isEmpty();
    }

    @Override
    public boolean hasGameSpawns() {
        return !getSpawns(PVStarAPI.getSpawnTypeManager().getGameSpawnType()).isEmpty();
    }

    @Override
    public boolean hasSpectatorSpawns() {
        return !getSpawns(PVStarAPI.getSpawnTypeManager().getSpectatorSpawnType()).isEmpty();
    }

    @Override
    public List<Spawnpoint> getLobbyOrGameSpawns() {

        SpawnType lobbyType = PVStarAPI.getSpawnTypeManager().getLobbySpawnType();

        List<Spawnpoint> spawns = getSpawns(lobbyType);

        if (spawns.size() == 0) {
            SpawnType gameType = PVStarAPI.getSpawnTypeManager().getGameSpawnType();
            spawns = getSpawns(gameType);
        }

        return spawns;
    }

    @Override
    public List<Spawnpoint> getLobbySpawns() {
        return getSpawns(
                PVStarAPI.getSpawnTypeManager().getLobbySpawnType());
    }

    @Override
    public List<Spawnpoint> getGameSpawns() {
        return getSpawns(
                PVStarAPI.getSpawnTypeManager().getGameSpawnType());
    }

    @Override
    public List<Spawnpoint> getSpectatorSpawns() {
        return getSpawns(
                PVStarAPI.getSpawnTypeManager().getSpectatorSpawnType());
    }


    @Override
    public boolean addSpawn(Spawnpoint spawn) {
        PreCon.notNull(spawn);

        boolean isSpawnAdded = !getArena().getEventManager().call(this, new SpawnPreAddEvent(getArena(), spawn)).isCancelled()
                && super.addSpawn(spawn);

        if (!isSpawnAdded)
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
    public void addSpawns(final Collection<? extends Spawnpoint> spawns) {
        PreCon.notNull(spawns);
        for (Spawnpoint spawn : spawns) {
            addSpawn(spawn);
        }
    }

    @Override
    public boolean removeSpawn(Spawnpoint spawn) {
        PreCon.notNull(spawn);

        boolean isRemoved = !getArena().getEventManager().call(this, new SpawnPreRemoveEvent(getArena(), spawn)).isCancelled()
                && super.removeSpawn(spawn);

        if (!isRemoved)
            return false;

        IDataNode node = _dataNode.getNode(spawn.getName());
        node.remove();
        node.save();

        getArena().getEventManager().call(this, new SpawnRemovedEvent(getArena(), spawn));

        return true;

    }

    @Override
    public void removeSpawns(final Collection<? extends Spawnpoint> spawns) {
        PreCon.notNull(spawns);

        for (Spawnpoint spawn : spawns)
            removeSpawn(spawn);
    }

    @Nullable
    @Override
    public Spawnpoint getRandomSpawn(IArenaPlayer player) {

        switch (player.getArenaRelation()) {

            case LOBBY:
                return getRandomLobbySpawn(player.getTeam());
            case GAME:
                return getRandomGameSpawn(player.getTeam());
            case SPECTATOR:
                return getRandomSpectatorSpawn(player.getTeam());
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Spawnpoint getRandomLobbySpawn(ArenaTeam team) {

        // get lobby spawns
        List<Spawnpoint> spawns = getSpawns(
                PVStarAPI.getSpawnTypeManager().getLobbySpawnType(), team);

        if (spawns == null || spawns.size() == 0) {

            // use game spawns if there are no lobby spawns
            spawns = getSpawns(
                    PVStarAPI.getSpawnTypeManager().getGameSpawnType(), team);

            if (spawns == null || spawns.size() == 0) {
                return null;
            }
        }

        return SpawnFilter.getRandomSpawn(spawns);
    }

    @Nullable
    @Override
    public Spawnpoint getRandomGameSpawn(ArenaTeam team) {
        return SpawnFilter.getRandomSpawn(
                PVStarAPI.getSpawnTypeManager().getGameSpawnType(), team, this.getSpawns());
    }

    @Nullable
    @Override
    public Spawnpoint getRandomSpectatorSpawn(ArenaTeam team) {
        return SpawnFilter.getRandomSpawn(
                PVStarAPI.getSpawnTypeManager().getLobbySpawnType(), team, this.getSpawns());
    }

    @Override
    public void reserveSpawn(IArenaPlayer player, Spawnpoint spawn) {
        PreCon.notNull(player);
        PreCon.notNull(spawn);

        if (getArena().getEventManager().call(this, new ReserveSpawnEvent(getArena(), player, spawn)).isCancelled())
            return;

        // remove spawn to prevent it's use
        super.removeSpawn(spawn);
        _reserved.put(player.getUniqueId(), spawn);
    }

    @Override
    public void unreserveSpawn(IArenaPlayer player) {
        PreCon.notNull(player);

        Spawnpoint spawn = _reserved.remove(player.getUniqueId());
        if (spawn == null)
            return;

        if (getArena().getEventManager().call(this, new UnreserveSpawnEvent(getArena(), player, spawn)).isCancelled()) {
            _reserved.put(player.getUniqueId(), spawn);
            return;
        }

        super.addSpawn(spawn);
    }

    @Override
    public void clearReserved() {

        if (getArena().getEventManager().call(this, new ClearReservedSpawnsEvent(getArena())).isCancelled())
            return;

        for (Spawnpoint spawn : _reserved.values()) {
            super.addSpawn(spawn);
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

            super.addSpawn(spawnpoint);
        }
    }

    /*
     * Un-reserve a spawn when a player leaves.
     */
    @EventMethod
    private void onPlayerRemoved(PlayerRemovedEvent event) {

        if (event.getReason() == RemovePlayerReason.ARENA_RELATION_CHANGE)
            return;

        unreserveSpawn(event.getPlayer());
    }
}
