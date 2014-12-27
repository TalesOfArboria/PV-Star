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


package com.jcwhatever.bukkit.pvs.spawns;

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaTeam;
import com.jcwhatever.bukkit.pvs.api.spawns.SpawnType;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.api.utils.SpawnFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public abstract class SpawnpointsCollection {

    private Map<String, Spawnpoint> _nameMap = new HashMap<>(30);
    private Map<ArenaTeam, Set<Spawnpoint>> _teamMap = new EnumMap<>(ArenaTeam.class);
    private Map<SpawnType, Set<Spawnpoint>> _typeMap = new HashMap<>(30);

    public SpawnpointsCollection() {}

    public SpawnpointsCollection(Collection<Spawnpoint> spawns) {
        PreCon.notNull(spawns);

        addSpawns(spawns);
    }

    public Set<ArenaTeam> getTeams() {
        return EnumSet.copyOf(_teamMap.keySet());
    }

    public List<Spawnpoint> getSpawns() {
        return new ArrayList<Spawnpoint>(_nameMap.values());
    }

    public List<Spawnpoint> getSpawns(String names) {
        PreCon.notNull(names);

        String[] nameArray = TextUtils.PATTERN_COMMA.split(names);

        List<Spawnpoint> results = new ArrayList<>(nameArray.length);

        for (String name : nameArray) {
            Spawnpoint spawnpoint = getSpawn(name.trim());
            if (spawnpoint != null)
                results.add(spawnpoint);
        }

        return results;
    }

    public List<Spawnpoint> getSpawns(SpawnType type) {
        PreCon.notNull(type);

        Set<Spawnpoint> spawns = getTypeSet(type, false);
        if (spawns == null || spawns.isEmpty())
            return new ArrayList<>(0);

        return new ArrayList<Spawnpoint>(spawns);
    }

    public List<Spawnpoint> getSpawns(ArenaTeam team) {
        PreCon.notNull(team);

        Set<Spawnpoint> spawns = getTeamSet(team, false);
        if (spawns == null || spawns.isEmpty())
            return new ArrayList<>(0);

        return new ArrayList<>(spawns);
    }

    public List<Spawnpoint> getSpawns (SpawnType type, ArenaTeam team) {
        PreCon.notNull(type);
        PreCon.notNull(team);

        Set<Spawnpoint> spawns = getTeamSet(team, false);
        if (spawns == null || spawns.isEmpty())
            return new ArrayList<>(0);

        return SpawnFilter.filter(type, spawns);
    }


    @Nullable
    public Spawnpoint getSpawn(String name) {
        return _nameMap.get(name.toLowerCase());
    }

    public boolean addSpawn(Spawnpoint spawn) {
        PreCon.notNull(spawn);

        _nameMap.put(spawn.getSearchName(), spawn);

        Set<Spawnpoint> spawnTypeSet = getTypeSet(spawn.getSpawnType(), true);
        if (spawnTypeSet != null) {
            spawnTypeSet.add(spawn);
        }

        Set<Spawnpoint> spawnTeamSet = getTeamSet(spawn.getTeam(), true);
        if (spawnTeamSet != null) {
            spawnTeamSet.add(spawn);
        }

        return true;
    }

    public void addSpawns(Collection<Spawnpoint> spawns) {
        PreCon.notNull(spawns);

        for (Spawnpoint spawn : spawns) {
            addSpawn(spawn);
        }
    }

    public boolean removeSpawn(Spawnpoint spawn) {
        PreCon.notNull(spawn);

        Set<Spawnpoint> spawnTypeSet = getTypeSet(spawn.getSpawnType(), false);
        if (spawnTypeSet != null) {
            spawnTypeSet.remove(spawn);
        }

        Set<Spawnpoint> spawnTeamSet = getTeamSet(spawn.getTeam(), false);
        if (spawnTeamSet != null) {
            spawnTeamSet.remove(spawn);
        }

        return _nameMap.remove(spawn.getSearchName()) != null;
    }

    public void removeSpawns(Collection<Spawnpoint> spawns) {
        PreCon.notNull(spawns);

        for (Spawnpoint spawn : spawns)
            removeSpawn(spawn);
    }

    @Nullable
    protected Set<Spawnpoint> getTypeSet(SpawnType spawnType, boolean addIfNotExists) {
        Set<Spawnpoint> spawns = _typeMap.get(spawnType);
        if (spawns == null && addIfNotExists) {
            spawns = new HashSet<>(15);
            _typeMap.put(spawnType, spawns);
        }
        return spawns;
    }

    @Nullable
    protected Set<Spawnpoint> getTeamSet(ArenaTeam team, boolean addIfNotExists) {
        Set<Spawnpoint> spawns = _teamMap.get(team);
        if (spawns == null && addIfNotExists) {
            spawns = new HashSet<>(15);
            _teamMap.put(team, spawns);
        }
        return spawns;
    }

}
