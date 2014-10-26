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


package com.jcwhatever.bukkit.pvs.scripting.api;

import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaTeam;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptApi;
import com.jcwhatever.bukkit.pvs.api.spawns.SpawnType;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides scripts with Api support to the arena spawn manager.
 */
public class SpawnApi extends ScriptApi {

    @Override
    public String getVariableName() {
        return "spawns";
    }

    @Override
    protected IScriptApiObject onCreateApiObject(Arena arena, EvaluatedScript script) {
        return new ApiObject(arena);
    }

    public static class ApiObject implements IScriptApiObject {

        private final Arena _arena;

        /**
         * Constructor.
         *
         * @param arena  The owning arena.
         */
        ApiObject(Arena arena) {
            _arena = arena;
        }

        /**
         * Reset the api and release resources.
         */
        @Override
        public void reset() {
            // do nothing
        }

        /**
         * Determine if there are lobby spawns available.
         */
        public boolean hasLobbySpawns() {
            return _arena.getSpawnManager().hasLobbySpawns();
        }

        /**
         * Determine if there are game spawns available.
         */
        public boolean hasGameSpawns() {
            return _arena.getSpawnManager().hasGameSpawns();
        }

        /**
         * Determine if there are spectator spawns available.
         */
        public boolean hasSpectatorSpawns() {
            return _arena.getSpawnManager().hasSpectatorSpawns();
        }

        /**
         * Get all lobby spawn points. If there are no
         * lobby spawns, returns all game spawns.
         */
        public List<Spawnpoint> getLobbyOrGameSpawn() {
            return _arena.getSpawnManager().getLobbyOrGameSpawns();
        }

        /**
         * Get all game spawn points.
         */
        public List<Spawnpoint> getGameSpawns() {
            return _arena.getSpawnManager().getGameSpawns();
        }

        /**
         * Get all lobby spawn points.
         */
        public List<Spawnpoint> getLobbySpawns() {
            return _arena.getSpawnManager().getLobbySpawns();
        }

        /**
         * Get all spectator spawn points.
         */
        public List<Spawnpoint> getSpectatorSpawns() {
            return _arena.getSpawnManager().getSpectatorSpawns();
        }

        /**
         * Get a random spawn for a player. The spawn returned correlates
         * to the players current arena relation. (i.e player in lobby gets a lobby spawn)
         *
         * @param player  The player to get a random spawn for.
         *
         * @return  Null if the player is not in an arena.
         */
        @Nullable
        public Spawnpoint getRandomSpawn(ArenaPlayer player) {
            PreCon.notNull(player);

            return _arena.getSpawnManager().getRandomSpawn(player);
        }

        /**
         * Get a random lobby spawn.
         *
         * @param teamName  The name of the team the spawn is for.
         */
        @Nullable
        public Spawnpoint getRandomLobbySpawn(String teamName) {
            PreCon.notNullOrEmpty(teamName);

            ArenaTeam team = getEnum(teamName, ArenaTeam.class);

            return _arena.getSpawnManager().getRandomLobbySpawn(team);
        }

        /**
         * Get a random game spawn.
         *
         * @param teamName  The team the spawn is for.
         */
        @Nullable
        public Spawnpoint getRandomGameSpawn(String teamName) {
            PreCon.notNullOrEmpty(teamName);

            ArenaTeam team = getEnum(teamName, ArenaTeam.class);

            return _arena.getSpawnManager().getRandomGameSpawn(team);
        }

        /**
         * Get a random spectator spawn.
         *
         * @param teamName  The team the spawn is for.
         */
        @Nullable
        public Spawnpoint getRandomSpectatorSpawn(String teamName) {
            PreCon.notNullOrEmpty(teamName);

            ArenaTeam team = getEnum(teamName, ArenaTeam.class);

            return _arena.getSpawnManager().getRandomSpectatorSpawn(team);
        }

        /**
         * Get a spawn by it's name.
         *
         * @param name  The name of the spawn.
         */
        @Nullable
        public Spawnpoint getSpawn(String name) {
            PreCon.notNullOrEmpty(name);

            return _arena.getSpawnManager().getSpawn(name);
        }

        /**
         * Get all spawnpoints
         */
        public List<Spawnpoint> getSpawns() {
            return _arena.getSpawnManager().getSpawns();
        }

        /**
         * Get all spawnpoints from a comma delimited string of spawn names.
         *
         * @param spawnNames  The names of the spawns to retrieve.
         */
        public List<Spawnpoint> getSpawnsByNames(String spawnNames) {
            PreCon.notNullOrEmpty(spawnNames);

            return _arena.getSpawnManager().getSpawns(spawnNames);
        }

        /**
         * Get all spawns of the specified type.
         *
         * @param typeName  The spawn type.
         */
        public List<Spawnpoint> getSpawnsByType(String typeName) {
            PreCon.notNullOrEmpty(typeName);

            SpawnType type = PVStarAPI.getSpawnTypeManager().getType(typeName);
            if (type == null)
                return new ArrayList<>(0);

            return _arena.getSpawnManager().getSpawns(type);
        }

        /**
         * Get all spawns for the specified team.
         *
         * @param teamName  The arena team.
         */
        public List<Spawnpoint> getSpawnsByTeam(String teamName) {
            PreCon.notNullOrEmpty(teamName);

            ArenaTeam team = getEnum(teamName, ArenaTeam.class);

            return _arena.getSpawnManager().getSpawns(team);
        }

        /**
         * Get all spawns for the specified team and type.
         *
         * @param typeName  The spawn type.
         * @param teamName  The arena team.
         */
        public List<Spawnpoint> getSpawnsByTypeAndTeam(String typeName, String teamName) {
            PreCon.notNullOrEmpty(typeName);
            PreCon.notNullOrEmpty(teamName);

            SpawnType type = PVStarAPI.getSpawnTypeManager().getType(typeName);
            if (type == null)
                return new ArrayList<>(0);

            ArenaTeam team = getEnum(teamName, ArenaTeam.class);

            return _arena.getSpawnManager().getSpawns(type, team);
        }

        /**
         * Reserves a spawn point for a player by removing it as a candidate
         * for the managers getter methods (getRandomSpawn, getSpawns, etc).
         *
         * @param player  The player to reserve the spawn for.
         * @param spawn   The spawnpoint to reserve.
         */
        public void reserveSpawn(Object player, Spawnpoint spawn) {
            PreCon.notNull(player);
            PreCon.notNull(spawn);

            ArenaPlayer p = PVStarAPI.getArenaPlayer(player);

            _arena.getSpawnManager().reserveSpawn(p, spawn);
        }

        /**
         * Removes the reserved status of the spawnpoint reserved for a player
         * and makes it available via the managers spawnpoint getter methods.
         *
         * @param player  The player the spawn was reserved for.
         */
        public void unreserveSpawn(Object player) {
            PreCon.notNull(player);

            ArenaPlayer p = PVStarAPI.getArenaPlayer(player);

            _arena.getSpawnManager().unreserveSpawn(p);
        }

        /**
         * Clear all reserved spawns and make them available via the managers
         * spawnpoint getter methods.
         */
        public void clearReserved() {
            _arena.getSpawnManager().clearReserved();
        }

        // convert enum constant name into enum constant
        private <T extends Enum<T>> T getEnum(String constantName, Class<T> enumClass) {
            constantName = constantName.toUpperCase();

            for (T constant : enumClass.getEnumConstants()) {
                if (constant.name().equals(constantName))
                    return constant;
            }

            throw new RuntimeException("Could not find enum constant named '" + constantName + "' in enum type " + enumClass.getSimpleName());
        }
    }
}
