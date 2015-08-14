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

import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.stats.IPlayerStats;
import com.jcwhatever.pvs.api.stats.StatTracking;
import com.jcwhatever.pvs.api.stats.StatType;

import java.util.UUID;

/**
 * Disk based implementation of {@link IPlayerStats}.
 */
public class DiskPlayerStats implements IPlayerStats {

    private final UUID _playerId;
    private final IDataNode _dataNode;

    /**
     * Constructor.
     *
     * @param playerId  The ID of the player.
     * @param dataNode  The players statistics data node.
     */
    DiskPlayerStats(UUID playerId, IDataNode dataNode) {
        _playerId = playerId;
        _dataNode = dataNode;
    }

    @Override
    public UUID getPlayerId() {
        return _playerId;
    }

    @Override
    public double getScore(StatType type, StatTracking.StatTrackType trackingType) {

        IDataNode node = getNode(type);

        switch (trackingType) {
            case TOTAL:
                return node.getDouble("total", 0.0D);
            case MIN:
                return node.getDouble("min", 0.0D);
            case MAX:
                return node.getDouble("max", 0.0D);
            default:
                return 0.0D;
        }
    }

    @Override
    public void addScore(StatType type, double amount) {
        PreCon.notNull(type);

        double current = getScore(type, StatTracking.StatTrackType.TOTAL);
        current += amount;

        IDataNode node = getNode(type);
        node.set("total", current);

        // add min score
        if (type.getTracking().hasType(StatTracking.StatTrackType.MIN)) {
            double min = Math.min(amount, node.getDouble("min"));
            node.set("min", min);
        }

        // add max score
        if (type.getTracking().hasType(StatTracking.StatTrackType.MAX)) {
            double max = Math.max(amount, node.getDouble("max"));
            node.set("max", max);
        }

        node.save();
    }

    private IDataNode getNode(StatType type) {
        return _dataNode.getNode(type.getName());
    }
}
