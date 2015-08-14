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

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.stats.IStatsFilter;
import com.jcwhatever.pvs.api.stats.StatTracking;
import com.jcwhatever.pvs.api.stats.StatType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract implementation of {@link IStatsFilter}.
 */
public abstract class AbstractStatsFilter implements IStatsFilter {

    private final Set<UUID> _arenaIds = new HashSet<>(7);
    private final List<StatParam> _stats = new ArrayList<>(5);

    @Override
    public void addArena(UUID arenaId) {
        PreCon.notNull(arenaId);

        _arenaIds.add(arenaId);
    }

    @Override
    public void addStat(StatType type, StatTracking.StatTrackType trackType) {
        PreCon.notNull(type);

        PreCon.notNull(trackType);

        _stats.add(new StatParam(type, trackType));
    }

    /**
     * Get the IDs of arenas to include in the filter results.
     */
    protected Set<UUID> arenaIds() {
        return _arenaIds;
    }

    /**
     * Get the statistics to include in the filter results.
     *
     * <p>Ordered by priority.</p>
     */
    protected List<StatParam> stats() {
        return _stats;
    }

    /**
     * Get the total number of statistics.
     */
    protected int totalStats() {
        return _stats.size();
    }

    /**
     * Get a statistic by index position/priority.
     *
     * @param index  The index position.
     */
    protected StatParam getStat(int index) {
        return _stats.get(index);
    }

    protected static class StatParam {
        public StatType statType;
        public StatTracking.StatTrackType trackType;

        public StatParam(StatType statType, StatTracking.StatTrackType trackType) {
            this.statType = statType;
            this.trackType = trackType;
        }
    }
}

