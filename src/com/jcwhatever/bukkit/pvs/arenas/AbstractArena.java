/* This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.pvs.arenas;

import com.jcwhatever.bukkit.generic.collections.WeakValueMap;
import com.jcwhatever.bukkit.generic.events.GenericsEventManager;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.permissions.IPermission;
import com.jcwhatever.bukkit.generic.permissions.Permissions;
import com.jcwhatever.bukkit.generic.storage.DataStorage;
import com.jcwhatever.bukkit.generic.storage.DataStorage.DataPath;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.ArenaProvider;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.modules.PVStarModule;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaRegion;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtensionManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.GameManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.LobbyManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.PlayerManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.SpawnManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.SpectatorManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.TeamManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.settings.ArenaSettings;
import com.jcwhatever.bukkit.pvs.api.events.ArenaDisposeEvent;
import com.jcwhatever.bukkit.pvs.api.scripting.ArenaScriptManager;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract arena implementation.
 */
public abstract class AbstractArena implements Arena {

    @Localizable static final String _JOIN_LEAVE_CURRENT_FIRST = "You must leave the current arena before you can join another.";
    @Localizable static final String _ARENA_BUSY = "Arena '{0}' is busy at the moment. Try again later.";
    @Localizable static final String _ARENA_RUNNING = "This arena is already in progress.";
    @Localizable static final String _ARENA_DISABLED = "Specified arena is not enabled.";
    @Localizable static final String _JOIN_LIMIT_REACHED = "No spots left. The arena is full.";
    @Localizable static final String _JOIN_NO_PERMISSION = "You don't have permission to join arena '{0}'.";

    private String _name;
    private String _searchName;
    private UUID _id;

    private boolean _isInitialized;
    private int _isBusy = 0;

    private ArenaTypeInfo _typeInfo;
    private IDataNode _dataNode;
    private File _dataFolder;

    private ArenaProvider _arenaProvider;
    private GenericsEventManager _eventManager;
    private ArenaRegion _region;
    private IPermission _permission;

    private Map<String, IDataNode> _nodeMap = new WeakValueMap<>(20);

    /*
     * Initialize an arenas typeInfo after it is instantiated.
     */
    @Override
    public final void init(UUID id, String name) {
        PreCon.notNull(id);
        PreCon.notNullOrEmpty(name);

        if (_isInitialized)
            throw new RuntimeException("An arena can only be initialized once.");

        _isInitialized = true;

        _id = id;
        _name = name;
        _searchName = name.toLowerCase();
        _typeInfo = getClass().getAnnotation(ArenaTypeInfo.class);

        _arenaProvider = getArenaProvider();

        _eventManager = new GenericsEventManager(PVStarAPI.getEventManager());

        _dataNode = DataStorage.getStorage(PVStarAPI.getPlugin(), new DataPath("arenas." + id.toString()));
        _dataNode.load();

        _region = new ArenaRegion(this, _dataNode.getNode("region"));

        // get data folder
        File arenaFolder = new File(PVStarAPI.getPlugin().getDataFolder(), "arenas");
        _dataFolder = new File(arenaFolder, id.toString());

        if (!_dataFolder.exists() && !_dataFolder.mkdirs()) {
            Msg.severe("Failed to create data folder '{0}' for arena '{1}'.", _dataFolder.getAbsolutePath(), name);
        }

        _permission = Permissions.register("pvstar.arena." + _name, PermissionDefault.TRUE);

        onInit();

        // ensure game manager is loaded
        getGameManager();

        // ensure lobby manager is loaded
        getLobbyManager();

        // ensure spectator manager is loaded
        getSpectatorManager();

        // ensure spawn manager is loaded
        getSpawnManager();

        // ensure team manager is loaded
        getTeamManager();

        // ensure extension manager is loaded
        getExtensionManager();

        // ensure script manager is loaded
        getScriptManager();

        // ensure settings are loaded
        getSettings();
    }

    /*
     * Get the arenas event manager.
     */
    @Override
    public final GenericsEventManager getEventManager() {
        return _eventManager;
    }

    /*
     * Get the arenas lobby manager.
     */
    @Override
    public final LobbyManager getLobbyManager() {
        return _arenaProvider.getLobbyManager();
    }

    /*
     * Get the arenas game manager.
     */
    @Override
    public final GameManager getGameManager() {
        return _arenaProvider.getGameManager();
    }

    /*
     * Get the arenas spectator manager.
     */
    @Override
    public final SpectatorManager getSpectatorManager() {
        return _arenaProvider.getSpectatorManager();
    }

    /*
     * Get the arenas team manager.
     */
    @Override
    public final TeamManager getTeamManager() {
        return _arenaProvider.getTeamManager();
    }

    /*
     * Get the arenas spawn point manager.
     */
    @Override
    public final SpawnManager getSpawnManager() {
        return _arenaProvider.getSpawnManager();
    }

    /*
     * Get the arenas extension manager.
     */
    @Override
    public final ArenaExtensionManager getExtensionManager() {
        return _arenaProvider.getExtensionManager();
    }

    /*
     * Get the arenas script manager.
     */
    @Override
    public final ArenaScriptManager getScriptManager() {
        return _arenaProvider.getScriptManager();
    }

    /*
     * Get the arenas settings.
     */
    @Override
    public final ArenaSettings getSettings() {
        return _arenaProvider.getSettings();
    }

    /*
     * Get the arenas data folder. This is where modules should store custom information
     * for an arena.
     */
    @Override
    public File getDataFolder(PVStarModule module) {
        return new File(_dataFolder, module.getName());
    }

    /*
     * Get the arenas data folder. This is where modules should store custom information
     * for an arena.
     */
    @Override
    public File getDataFolder(ArenaExtension module) {
        return new File(_dataFolder, module.getName());
    }

    /*
     * Get the arenas name
     */
    @Override
    public final String getName() {
        return _name;
    }

    /*
     * Set the arenas name.
     */
    @Override
    public final void setName(String name) {
        PreCon.notNullOrEmpty(name);

        _name = name;
        _searchName = _name.toLowerCase();
    }

    /*
     * Get the arenas name in lower case letters.
     */
    @Override
    public final String getSearchName() {
        return _searchName;
    }

    /*
     * Get the arenas unique ID.
     */
    @Override
    public final UUID getId() {
        return _id;
    }

    /*
     * Get the arenas type name.
     */
    public final String getTypeName() {
        return _typeInfo.typeName();
    }

    /*
     * Get the arenas type description.
     */
    public final String getTypeDescription() {
        return _typeInfo.description();
    }

    /*
     * Get a data storage node from the arena.
     */
    @Override
    public final IDataNode getDataNode(String nodeName) {
        PreCon.notNullOrEmpty(nodeName);

        IDataNode node = _nodeMap.get(nodeName.toLowerCase());
        if (node == null) {
            node = _dataNode.getNode(nodeName);
            _nodeMap.put(nodeName.toLowerCase(), node);
        }

        return node;
    }

    /*
     * Get the permission players must have in order
     * to join the arena.
     */
    @Override
    public final IPermission getPermission() {
        return _permission;
    }

    /*
     * Get the arenas region.
     */
    @Override
    public final ArenaRegion getRegion() {
        return _region;
    }

    /*
     * Determine if the arena is busy. No actions
     * can be performed while the arena is busy.
     *
     * Arena may be busy after a game ends while it performs cleanup.
     */
    @Override
    public final boolean isBusy() {
        return _isBusy != 0;
    }

    /*
     * Set the arenas busy state.
     */
    @Override
    public final void setBusy() {
        _isBusy++;
    }

    /*
     * Set the arenas busy state to idle.
     */
    @Override
    public final void setIdle() {
        _isBusy--;
        _isBusy = Math.max(0, _isBusy);
    }

    /*
     * Get the number of available player slots left to join.
     */
    @Override
    public int getAvailableSlots() {

        int maxPlayers = getSettings().getMaxPlayers();

        // if spawns are reserved, the max players is the number of spawns
        if (getLobbyManager().getSettings().isPlayerSpawnsReserved()) {
            maxPlayers = getSpawnManager().getLobbyOrGameSpawns().size();
        }

        List<ArenaPlayer> players = getLobbyManager().getPlayers();
        if (players == null || players.isEmpty())
            return maxPlayers;

        return Math.max(0, maxPlayers - players.size());
    }

    /*
     * Determine if a player is in the arena.
     */
    @Override
    public boolean hasPlayer(ArenaPlayer player) {

        return getLobbyManager().hasPlayer(player) ||
               getGameManager().hasPlayer(player) ||
                getSpectatorManager().hasPlayer(player);
    }

    /*
     * Determine if players can join the arena.
     */
    @Override
    public final boolean canJoin() {
        return getSettings().isEnabled() &&
                !isBusy() &&
                getAvailableSlots() > 0 &&
                onCanJoin();
    }

    /*
     * Join a player to the arena.
     */
    @Override
    public boolean join(ArenaPlayer player, AddPlayerReason reason) {
        PreCon.notNull(player);
        PreCon.notNull(reason);
        PreCon.isValid(reason != AddPlayerReason.ARENA_RELATION_CHANGE);
        PreCon.isValid(reason != AddPlayerReason.FORWARDING);

        // Make sure the player is not already in an arena
        Arena currentArena = player.getArena();
        if (currentArena != null) {
            Msg.tellError(player, Lang.get(_JOIN_LEAVE_CURRENT_FIRST, getName()));
            return false; // finish
        }

        // make sure arena is enabled
        if (!getSettings().isEnabled()) {
            Msg.tellError(player, Lang.get(_ARENA_DISABLED, getName()));
            return false; // finish
        }

        // check player permission
        if (!player.getHandle().hasPermission(_permission.getName())) {
            Msg.tellError(player.getHandle(), Lang.get(_JOIN_NO_PERMISSION), getName());
            return false;
        }

        // make sure arena isn't busy
        if (isBusy()) {
            Msg.tellError(player, Lang.get(_ARENA_BUSY, getName()));
            return false; // finish
        }

        // make sure game isn't already running
        if (getGameManager().isRunning()) {
            Msg.tellError(player, Lang.get(_ARENA_RUNNING, getName()));
            return false; // finish
        }

        // make sure there are enough join slots available
        if (getAvailableSlots() <= 0) {
            Msg.tellError(player, Lang.get(_JOIN_LIMIT_REACHED, getName()));
            return false; // finish
        }

        // add player to lobby
        return getLobbyManager().addPlayer(player, reason);
    }

    /*
     * Remove a player from the arena. Should not be used for arena relation
     * changes or forwarding.
     */
    @Override
    public boolean remove(ArenaPlayer player, RemovePlayerReason reason) {
        PreCon.notNull(player);
        PreCon.notNull(reason);
        PreCon.isValid(reason != RemovePlayerReason.ARENA_RELATION_CHANGE);
        PreCon.isValid(reason != RemovePlayerReason.FORWARDING);

        // make sure the player is in the game if they are being removed as a loss
        if (reason == RemovePlayerReason.LOSE && !getGameManager().hasPlayer(player))
            return false;

        PlayerManager manager = player.getRelatedManager();
        return manager != null && manager.removePlayer(player, reason);
    }

    /*
     * Dispose the arena and release resources.
     * Calls disable method first.
     */
    @Override
    public final void dispose() {

        getSettings().setEnabled(false);

        onDispose();

        Permissions.unregister("pvstar.arena." + _name);

        getEventManager().call(new ArenaDisposeEvent(this));

        DataStorage.removeStorage(PVStarAPI.getPlugin(), new DataPath("arenas." + _id.toString()));

        if (_dataFolder.exists() && !_dataFolder.delete()) {
            Msg.warning("Failed to delete arena folder: {0}", _dataFolder.getAbsolutePath());
        }

        getEventManager().dispose();

        _dataNode = null;
    }

    /*
     * Return the name of the arena as the object string.
     */
    @Override
    public String toString() {
        return _name;
    }

    /*
     * Get the arena provider.
     */
    protected ArenaProvider getArenaProvider() {
        return new ArenaProvider(this);
    }

    /*
     * Called when the arena is initialized.
     */
    protected void onInit() {}

    /*
     * Called when the arena is enabled.
     */
    protected void onEnable() {}

    /*
     * Called when the arena is disabled.
     */
    protected void onDisable() {}

    /*
     * Called when the arena is disposed.
     */
    protected void onDispose() {}

    /*
     * Called when determining if players can join.
     */
    protected abstract boolean onCanJoin();

}
