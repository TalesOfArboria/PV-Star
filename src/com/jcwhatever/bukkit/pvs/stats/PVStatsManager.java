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


package com.jcwhatever.bukkit.pvs.stats;

import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.stats.ArenaStats;
import com.jcwhatever.bukkit.pvs.api.stats.StatType;
import com.jcwhatever.bukkit.pvs.api.stats.StatsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public class PVStatsManager implements StatsManager {

    private final Map<UUID, ArenaStats> _arenaStats = new HashMap<>(50);
    private final Map<String, StatType> _typeMap = new HashMap<>(25);

    @Override
    public void registerType(StatType type) {
        PreCon.notNull(type);

        _typeMap.put(type.getName().toLowerCase(), type);
    }

    @Override
    public List<StatType> getTypes() {
        return new ArrayList<>(_typeMap.values());
    }

    @Nullable
    @Override
    public StatType getType(String name) {
        PreCon.notNullOrEmpty(name);

        return _typeMap.get(name.toLowerCase());
    }

    @Override
    public ArenaStats getArenaStats(UUID arenaId) {

        ArenaStats stats = _arenaStats.get(arenaId);
        if (stats == null) {

            stats = new PVArenaStats(arenaId);

            _arenaStats.put(arenaId, stats);
        }

        return stats;
    }
}
