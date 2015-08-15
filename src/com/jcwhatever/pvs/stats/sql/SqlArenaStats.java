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

import com.google.common.collect.MapMaker;
import com.jcwhatever.nucleus.collections.players.PlayerMap;
import com.jcwhatever.nucleus.providers.sql.ISqlQueryResult;
import com.jcwhatever.nucleus.providers.sql.ISqlResult;
import com.jcwhatever.nucleus.providers.sql.ISqlTable;
import com.jcwhatever.nucleus.providers.sql.observer.SqlAutoCloseSubscriber;
import com.jcwhatever.nucleus.providers.sql.statement.ISqlTransaction;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.future.FutureResultAgent;
import com.jcwhatever.nucleus.utils.observer.future.IFutureResult;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.stats.IArenaStats;
import com.jcwhatever.pvs.api.stats.IPlayerStats;
import com.jcwhatever.pvs.api.stats.StatType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Sql based implementation of {@link IArenaStats}.
 */
public class SqlArenaStats implements IArenaStats {

    private final UUID _arenaId;
    private final ISqlTable _table;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<UUID, SqlArenaPlayerStats> _onlineCache = new PlayerMap<SqlArenaPlayerStats>(
            PVStarAPI.getPlugin(), Bukkit.getMaxPlayers());

    private final Map<UUID, SqlArenaPlayerStats> _weakCache =
            new MapMaker().weakValues().concurrencyLevel(1).initialCapacity(25).makeMap();

    /**
     * Constructor.
     *
     * @param arenaId  The ID of the arena.
     * @param table    The table that stores all statistics.
     */
    public SqlArenaStats(UUID arenaId, ISqlTable table) {
        PreCon.notNull(arenaId);
        PreCon.notNull(table);

        _arenaId = arenaId;
        _table = table;
    }

    @Override
    public UUID getArenaId() {
        return _arenaId;
    }

    @Override
    public IFutureResult<IPlayerStats> get(final UUID playerId) {
        PreCon.notNull(playerId);

        final FutureResultAgent<IPlayerStats> agent = new FutureResultAgent<>();

        SqlArenaPlayerStats stats = _weakCache.get(playerId);
        if (stats != null) {
            return agent.success(stats);
        }

        ISqlTransaction transaction = _table.getDatabase().createTransaction();

        getDbValues(playerId, transaction);

        transaction.execute()
                .onSuccess(new SqlAutoCloseSubscriber() {
                    @Override
                    public void onResult(@Nullable ISqlResult result, @Nullable String message) {

                        assert result != null;

                        SqlArenaPlayerStats stats = _weakCache.get(playerId);
                        if (stats != null) {
                            agent.success(stats);
                            return;
                        }

                        ISqlQueryResult data = result.getFirstResult();
                        assert data != null;

                        stats = new SqlArenaPlayerStats(SqlArenaStats.this, playerId);

                        try {

                            while (data.next()) {
                                String statName = data.getString("statName");
                                SqlStat stat = stats.getStat(statName);

                                stat.records = data.getInt("records");
                                stat.total = data.getDouble("total");
                                stat.max = data.getDouble("max");
                                stat.min = data.getDouble("min");
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                            agent.error(null, "Failed to parse data from database results.");
                            return;
                        }

                        insertIntoCache(stats, false);
                        agent.success(stats);
                    }
                })
                .onError(new SqlAutoCloseSubscriber() {
                    @Override
                    public void onResult(@Nullable ISqlResult result, @Nullable String message) {
                        agent.error(null, message);
                    }
                });

        return agent.getFuture();
    }

    @Override
    public void addScore(UUID playerId, StatType type, double amount) {
        PreCon.notNull(playerId);
        PreCon.notNull(type);

        ISqlTransaction transaction = _table.getDatabase().createTransaction();

        _table.insertRow("arenaId", "playerId", "statName", "records", "total", "max", "min")
                .values(_arenaId, playerId, type.getName(), 1, amount, amount, amount)
                .ifExists()
                .set("records").add(1)
                .set("total").add(amount)
                .addToTransaction(transaction);

        _table.updateRow()
                .set("max").value(amount)
                .where("arenaId").isEqualTo(_arenaId)
                .and("playerId").isEqualTo(playerId)
                .and("statName").isEqualTo(type.getName())
                .and("max").isLessThan(amount)
                .addToTransaction(transaction);

        _table.updateRow()
                .set("min").value(amount)
                .where("arenaId").isEqualTo(_arenaId)
                .and("playerId").isEqualTo(playerId)
                .and("statName").isEqualTo(type.getName())
                .and("min").isGreaterThan(amount)
                .addToTransaction(transaction);

        getDbValues(playerId, transaction);

        transaction.execute();
    }

    @Override
    public int hashCode() {
        return _arenaId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SqlArenaStats) {
            SqlArenaStats other = (SqlArenaStats)obj;
            return other._arenaId.equals(_arenaId);
        }
        return false;
    }

    private void getDbValues(final UUID playerId, ISqlTransaction transaction) {

        _table.selectRows("statName", "records", "total", "max", "min")
                .where("arenaId").isEqualTo(_arenaId)
                .and("playerId").isEqualTo(playerId)
                .addToTransaction(transaction)
                .onSuccess(new SqlAutoCloseSubscriber() {
                    @Override
                    public void onResult(@Nullable ISqlResult result, @Nullable String message) {

                        assert result != null;

                        ISqlQueryResult data = result.getFirstResult();
                        if (data == null)
                            return;

                        Player player = Bukkit.getPlayer(playerId);
                        if (player == null || !player.isOnline())
                            return;

                        SqlArenaPlayerStats stats = _weakCache.get(playerId);
                        if (stats == null) {
                            stats = new SqlArenaPlayerStats(SqlArenaStats.this, playerId);
                            _onlineCache.put(playerId, stats);
                            _weakCache.put(playerId, stats);
                        }

                        try {

                            while (data.next()) {
                                String statName = data.getString("statName");

                                SqlStat stat = stats.getStat(statName);
                                stat.records = data.getInt("records");
                                stat.total = data.getDouble("total");
                                stat.max = data.getDouble("max");
                                stat.min = data.getDouble("min");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Nullable
    SqlArenaPlayerStats getCached(UUID playerId) {
        return _weakCache.get(playerId);
    }

    void cache(SqlArenaPlayerStats stats) {
        insertIntoCache(stats, true);
    }

    private void insertIntoCache(SqlArenaPlayerStats stats, boolean insertOnline) {

        if (insertOnline) {
            Player player = Bukkit.getPlayer(stats.getPlayerId());
            if (player != null && player.isOnline()) {
                _onlineCache.put(stats.getPlayerId(), stats);
            }
        }

        _weakCache.put(stats.getPlayerId(), stats);
    }
}
