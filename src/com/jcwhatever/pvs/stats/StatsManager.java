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


package com.jcwhatever.pvs.stats;

import com.jcwhatever.nucleus.events.manager.EventMethod;
import com.jcwhatever.nucleus.events.manager.IEventListener;
import com.jcwhatever.nucleus.providers.sql.ISqlDatabase;
import com.jcwhatever.nucleus.providers.sql.Sql;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.future.FutureResultSubscriber;
import com.jcwhatever.nucleus.utils.observer.future.Result;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.events.ArenaDisposeEvent;
import com.jcwhatever.pvs.api.stats.IArenaStats;
import com.jcwhatever.pvs.api.stats.IStatsFilter;
import com.jcwhatever.pvs.api.stats.IStatsManager;
import com.jcwhatever.pvs.api.stats.StatType;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.stats.disk.DiskArenaStats;
import com.jcwhatever.pvs.stats.disk.DiskStatFilter;
import com.jcwhatever.pvs.stats.sql.SqlArenaStats;
import com.jcwhatever.pvs.stats.sql.SqlDataTables;
import com.jcwhatever.pvs.stats.sql.SqlStatFilter;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PV-Star implementation of {@link StatsManager}.
 */
public class StatsManager implements IStatsManager, IEventListener{

    private final Map<UUID, IArenaStats> _arenaStats = new HashMap<>(50);
    private final Map<String, StatType> _typeMap = new HashMap<>(25);

    private final String _address;
    private final String _databaseName;
    private final String _userName;
    private final String _password;
    private boolean _useDatabase;
    private SqlDataTables _tables;

    /**
     * Constructor.
     *
     * @param dataNode  The statistics data node settings.
     */
    public StatsManager(IDataNode dataNode) {
        PreCon.notNull(dataNode);

        _useDatabase = dataNode.getBoolean("use-database", false);
        _address = dataNode.getString("db-address", "localhost");
        _databaseName = dataNode.getString("db-name", "databaseName");
        _userName = dataNode.getString("db-user", "userName");
        _password = dataNode.getString("db-pass", "password");

        if (_useDatabase)
            loadDatabase();

        PVStarAPI.getEventManager().register(this);
    }

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    public void registerType(StatType type) {
        PreCon.notNull(type);

        _typeMap.put(type.getName().toLowerCase(), type);
    }

    @Override
    public List<StatType> getTypes() {
        return new ArrayList<>(_typeMap.values());
    }

    @Nullable
    @Override
    public StatType getType(String name) {
        PreCon.notNullOrEmpty(name);

        return _typeMap.get(name.toLowerCase());
    }

    @Override
    public IArenaStats getArenaStats(UUID arenaId) {

        IArenaStats stats = _arenaStats.get(arenaId);
        if (stats == null) {

            if (_useDatabase) {

                if (_tables == null)
                    throw new IllegalStateException("Not ready to use the database yet.");

                stats = new SqlArenaStats(arenaId, _tables.getTable());
            }
            else {
                stats = new DiskArenaStats(arenaId);
            }

            _arenaStats.put(arenaId, stats);
        }

        return stats;
    }

    @Override
    public IStatsFilter createFilter() {
        return _useDatabase
                ? new SqlStatFilter(_tables.getTable())
                : new DiskStatFilter(this);
    }

    @EventMethod
    private void onArenaDeleted(ArenaDisposeEvent event) {
        _arenaStats.remove(event.getArena().getId());
    }

    private void loadDatabase() {

        Msg.info("Connecting to statistics database ({0}) at {1}", _databaseName, _address);

        Sql.connect(_address, _databaseName, _userName, _password)
                .onResult(new FutureResultSubscriber<ISqlDatabase>() {
                    @Override
                    public void on(Result<ISqlDatabase> result) {
                        if (!result.hasResult())
                            _useDatabase = false;
                    }
                    @Override
                    public void onSuccess(Result<ISqlDatabase> result) {
                        ISqlDatabase database = result.getResult();
                        _tables = new SqlDataTables(database);
                        Msg.info("Connection success.");
                    }
                    @Override
                    public void onError(Result<ISqlDatabase> result) {
                        Msg.warning("Failed to connect to database.");
                        Msg.warning(result.getMessage());
                    }
                });
    }
}
