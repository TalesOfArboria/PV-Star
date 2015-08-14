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

package com.jcwhatever.pvs.stats;

import com.jcwhatever.nucleus.mixins.INamed;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.stats.IPlayerStats;
import com.jcwhatever.pvs.api.stats.StatTracking;
import com.jcwhatever.pvs.api.stats.StatType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generic implementation of {@link IPlayerStats}.
 */
public class PlayerStats implements IPlayerStats {

    private final UUID _playerId;
    private final Map<String, Stat> _stats = new HashMap<>(15);

    /**
     * Constructor.
     *
     * @param playerId  The ID of the player the stats belong to.
     */
    public PlayerStats(UUID playerId) {
        PreCon.notNull(playerId);

        _playerId = playerId;
    }

    @Override
    public UUID getPlayerId() {
        return _playerId;
    }

    @Override
    public double getScore(StatType type, StatTracking.StatTrackType trackingType) {

        Stat stat = _stats.get(type.getName());
        if (stat == null)
            return 0;

        return getStatValue(stat, trackingType);
    }

    @Override
    public void addScore(StatType type, double amount) {

        Stat stat = _stats.get(type.getName());
        if (stat == null) {
            stat = new Stat(type.getName(), 0, 0, 0, 0);
            _stats.put(type.getName(), stat);
        }

        stat.records += 1;
        stat.total += amount;
        stat.max = Math.max(stat.max, amount);
        stat.min = Math.min(stat.min, amount);
    }

    @Override
    public int hashCode() {
        return _playerId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayerStats) {
            PlayerStats other = (PlayerStats)obj;

            return other._playerId.equals(_playerId);
        }
        return false;
    }

    /**
     * Get statistic data by name.
     *
     * <p>If a record does not exist, a new one is created.</p>
     *
     * @param name  The name of the statistic. Case sensitive.
     */
    public Stat getStat(String name) {
        Stat stat = _stats.get(name);
        if (stat == null) {
            stat = new Stat(name, 0, 0, 0, 0);
            _stats.put(name, stat);
        }

        return stat;
    }

    private double getStatValue(Stat stat, StatTracking.StatTrackType trackingType) {
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

    /**
     * Stores data about a statistic.
     */
    public static class Stat implements INamed {
        final String name;
        int records;
        double total;
        double max;
        double min;

        /**
         * Constructor.
         *
         * @param name     The name of the statistic.
         * @param records  The number of records added.
         * @param total    The total of all records added.
         * @param max      The largest record added.
         * @param min      The smallest record added.
         */
        Stat(String name, int records, double total, double max, double min) {
            this.name = name;
            this.records = records;
            this.total = total;
            this.max = max;
            this.min = min;
        }

        @Override
        public String getName() {
            return name;
        }

        public int records() {
            return records;
        }

        public void records(int records) {
            this.records = records;
        }

        public double total() {
            return total;
        }

        public void total(double total) {
            this.total = total;
        }

        public double max() {
            return max;
        }

        public void max(double max) {
            this.max = max;
        }

        public double min() {
            return min;
        }

        public void min(double min) {
            this.min = min;
        }
    }
}
