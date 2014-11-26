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


package com.jcwhatever.bukkit.pvs;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.regions.ReadOnlyRegion;
import com.jcwhatever.bukkit.generic.storage.DataStorage;
import com.jcwhatever.bukkit.generic.storage.DataStorage.DataPath;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.Utils;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaRegion;
import com.jcwhatever.bukkit.pvs.api.arena.managers.ArenaManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.NameMatchMode;
import com.jcwhatever.bukkit.pvs.api.exceptions.MissingTypeInfoException;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.arenas.ArenaTypeInfo;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Arena Manager implementation.
 */
public class PVArenaManager implements ArenaManager {

    private final Map<UUID, Arena> _arenaIdMap = new HashMap<>(20);
    private final Map<String, Class<? extends Arena>> _arenaTypes = new HashMap<>(20);
    private final IDataNode _dataNode;
    private final PVStar _pvStar;
    private Map<String, Arena> _selectedArenas = new HashMap<>(20); // keyed to player name

    public PVArenaManager(IDataNode dataNode) {
        _dataNode = dataNode;
        _pvStar = PVStar.getPlugin(PVStar.class);
    }

    public boolean registerType(Class<? extends Arena> arenaClass) {
        PreCon.notNull(arenaClass);

        ArenaTypeInfo typeInfo = arenaClass.getAnnotation(ArenaTypeInfo.class);
        if (typeInfo == null)
            throw new MissingTypeInfoException(arenaClass);

        return _arenaTypes.put(typeInfo.typeName().toLowerCase(), arenaClass) != null;
    }

    public List<Class<? extends Arena>> getArenaTypes() {
        return new ArrayList<>(_arenaTypes.values());
    }

    @Override
    public Arena getSelectedArena(CommandSender sender) {
        PreCon.notNull(sender);

        return _selectedArenas.get(sender.getName());
    }

    @Override
    public void setSelectedArena(CommandSender sender, Arena arena) {
        PreCon.notNull(sender);
        PreCon.notNull(arena);

        if (sender instanceof Player) {
            Player p = (Player)sender;
            if (!p.isOp())
                return;

            _selectedArenas.put(p.getName(), arena);
        }
    }

    /**
     * Change an arenas name.
     *
     * @param arenaId  The id of the arena.
     * @param name     The new arena name.
     */
    @Override
    public boolean setArenaName(UUID arenaId, String name) {
        PreCon.notNull(arenaId);
        PreCon.notNullOrEmpty(name);

        Arena arena = _arenaIdMap.get(arenaId);
        if (arena == null)
            return false;

        IDataNode arenaNode = _dataNode.getNode(arenaId.toString());
        arenaNode.set("name", name);
        arenaNode.saveAsync(null);

        arena.setName(name);
        return true;
    }


    @Override
    @Nullable
    public Arena getArena(UUID arenaId) {
        PreCon.notNull(arenaId);

        return _arenaIdMap.get(arenaId);
    }

    @Override
    @Nullable
    public Arena getArena(Player p) {
        PreCon.notNull(p);

        ArenaPlayer player = PVArenaPlayer.get(p);
        if (player == null)
            return null;

        return player.getArena();
    }

    @Override
    @Nullable
    public Arena getArena(Location location) {

        List<ReadOnlyRegion> regions = GenericsLib.getRegionManager().getRegions(location);
        if (regions.isEmpty())
            return null;

        for (ReadOnlyRegion readOnlyRegion : regions) {

            ArenaRegion region = readOnlyRegion.getMeta(ArenaRegion.class.getName());
            if (region == null)
                continue;

            return region.getArena();
        }

        return null;
    }

    @Override
    public List<Arena> getArena(String arenaName, NameMatchMode matchMode) {
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

        List<Arena> results = new ArrayList<>(5);

        for (Arena arena : _arenaIdMap.values()) {

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
    public List<Arena> getArenas() {
        return new ArrayList<>(_arenaIdMap.values());
    }

    @Override
    public int getArenaCount() {
        return _arenaIdMap.size();
    }

    @Override
    @Nullable
    public Arena addArena(String arenaName, String typeName) {
        PreCon.notNullOrEmpty(arenaName);
        PreCon.notNullOrEmpty(typeName);

        typeName = typeName.toLowerCase();

        Arena arena = loadArena("arena");
        if (arena == null)
            return null;

        UUID arenaId = UUID.randomUUID();

        IDataNode arenaNode = _dataNode.getNode(arenaId.toString());
        arenaNode.set("name", arenaName);
        arenaNode.set("type", "arena");
        arenaNode.saveAsync(null);

        _arenaIdMap.put(arenaId, arena);

        arena.init(arenaId, arenaName);
        arena.getSettings().setTypeDisplayName(typeName);

        _pvStar.getPointsManager().loadTypes(arena);

        return arena;
    }

    @Override
    public boolean removeArena(UUID arenaId) {
        PreCon.notNull(arenaId);

        Arena arena = _arenaIdMap.remove(arenaId);
        if (arena == null)
            return false;

        _arenaIdMap.remove(arena.getId());

        arena.dispose();

        DataStorage.removeStorage(PVStarAPI.getPlugin(), new DataPath("arenas." + arena.getId().toString()));

        File arenaFolder = new File(PVStarAPI.getPlugin().getDataFolder(), "arenas");
        File dataFolder = new File(arenaFolder, arena.getId().toString());

        if (dataFolder.exists() && !dataFolder.delete()) {
            Msg.warning("Failed to delete arena folder: {0}", dataFolder.getAbsolutePath());
        }

        IDataNode arenaNode = _dataNode.getNode(arenaId.toString());
        arenaNode.remove();
        arenaNode.saveAsync(null);

        return true;
    }

    void loadArenas() {

        Set<String> rawArenaIds = _dataNode.getSubNodeNames();

        if (rawArenaIds == null || rawArenaIds.isEmpty())
            return;

        for (String rawId : rawArenaIds) {

            UUID arenaId = Utils.getId(rawId);
            if (arenaId == null) {
                Msg.warning("Invalid arena id found in config file: {0}", rawId);
                continue;
            }

            IDataNode arenaNode = _dataNode.getNode(rawId);

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

            Arena arena = loadArena(typeName);
            if (arena == null) {
                Msg.warning("Failed to load arena named '{0}' of type '{1}'.", arenaName, typeName);
                continue;
            }

            arena.init(arenaId, arenaName);

            _pvStar.getPointsManager().loadTypes(arena);

            _arenaIdMap.put(arenaId, arena);
        }
    }


    @Nullable
    private Arena loadArena(String typeName) {

        Class<? extends Arena> arenaClass = _arenaTypes.get(typeName.toLowerCase());
        if (arenaClass == null)
            return null;

        Arena arena;

        try {
            arena = arenaClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        return arena;
    }
}
