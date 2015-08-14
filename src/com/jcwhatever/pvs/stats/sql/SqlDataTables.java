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

import com.jcwhatever.nucleus.events.manager.EventMethod;
import com.jcwhatever.nucleus.events.manager.IEventListener;
import com.jcwhatever.nucleus.providers.sql.ISqlDatabase;
import com.jcwhatever.nucleus.providers.sql.ISqlResult;
import com.jcwhatever.nucleus.providers.sql.ISqlTable;
import com.jcwhatever.nucleus.providers.sql.ISqlTableDefinition;
import com.jcwhatever.nucleus.providers.sql.SqlDbType;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.future.FutureResultSubscriber;
import com.jcwhatever.nucleus.utils.observer.future.Result;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.events.ArenaDisposeEvent;
import com.jcwhatever.pvs.api.utils.Msg;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

/**
 * Manages Sql statistics data table.
 */
public class SqlDataTables implements IEventListener {

    private ISqlTable _dataTable;

    /**
     * Constructor.
     *
     * @param database  The database the table is in or will be created in.
     */
    public SqlDataTables(ISqlDatabase database) {
        PreCon.notNull(database);

        ISqlTableDefinition definition = database.createTableBuilder()
                .usageReadInsert()
                .transactional()
                .column("arenaId", SqlDbType.UNIQUE_ID).primary()
                .column("playerId", SqlDbType.UNIQUE_ID).primary()
                .column("statName", SqlDbType.getString(45)).primary()
                .column("records", SqlDbType.INTEGER_UNSIGNED)
                .column("total", SqlDbType.DOUBLE)
                .column("max", SqlDbType.DOUBLE)
                .column("min", SqlDbType.DOUBLE)
                .define();

        database.createTable("pvArenaStats", definition)
                .onResult(new FutureResultSubscriber<ISqlTable>() {
                    @Override
                    public void on(Result<ISqlTable> result) {
                        // do nothing
                    }
                    @Override
                    public void onSuccess(Result<ISqlTable> result) {
                        _dataTable = result.getResult();
                    }
                    @Override
                    public void onError(Result<ISqlTable> result) {
                        Msg.warning("Failed to load arena statistics data table.");
                        Msg.warning(result.getMessage());
                    }
                });
    }

    @Nullable
    public ISqlTable getTable() {

        if (_dataTable == null)
            throw new IllegalStateException("Not ready to use the database yet.");

        return _dataTable;
    }

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @EventMethod
    private void onArenaDeleted(final ArenaDisposeEvent event) {

        final IArena arena = event.getArena();

        if (_dataTable == null) {
            Msg.warning("Failed to delete statistics data for arena '{0}' ({1}) from database because " +
                    "the table is not loaded yet.", arena.getName(), arena.getId());
        }

        _dataTable
                .deleteRows().where("arenaId").isEqualTo(arena.getId())
                .execute()
                .onSuccess(new FutureResultSubscriber<ISqlResult>() {
                    @Override
                    public void on(Result<ISqlResult> result) {
                        Msg.info("Deleted statistics data for arena '{0}' ({1}) from database.",
                                arena.getName(), arena.getId());
                    }
                })
                .onError(new FutureResultSubscriber<ISqlResult>() {
                    @Override
                    public void on(Result<ISqlResult> result) {
                        Msg.info("Error while deleting statistics data for arena '{0}' ({1}) from database.",
                                arena.getName(), arena.getId());
                        Msg.info(result.getMessage());
                    }
                });
    }
}
