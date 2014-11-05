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


package com.jcwhatever.bukkit.pvs;

import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.pvs.api.spawns.SpawnType;
import com.jcwhatever.bukkit.pvs.api.spawns.SpawnTypeManager;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.spawns.GameSpawnType;
import com.jcwhatever.bukkit.pvs.spawns.LobbySpawnType;
import com.jcwhatever.bukkit.pvs.spawns.LocationSpawnType;
import com.jcwhatever.bukkit.pvs.spawns.SpectatorSpawnType;

import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Manages types of spawns
 */
public class PVSpawnTypeManager implements SpawnTypeManager {

    public final LobbySpawnType _lobbySpawn = new LobbySpawnType();
    public final GameSpawnType _gameSpawn = new GameSpawnType();
    public final SpectatorSpawnType _spectatorSpawn = new SpectatorSpawnType();

    private Map<String, SpawnType> _typeMap = new HashMap<>(40);
    private Map<EntityType, Set<SpawnType>> _entityMap = new EnumMap<>(EntityType.class);

    /*
     * Constructor.
     */
    public PVSpawnTypeManager() {
        registerType(_gameSpawn);
        registerType(_lobbySpawn);
        registerType(_spectatorSpawn);
        registerType(new LocationSpawnType());
    }

    /*
     * Get spawn type used for arena lobby spawns.
     */
    @Override
    public SpawnType getLobbySpawnType() {
        return _lobbySpawn;
    }

    /*
     * Get spawn type used for arena game spawns.
     */
    @Override
    public SpawnType getGameSpawnType() {
        return _gameSpawn;
    }

    /*
     * Get spawn type used for arena spectator spawns.
     */
    @Override
    public SpawnType getSpectatorSpawnType() {
        return _spectatorSpawn;
    }

    /*
     * Get all registered spawn types.
     */
    @Override
    public List<SpawnType> getSpawnTypes() {
        return new ArrayList<SpawnType>(_typeMap.values());
    }

    /*
     * Get spawn types that can spawn the specified entity type.
     */
    @Override
    public List<SpawnType> getSpawnTypes(EntityType entityType) {
        Set<SpawnType> types = _entityMap.get(entityType);
        if (types == null)
            return new ArrayList<>(0);

        return new ArrayList<>(types);
    }

    /*
     * Get a spawn type by type name.
     */
    @Nullable
    @Override
    public SpawnType getType(String typeName) {
        return _typeMap.get(typeName.toLowerCase());
    }

    /*
     * Register a spawn type
     */
    @Override
    public boolean registerType(SpawnType spawnType) {

        if (_typeMap.containsKey(spawnType.getSearchName())) {
            Msg.debug("SpawnType '{0}' could not be registered because a type with that name is already registered", spawnType.getName());
            return false;
        }

        if (spawnType.getName() == null || spawnType.getName().isEmpty()) {
            throw new RuntimeException("Failed to register spawn type because it has no name.");
        }

        if (!TextUtils.isValidName(spawnType.getName())) {
            throw new RuntimeException("Failed to register spawn type because it has an invalid name.");
        }

        EntityType[] entities = spawnType.getEntityTypes();
        if (entities != null) {

            for (EntityType type : entities) {

                Set<SpawnType> types = _entityMap.get(type);
                if (types == null) {
                    types = new HashSet<>(5);
                    _entityMap.put(type, types);
                }

                types.add(spawnType);
            }
        }

        return _typeMap.put(spawnType.getSearchName(), spawnType) != null;
    }
}
