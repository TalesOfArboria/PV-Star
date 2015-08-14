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


package com.jcwhatever.pvs.stats.disk;

import com.google.common.collect.MapMaker;
import com.jcwhatever.nucleus.collections.players.PlayerMap;
import com.jcwhatever.nucleus.providers.storage.DataStorage;
import com.jcwhatever.nucleus.storage.DataPath;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.future.FutureResultAgent;
import com.jcwhatever.nucleus.utils.observer.future.IFutureResult;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.stats.IArenaStats;
import com.jcwhatever.pvs.api.stats.IPlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PVStar implementation of {@link IArenaStats}.
 */
public class DiskArenaStats implements IArenaStats {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<UUID, DiskPlayerStats> _onlineCache = new PlayerMap<>(
            PVStarAPI.getPlugin(), Bukkit.getMaxPlayers());

    private final Map<UUID, DiskPlayerStats> _weakCache =
            new MapMaker().weakValues().concurrencyLevel(1).initialCapacity(25).makeMap();

    private final UUID _arenaId;
    private final IDataNode _dataNode;

    /**
     * Constructor.
     *
     * @param arenaId  The ID of the arena the stats are for.
     */
    public DiskArenaStats(UUID arenaId) {
        PreCon.notNull(arenaId);

        _arenaId = arenaId;

        _dataNode = DataStorage.get(PVStarAPI.getPlugin(),
                new DataPath("stats." + _arenaId));
        _dataNode.load();
    }

    @Override
    public UUID getArenaId() {
        return _arenaId;
    }

    @Override
    public IFutureResult<IPlayerStats> get(UUID playerId) {
        PreCon.notNull(playerId);

        IPlayerStats stats = getLocal(playerId);

        return FutureResultAgent.successResult(stats);
    }

    List<IPlayerStats> getPlayerStats() {

        List<IPlayerStats> results = new ArrayList<>(_dataNode.size());

        for (IDataNode playerNode : _dataNode) {

            UUID playerId = TextUtils.parseUUID(playerNode.getName());
            if (playerId == null)
                continue;

            results.add(getLocal(playerId));
        }

        return results;
    }

    private IDataNode getNode(UUID playerId) {
        return _dataNode.getNode(playerId.toString());
    }

    private DiskPlayerStats getLocal(UUID playerId) {

        DiskPlayerStats stats = _weakCache.get(playerId);
        if (stats == null) {
            stats = new DiskPlayerStats(playerId, getNode(playerId));

            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                _onlineCache.put(playerId, stats);
            }
            _weakCache.put(playerId, stats);
        }

        return stats;
    }
}
