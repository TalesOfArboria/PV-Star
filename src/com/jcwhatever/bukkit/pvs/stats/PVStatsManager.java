package com.jcwhatever.bukkit.pvs.stats;

import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.stats.ArenaStats;
import com.jcwhatever.bukkit.pvs.api.stats.StatType;
import com.jcwhatever.bukkit.pvs.api.stats.StatsManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
