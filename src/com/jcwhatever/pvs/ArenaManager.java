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


package com.jcwhatever.pvs;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.providers.storage.DataStorage;
import com.jcwhatever.nucleus.storage.DataPath;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.ArenaRegion;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.managers.IArenaManager;
import com.jcwhatever.pvs.api.arena.options.NameMatchMode;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.arenas.Arena;
import com.jcwhatever.pvs.arenas.ArenaTypeInfo;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * PVStar implementation of {@link IArenaManager}.
 */
public class ArenaManager implements IArenaManager {

    private final Map<UUID, IArena> _arenaIdMap = new HashMap<>(20);
    private final Map<String, Class<? extends IArena>> _arenaTypes = new HashMap<>(20);
    private final IDataNode _dataNode;
    private final PVStar _pvStar;
    private final Map<String, IArena> _selectedArenas = new HashMap<>(20); // keyed to player name

    public ArenaManager(IDataNode dataNode) {
        _dataNode = dataNode;
        _pvStar = PVStar.getPlugin(PVStar.class);
    }

    public boolean registerType(Class<? extends IArena> arenaClass) {
        PreCon.notNull(arenaClass);

        ArenaTypeInfo typeInfo = arenaClass.getAnnotation(ArenaTypeInfo.class);
        if (typeInfo == null) {
            throw new IllegalArgumentException(
                    "Expected but did not find proper type info annotation on " +
                            "class: " + arenaClass.getName());
        }

        return _arenaTypes.put(typeInfo.typeName().toLowerCase(), arenaClass) != null;
    }

    public List<Class<? extends IArena>> getArenaTypes() {
        return new ArrayList<>(_arenaTypes.values());
    }

    @Override
    public IArena getSelectedArena(CommandSender sender) {
        PreCon.notNull(sender);

        return _selectedArenas.get(sender.getName());
    }

    @Override
    public void setSelectedArena(CommandSender sender, IArena arena) {
        PreCon.notNull(sender);
        PreCon.notNull(arena);

        if (sender instanceof Player) {
            Player p = (Player)sender;
            if (!p.isOp())
                return;

            _selectedArenas.put(p.getName(), arena);
        }
    }

    @Override
    public boolean setArenaName(UUID arenaId, String name) {
        PreCon.notNull(arenaId);
        PreCon.notNullOrEmpty(name);

        IArena arena = _arenaIdMap.get(arenaId);
        if (arena == null)
            return false;

        IDataNode arenaNode = _dataNode.getNode(arenaId.toString());
        arenaNode.set("name", name);
        arenaNode.save();

        arena.setName(name);
        return true;
    }


    @Override
    @Nullable
    public IArena getArena(UUID arenaId) {
        PreCon.notNull(arenaId);

        return _arenaIdMap.get(arenaId);
    }

    @Override
    @Nullable
    public IArena getArena(Player p) {
        PreCon.notNull(p);

        IArenaPlayer player = ArenaPlayer.get(p);
        if (player == null)
            return null;

        return player.getArena();
    }

    @Override
    @Nullable
    public IArena getArena(Location location) {

        List<ArenaRegion> regions = Nucleus.getRegionManager().getRegions(location, ArenaRegion.class);
        if (regions.isEmpty())
            return null;

        ArenaRegion region = regions.get(0);
        return region.getArena();
    }

    @Override
    public List<IArena> getArena(String arenaName, NameMatchMode matchMode) {
        PreCon.notNullOrEmpty(arenaName);
        PreCon.notNull(matchMode);

        switch (matchMode) {
            case CASE_SENSITIVE:
                // do nothing
                break;

            case CASE_INSENSITIVE:
                // fall through
            case BEGINS_WITH:
                // fall through
            case SEARCH:
                arenaName = arenaName.toLowerCase();
                break;
        }

        List<IArena> results = new ArrayList<>(5);

        for (IArena arena : _arenaIdMap.values()) {

            switch (matchMode) {
                case CASE_SENSITIVE:
                    if (!arena.getName().equals(arenaName))
                        continue;
                    break;
                case CASE_INSENSITIVE:
                    if (!arena.getSearchName().equals(arenaName))
                        continue;
                    break;
                case BEGINS_WITH:
                    if (!arena.getSearchName().startsWith(arenaName))
                        continue;
                    break;
                case SEARCH:
                    if (!arena.getSearchName().contains(arenaName))
                        continue;
                    break;
            }

            results.add(arena);
        }

        return results;
    }

    @Override
    public List<IArena> getArenas() {
        return new ArrayList<>(_arenaIdMap.values());
    }

    @Override
    public int getArenaCount() {
        return _arenaIdMap.size();
    }

    @Override
    @Nullable
    public IArena addArena(String arenaName, String typeName) {
        PreCon.notNullOrEmpty(arenaName);
        PreCon.notNullOrEmpty(typeName);

        typeName = typeName.toLowerCase();

        UUID arenaId = UUID.randomUUID();

        IDataNode arenaNode = _dataNode.getNode(arenaId.toString());
        arenaNode.set("name", arenaName);
        arenaNode.set("type", "arena");
        arenaNode.save();

        Arena arena = new Arena();
        _arenaIdMap.put(arenaId, arena);

        arena.init(arenaId, arenaName);
        arena.getSettings().setTypeDisplayName(typeName);

        _pvStar.getPointsManager().loadTypes(arena);

        return arena;
    }

    @Override
    public boolean removeArena(UUID arenaId) {
        PreCon.notNull(arenaId);

        Arena arena = (Arena)_arenaIdMap.remove(arenaId);
        if (arena == null)
            return false;

        _arenaIdMap.remove(arena.getId());

        arena.dispose();

        DataStorage.remove(PVStarAPI.getPlugin(), new DataPath("arenas." + arena.getId().toString()));

        File arenaFolder = new File(PVStarAPI.getPlugin().getDataFolder(), "arenas");
        File dataFolder = new File(arenaFolder, arena.getId().toString());

        if (dataFolder.exists() && !dataFolder.delete()) {
            Msg.warning("Failed to delete arena folder: {0}", dataFolder.getAbsolutePath());
        }

        IDataNode arenaNode = _dataNode.getNode(arenaId.toString());
        arenaNode.remove();
        arenaNode.save();

        return true;
    }

    void loadArenas() {

        for (IDataNode arenaNode : _dataNode) {

            UUID arenaId = TextUtils.parseUUID(arenaNode.getName());
            if (arenaId == null) {
                Msg.warning("Invalid arena id found in config file: {0}", arenaNode.getName());
                continue;
            }

            String arenaName = arenaNode.getString("name");
            String typeName = arenaNode.getString("type");

            if (arenaName == null || arenaName.isEmpty()) {
                Msg.warning("An arena entry in the config did not have an arena name. Skipping loading of arena.");
                continue;
            }

            if (typeName == null || typeName.isEmpty()) {
                Msg.warning("An arena entry in the config did not have a type name. Skipping loading of arena.");
                continue;
            }

            Arena arena = new Arena();
            arena.init(arenaId, arenaName);

            _pvStar.getPointsManager().loadTypes(arena);

            _arenaIdMap.put(arenaId, arena);
        }
    }
}
