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

package com.jcwhatever.pvs.stats.disk;

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.future.FutureResultAgent;
import com.jcwhatever.nucleus.utils.observer.future.IFutureResult;
import com.jcwhatever.pvs.api.stats.IPlayerStats;
import com.jcwhatever.pvs.api.stats.IStatsFilter;
import com.jcwhatever.pvs.api.stats.StatOrder;
import com.jcwhatever.pvs.api.stats.StatTracking;
import com.jcwhatever.pvs.stats.AbstractStatsFilter;
import com.jcwhatever.pvs.stats.PlayerStats;
import com.jcwhatever.pvs.stats.StatsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Disk based implementation of {@link IStatsFilter}.
 */
public class DiskStatFilter extends AbstractStatsFilter implements IStatsFilter {

    private final StatsManager _manager;

    /**
     * Constructor.
     *
     * @param manager  The statistics manager.
     */
    public DiskStatFilter(StatsManager manager) {
        PreCon.notNull(manager);

        _manager = manager;
    }

    @Override
    public IFutureResult<List<IPlayerStats>> filter(int offset, int limit) {

        Map<UUID, PlayerStats> map = new HashMap<>(1024);
        List<IPlayerStats> buffer = new ArrayList<>(1024);

        for (UUID arenaId : arenaIds()) {

            DiskArenaStats arenaStats = (DiskArenaStats)_manager.getArenaStats(arenaId);

            List<IPlayerStats> arenaPlayers = arenaStats.getPlayerStats();
            for (IPlayerStats playerStats : arenaPlayers) {

                PlayerStats stats = map.get(playerStats.getPlayerId());
                if (stats == null) {
                    stats = new PlayerStats(playerStats.getPlayerId());
                    map.put(playerStats.getPlayerId(), stats);
                    buffer.add(stats);
                }

                for (StatParam param : stats()) {
                    PlayerStats.Stat stat = stats.getStat(param.statType.getName());
                    stat.total(playerStats.getScore(param.statType, StatTracking.StatTrackType.TOTAL));
                    stat.max(playerStats.getScore(param.statType, StatTracking.StatTrackType.MAX));
                    stat.min(playerStats.getScore(param.statType, StatTracking.StatTrackType.MIN));
                }
            }
        }

        Collections.sort(buffer, new Comparator<IPlayerStats>() {
            @Override
            public int compare(IPlayerStats o1, IPlayerStats o2) {

                for (StatParam param : stats()) {

                    double score1 = o1.getScore(param.statType, param.trackType);
                    double score2 = o2.getScore(param.statType, param.trackType);

                    if (score1 == score2)
                        continue;

                    return param.statType.getOrder() == StatOrder.ASCENDING
                            ? Double.compare(score1, score2)
                            : Double.compare(score2, score1);
                }

                return 0;
            }
        });

        List<IPlayerStats> results = new ArrayList<>(limit);
        if (offset > buffer.size())
            return FutureResultAgent.successResult(results);

        for (int i=offset; i < offset + limit && i < buffer.size(); i++) {
            results.add(buffer.get(i));
        }

        return FutureResultAgent.successResult(results);
    }
}
