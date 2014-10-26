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


package com.jcwhatever.bukkit.pvs.arenas.managers;

import com.jcwhatever.bukkit.generic.events.GenericsEventHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventListener;
import com.jcwhatever.bukkit.generic.storage.BatchOperation;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaTeam;
import com.jcwhatever.bukkit.pvs.api.arena.managers.SpawnManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerRemovedEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.AddSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.ClearReservedSpawnsEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.RemoveSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.ReserveSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.events.spawns.UnreserveSpawnEvent;
import com.jcwhatever.bukkit.pvs.api.spawns.SpawnType;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.api.utils.SpawnFilter;
import com.jcwhatever.bukkit.pvs.spawns.SpawnpointsCollection;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Spawn manager implementation.
 */
public class PVSpawnManager extends SpawnpointsCollection implements SpawnManager, GenericsEventListener {

    private final Map<UUID, Spawnpoint> _reserved = new HashMap<>(15); // key is player id
    private final Arena _arena;
    private final IDataNode _dataNode;

    /*
     * Constructor.
     */
    public PVSpawnManager(Arena arena) {
        _arena = arena;
        _dataNode = arena.getDataNode("spawns");

        loadSettings();

        arena.getEventManager().register(this);
    }

    /*
     * Get the owning arena.
     */
    @Override
    public Arena getArena() {
        return _arena;
    }

    /*
     * Determine if there are lobby spawns available.
     */
    @Override
    public boolean hasLobbySpawns() {
        return !getSpawns(PVStarAPI.getSpawnTypeManager().getLobbySpawnType()).isEmpty();
    }

    /*
     * Determine if there are game spawns available.
     */
    @Override
    public boolean hasGameSpawns() {
        return !getSpawns(PVStarAPI.getSpawnTypeManager().getGameSpawnType()).isEmpty();
    }

    /*
     * Determine if there are spectator spawns available.
     */
    @Override
    public boolean hasSpectatorSpawns() {
        return !getSpawns(PVStarAPI.getSpawnTypeManager().getSpectatorSpawnType()).isEmpty();
    }

    /*
     * Get all lobby spawn points. If there are no lobby spawns,
     * returns game spawns.
     */
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

    /*
     * Get all lobby spawn points.
     */
    @Override
    public List<Spawnpoint> getLobbySpawns() {
        return getSpawns(
                PVStarAPI.getSpawnTypeManager().getLobbySpawnType());
    }

    /*
     * Get all game spawn points.
     */
    @Override
    public List<Spawnpoint> getGameSpawns() {
        return getSpawns(
                PVStarAPI.getSpawnTypeManager().getGameSpawnType());
    }

    /*
     * Get all spectator spawn points.
     */
    @Override
    public List<Spawnpoint> getSpectatorSpawns() {
        return getSpawns(
                PVStarAPI.getSpawnTypeManager().getSpectatorSpawnType());
    }


    /*
     * Add a spawn point.
     */
    @Override
    public boolean addSpawn(Spawnpoint spawn) {
        PreCon.notNull(spawn);

        boolean isSpawnAdded = !getArena().getEventManager().call(new AddSpawnEvent(getArena(), spawn)).isCancelled()
                && super.addSpawn(spawn);

        if (!isSpawnAdded)
            return false;

        IDataNode node = _dataNode.getNode(spawn.getName());

        node.set("type", spawn.getSpawnType().getName());
        node.set("team", spawn.getTeam());
        node.set("location", spawn.getLocation());

        node.saveAsync(null);

        return true;
    }

    /*
     * Add a collection of spawnpoints.
     */
    @Override
    public void addSpawns(final Collection<Spawnpoint> spawns) {
        PreCon.notNull(spawns);

        _dataNode.runBatchOperation(new BatchOperation() {

            @Override
            public void run(IDataNode config) {
                for (Spawnpoint spawn : spawns) {
                    addSpawn(spawn);
                }
            }
        });
    }

    /*
     * Remove a spawn point.
     */
    @Override
    public boolean removeSpawn(Spawnpoint spawn) {
        PreCon.notNull(spawn);

        boolean isRemoved = !getArena().getEventManager().call(new RemoveSpawnEvent(getArena(), spawn)).isCancelled()
                && super.removeSpawn(spawn);

        if (!isRemoved)
            return false;

        IDataNode node = _dataNode.getNode(spawn.getName());
        node.remove();
        node.saveAsync(null);

        return true;

    }

    /*
     * Remove a collection of spawnpoints.
     */
    @Override
    public void removeSpawns(final Collection<Spawnpoint> spawns) {
        PreCon.notNull(spawns);

        _dataNode.runBatchOperation(new BatchOperation() {

            @Override
            public void run(IDataNode config) {
                for (Spawnpoint spawn : spawns)
                    removeSpawn(spawn);
            }
        });
    }

    /*
     * Get a random spawn for a player. The spawn returned correlates
     * to the players current arena relation. (i.e player in lobby gets a lobby spawn)
     * Returns null if the player is not in an arena or a related spawn is not found.
     */
    @Nullable
    @Override
    public Spawnpoint getRandomSpawn(ArenaPlayer player) {

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

    /*
     * Get a random lobby spawn.
     */
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


    /*
     * Get a random game spawn.
     */
    @Nullable
    @Override
    public Spawnpoint getRandomGameSpawn(ArenaTeam team) {
        return SpawnFilter.getRandomSpawn(
                PVStarAPI.getSpawnTypeManager().getGameSpawnType(), team, this.getSpawns());
    }

    /*
     * Get a random spectator spawn.
     */
    @Nullable
    @Override
    public Spawnpoint getRandomSpectatorSpawn(ArenaTeam team) {
        return SpawnFilter.getRandomSpawn(
                PVStarAPI.getSpawnTypeManager().getLobbySpawnType(), team, this.getSpawns());
    }

    /*
     * Reserves a spawn point for a player by removing it as a candidate
     * for the managers getter methods (getRandomSpawn, getSpawns, etc).
     */
    @Override
    public void reserveSpawn(ArenaPlayer p, Spawnpoint spawn) {
        PreCon.notNull(p);
        PreCon.notNull(spawn);

        if (getArena().getEventManager().call(new ReserveSpawnEvent(getArena(), p, spawn)).isCancelled())
            return;

        // remove spawn to prevent it's use
        super.removeSpawn(spawn);
        _reserved.put(p.getUniqueId(), spawn);
    }

    /*
     * Removes the reserved status of the spawnpoint reserved for a player
     * and makes it available via the managers spawnpoint getter methods.
     */
    @Override
    public void unreserveSpawn(ArenaPlayer p) {
        PreCon.notNull(p);

        Spawnpoint spawn = _reserved.remove(p.getUniqueId());
        if (spawn == null)
            return;

        if (getArena().getEventManager().call(new UnreserveSpawnEvent(getArena(), p, spawn)).isCancelled()) {
            _reserved.put(p.getUniqueId(), spawn);
            return;
        }

        super.addSpawn(spawn);
    }

    /*
     * Clear all reserved spawns and make them available via the managers
     * spawnpoint getter methods.
     */
    @Override
    public void clearReserved() {

        if (getArena().getEventManager().call(new ClearReservedSpawnsEvent(getArena())).isCancelled())
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

        Set<String> spawnNames = _dataNode.getSubNodeNames();
        if (spawnNames != null && !spawnNames.isEmpty()) {

            for (String spawnName : spawnNames) {

                IDataNode spawnNode = _dataNode.getNode(spawnName);

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

                Spawnpoint spawnpoint = new Spawnpoint(spawnName, type, team, location);

                super.addSpawn(spawnpoint);
            }
        }
    }


    /*
     * Un-reserve a spawn when a player leaves.
     */
    @GenericsEventHandler
    private void onPlayerRemoved(PlayerRemovedEvent event) {

        if (event.getReason() == RemovePlayerReason.ARENA_RELATION_CHANGE)
            return;

        unreserveSpawn(event.getPlayer());
    }
}
