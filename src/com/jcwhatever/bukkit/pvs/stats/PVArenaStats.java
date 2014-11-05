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

import com.jcwhatever.bukkit.generic.collections.WeakValueMap;
import com.jcwhatever.bukkit.generic.storage.DataStorage;
import com.jcwhatever.bukkit.generic.storage.DataStorage.DataPath;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.Scheduler;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.stats.ArenaStats;
import com.jcwhatever.bukkit.pvs.api.stats.StatTracking.StatTrackType;
import com.jcwhatever.bukkit.pvs.api.stats.StatType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PVArenaStats implements ArenaStats {

    private static Set<IDataNode> _nodesToSave = new HashSet<>(100);
    private Map<StatType, IDataNode> _cachedNodes = new WeakValueMap<>(25);

    static {
        // schedule data saving
        Scheduler.runTaskRepeatAsync(PVStarAPI.getPlugin(), 30, 30, new SaveTask());
    }

    private final UUID _arenaId;

    public PVArenaStats(UUID arenaId) {
        PreCon.notNull(arenaId);

        _arenaId = arenaId;
    }

    @Override
    public UUID getArenaId() {
        return _arenaId;
    }

    private IDataNode getNode(StatType type) {

        IDataNode node = _cachedNodes.get(type);
        if (node == null) {
            node = DataStorage.getStorage(PVStarAPI.getPlugin(), new DataPath("stats." + _arenaId + '.' + type.getName()));
            node.load();
            _cachedNodes.put(type, node);
        }

        return node;
    }

    @Override
    public double getValue(StatType type, UUID playerId, StatTrackType trackingType) {
        return getValue(type, playerId.toString(), trackingType);
    }

    @Override
    public double getValue(StatType type, String playerId, StatTrackType trackingType) {
        PreCon.notNull(type);
        PreCon.notNullOrEmpty(playerId);
        PreCon.isValid(playerId.length() == 36);
        PreCon.notNull(trackingType);

        IDataNode node = getNode(type);

        switch (trackingType) {
            case TOTAL:
                return node.getDouble(playerId + ".total", 0.0D);
            case MIN:
                return node.getDouble(playerId + ".min", 0.0D);
            case MAX:
                return node.getDouble(playerId + ".max", 0.0D);
            default:
                return 0.0D;
        }
    }

    @Override
    public double addScore(StatType type, UUID playerId, double amount) {
        PreCon.notNull(type);
        PreCon.notNull(playerId);

        double current = getValue(type, playerId, StatTrackType.TOTAL);
        current += amount;

        IDataNode node = getNode(type);
        node.set(playerId.toString() + ".total", current);

        // add min score
        if (type.getTracking().hasType(StatTrackType.MIN)) {
            double min = Math.min(amount, node.getDouble(playerId.toString() + ".min"));
            node.set(playerId.toString() + ".min", min);
        }

        // add max score
        if (type.getTracking().hasType(StatTrackType.MAX)) {
            double max = Math.max(amount, node.getDouble(playerId.toString() + ".max"));
            node.set(playerId.toString() + ".max", max);
        }

        _nodesToSave.add(node.getRoot());

        return current;
    }

    @Override
    public Set<String> getRawPlayerIds(StatType type) {
        IDataNode node = getNode(type);

        return node.getSubNodeNames();
    }

    private static class SaveTask implements Runnable {

        @Override
        public void run() {

            if (_nodesToSave.isEmpty())
                return;

            for (IDataNode node : _nodesToSave) {
                node.saveAsync(null);
            }

            _nodesToSave.clear();
        }
    }

}
