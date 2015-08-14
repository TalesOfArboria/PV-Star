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

import com.jcwhatever.nucleus.providers.sql.ISqlQueryResult;
import com.jcwhatever.nucleus.providers.sql.ISqlResult;
import com.jcwhatever.nucleus.providers.sql.ISqlTable;
import com.jcwhatever.nucleus.providers.sql.ISqlTableBuilder;
import com.jcwhatever.nucleus.providers.sql.SqlDbType;
import com.jcwhatever.nucleus.providers.sql.observer.SqlAutoCloseSubscriber;
import com.jcwhatever.nucleus.providers.sql.statement.ISqlTransaction;
import com.jcwhatever.nucleus.providers.sql.statement.generators.IOrderGenerator;
import com.jcwhatever.nucleus.providers.sql.statement.generators.SqlColumnOrder;
import com.jcwhatever.nucleus.providers.sql.SqlOrder;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.future.FutureResultAgent;
import com.jcwhatever.nucleus.utils.observer.future.IFutureResult;
import com.jcwhatever.pvs.api.stats.IPlayerStats;
import com.jcwhatever.pvs.api.stats.IStatsFilter;
import com.jcwhatever.pvs.api.stats.StatOrder;
import com.jcwhatever.pvs.stats.AbstractStatsFilter;
import com.jcwhatever.pvs.stats.PlayerStats;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sql based implementation of {@link IStatsFilter}.
 */
public class SqlStatFilter extends AbstractStatsFilter implements IStatsFilter {

    private final ISqlTable _table;

    /**
     * Constructor.
     *
     * @param table  The table that stores all statistics.
     */
    public SqlStatFilter(ISqlTable table) {
        PreCon.notNull(table);

        _table = table;
    }

    @Override
    public IFutureResult<List<IPlayerStats>> filter(int offset, final int limit) {
        PreCon.positiveNumber(offset);
        PreCon.greaterThanZero(limit);

        final FutureResultAgent<List<IPlayerStats>> agent = new FutureResultAgent<>();

        ISqlTransaction transaction = _table.getDatabase().createTransaction();
        final ISqlTable temp = createTempTable("temp", transaction);

        fillTempTable(temp, transaction);

        temp.selectRows(getTempColumns())
                .orderBy(new IOrderGenerator() {
                    @Override
                    public SqlColumnOrder[] getOrder(ISqlTable table) {

                        SqlColumnOrder[] results = new SqlColumnOrder[totalStats()];

                        for (int i = 0; i < results.length; i++) {
                            StatParam param = getStat(i);

                            SqlOrder order = param.statType.getOrder() == StatOrder.ASCENDING
                                    ? SqlOrder.ASCENDING
                                    : SqlOrder.DESCENDING;

                            results[i] = new SqlColumnOrder(temp, "order" + i, order);
                        }
                        return results;
                    }
                })
                .limit(offset, limit)
                .addToTransaction(transaction);

        // execute transaction
        transaction.execute().onResult(new SqlAutoCloseSubscriber() {
            @Override
            public void onResult(@Nullable ISqlResult result, @Nullable String message) {

                if (result == null) {
                    agent.error(null, message);
                    return;
                }

                ISqlQueryResult data = result.getFirstResult();
                assert data != null;

                Map<UUID, PlayerStats> map = new HashMap<UUID, PlayerStats>(limit + (int)(limit * 0.35));
                List<IPlayerStats> results = new ArrayList<IPlayerStats>(limit);

                try {
                    while (data.next()) {

                        UUID playerId = data.getUUID("playerId");

                        PlayerStats stats = map.get(playerId);
                        if (stats == null) {
                            stats = new PlayerStats(playerId);
                            map.put(playerId, stats);
                            results.add(stats);
                        }

                        for (int i=0; i < totalStats(); i++) {

                            StatParam param = getStat(i);

                            PlayerStats.Stat stat = stats.getStat(param.statType.getName());
                            double score = data.getDouble("order" + i);

                            switch (param.trackType) {
                                case TOTAL:
                                    stat.total(score);
                                    break;
                                case MAX:
                                    stat.max(score);
                                    break;
                                case MIN:
                                    stat.min(score);
                                    break;
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    agent.error();
                    return;
                }

                agent.success(results);
            }
        });

        return agent.getFuture();
    }

    // fill temporary table with data from the main table
    private void fillTempTable(ISqlTable temp, ISqlTransaction transaction) {

        List<UUID> arenaIds = new ArrayList<>(arenaIds());

        for (UUID arenaId : arenaIds) {

            for (int j = 0; j < totalStats(); j++) {

                StatParam param = getStat(j);
                String statName = param.statType.getName();
                String orderColumn = param.trackType.name().toLowerCase();

                switch (param.trackType) {
                    case TOTAL:
                        _table.insertInto(temp)
                                .columns("playerId", "order" + j)
                                .select("playerId", orderColumn)
                                .where(_table, "statName").isEqualTo(statName)
                                .and(_table, "arenaId").isEqualTo(arenaId)
                                .ifExists()
                                .set(temp, "order" + j).addColumn(_table, orderColumn)
                                .addToTransaction(transaction);
                        break;
                    case MAX:
                        _table.insertInto(temp)
                                .columns("playerId", "order" + j)
                                .select("playerId", orderColumn)
                                .where(_table, "statName").isEqualTo(statName)
                                .and(_table, "arenaId").isEqualTo(arenaId)
                                .and(_table, orderColumn).isGreaterThanColumn(temp, "order" + j)
                                .ifExists()
                                .set(temp, "order" + j).equalsColumn(_table, orderColumn)
                                .addToTransaction(transaction);
                        break;
                    case MIN:
                        _table.insertInto(temp)
                                .columns("playerId", "order" + j)
                                .select("playerId", orderColumn)

                                .where(_table, "statName").isEqualTo(statName)
                                .and(_table, "arenaId").isEqualTo(arenaId)
                                .and(_table, orderColumn).isLessThanColumn(temp, "order" + j)

                                .or(_table, "statName").isEqualTo(statName)
                                .and(_table, "arenaId").isEqualTo(arenaId)
                                .and(temp, "order" + j).isEqualTo(0.0D)

                                .ifExists()
                                .set(temp, "order" + j).equalsColumn(_table, orderColumn)
                                .addToTransaction(transaction);
                        break;

                }
            }
        }
    }

    // get the names of the columns in the temporary table
    private String[] getTempColumns() {
        String[] results = new String[totalStats() + 1];
        results[0] = "playerId";

        for (int i=1; i <= totalStats(); i++) {
            results[i] = "order" + (i - 1);
        }

        return results;
    }

    // create a new temporary table
    private ISqlTable createTempTable(String name, ISqlTransaction transaction) {

        ISqlTableBuilder.ISqlTableBuilderPrimaryKey constr =
                _table.getDatabase().createTableBuilder()
                        .usageTemporary()
                        .column("playerId", SqlDbType.UNIQUE_ID).primary();

        for (int i=0; i < totalStats(); i++) {
            constr.column("order" + i, SqlDbType.DOUBLE).defaultValue(0.0D);
        }

        return transaction.createTempTable(name, constr.define());
    }
}
