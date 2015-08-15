/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
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

package com.jcwhatever.pvs.stats.sql;

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.stats.IPlayerStats;
import com.jcwhatever.pvs.api.stats.StatTracking;
import com.jcwhatever.pvs.api.stats.StatType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sql based implementation of {@link IPlayerStats} for arena data.
 */
public class SqlArenaPlayerStats implements IPlayerStats {

    private final SqlArenaStats _arenaStats;
    private final UUID _playerId;
    private final Map<String, SqlStat> _stats = new HashMap<>(15);

    /**
     * Constructor.
     *
     * @param arenaStats  The owning arena statistics.
     * @param playerId    The ID of the player the statistics belong to.
     */
    public SqlArenaPlayerStats(SqlArenaStats arenaStats, UUID playerId) {
        PreCon.notNull(arenaStats);
        PreCon.notNull(playerId);

        _arenaStats = arenaStats;
        _playerId = playerId;
    }

    @Override
    public UUID getPlayerId() {
        return _playerId;
    }

    @Override
    public double getScore(StatType type, StatTracking.StatTrackType trackingType) {

        SqlStat stat = _stats.get(type.getName());
        if (stat == null)
            return 0;

        return getStatValue(stat, trackingType);
    }

    @Override
    public void addScore(StatType type, double amount) {
        _arenaStats.addScore(_playerId, type, amount);
    }

    @Override
    public int hashCode() {
        return _playerId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SqlArenaPlayerStats) {
            SqlArenaPlayerStats other = (SqlArenaPlayerStats)obj;

            return other._playerId.equals(_playerId) &&
                    other._arenaStats.equals(_arenaStats);
        }
        return false;
    }

    SqlStat getStat(String name) {
        SqlStat stat = _stats.get(name);
        if (stat == null) {
            stat = new SqlStat(name, 0, 0, 0, 0);
            _stats.put(name, stat);
        }

        return stat;
    }

    private double getStatValue(SqlStat stat, StatTracking.StatTrackType trackingType) {
        switch (trackingType) {
            case TOTAL:
                return stat.total;
            case MAX:
                return stat.max;
            case MIN:
                return stat.min;
            default:
                throw new AssertionError("StatTrackType constant not recognized: " + trackingType.name());
        }
    }
}
