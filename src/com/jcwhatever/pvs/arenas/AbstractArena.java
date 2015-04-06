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


package com.jcwhatever.pvs.arenas;

import com.google.common.collect.MapMaker;
import com.jcwhatever.nucleus.events.manager.EventManager;
import com.jcwhatever.nucleus.events.manager.IEventListener;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.providers.permissions.IPermission;
import com.jcwhatever.nucleus.providers.permissions.Permissions;
import com.jcwhatever.nucleus.providers.storage.DataStorage;
import com.jcwhatever.nucleus.storage.DataPath;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Result;
import com.jcwhatever.pvs.PVArenaExtensionManager;
import com.jcwhatever.pvs.PVArenaPlayer;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.ArenaRegion;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.collections.IArenaPlayerCollection;
import com.jcwhatever.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.pvs.api.arena.extensions.IArenaExtensionManager;
import com.jcwhatever.pvs.api.arena.managers.IGameManager;
import com.jcwhatever.pvs.api.arena.managers.ILobbyManager;
import com.jcwhatever.pvs.api.arena.managers.IPlayerManager;
import com.jcwhatever.pvs.api.arena.managers.ISpawnManager;
import com.jcwhatever.pvs.api.arena.managers.ISpectatorManager;
import com.jcwhatever.pvs.api.arena.managers.ITeamManager;
import com.jcwhatever.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.pvs.api.arena.settings.IArenaSettings;
import com.jcwhatever.pvs.api.events.ArenaBusyEvent;
import com.jcwhatever.pvs.api.events.ArenaDisposeEvent;
import com.jcwhatever.pvs.api.events.ArenaIdleEvent;
import com.jcwhatever.pvs.api.events.ArenaLoadedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerJoinedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerLeaveEvent;
import com.jcwhatever.pvs.api.events.players.PlayerPreJoinEvent;
import com.jcwhatever.pvs.api.modules.PVStarModule;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.arenas.managers.PVGameManager;
import com.jcwhatever.pvs.arenas.managers.PVLobbyManager;
import com.jcwhatever.pvs.arenas.managers.PVSpawnManager;
import com.jcwhatever.pvs.arenas.managers.PVSpectatorManager;
import com.jcwhatever.pvs.arenas.managers.PVTeamManager;
import com.jcwhatever.pvs.arenas.settings.PVArenaSettings;

import org.bukkit.Location;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract arena implementation.
 */
public abstract class AbstractArena implements IArena, IDisposable, IEventListener {

    @Localizable static final String _JOIN_LEAVE_CURRENT_FIRST =
            "You must leave the current arena before you can join another.";

    @Localizable static final String _ARENA_BUSY =
            "Arena '{0: arena name}' is busy at the moment. Try again later.";

    @Localizable static final String _ARENA_RUNNING =
            "This arena is already in progress.";

    @Localizable static final String _ARENA_DISABLED =
            "Specified arena is not enabled.";

    @Localizable static final String _JOIN_LIMIT_REACHED =
            "No spots left. The arena is full.";

    @Localizable static final String _JOIN_NO_PERMISSION =
            "You don't have permission to join arena '{0: arena name}'.";

    private String _name;
    private String _searchName;
    private UUID _id;

    private boolean _isInitialized;
    private int _isBusy = 0;

    private ArenaTypeInfo _typeInfo;
    private IDataNode _dataNode;
    private File _dataFolder;

    private EventManager _eventManager;
    private ArenaRegion _region;
    private IPermission _permission;

    private Map<String, IDataNode> _nodeMap =
            new MapMaker().weakValues().concurrencyLevel(1).initialCapacity(20).makeMap();

    private IGameManager _gameManager;
    private ILobbyManager _lobbyManager;
    private ISpectatorManager _spectatorManager;
    private ISpawnManager _spawnManager;
    private ITeamManager _teamManager;
    private IArenaExtensionManager _extensionManager;
    private IArenaSettings _arenaSettings;

    private boolean _isDisposed;

    public final void init(UUID id, String name) {
        PreCon.notNull(id);
        PreCon.notNullOrEmpty(name);

        if (_isInitialized)
            throw new IllegalStateException("An arena can only be initialized once.");

        _isInitialized = true;

        _id = id;
        _name = name;
        _searchName = name.toLowerCase();
        _typeInfo = getClass().getAnnotation(ArenaTypeInfo.class);

        _eventManager = new EventManager(PVStarAPI.getPlugin(), PVStarAPI.getEventManager());

        _dataNode = DataStorage.get(PVStarAPI.getPlugin(), new DataPath("arenas." + id.toString()));
        _dataNode.load();

        _region = new ArenaRegion(this, _dataNode.getNode("region"));

        // get data folder
        File arenaFolder = new File(PVStarAPI.getPlugin().getDataFolder(), "arenas");
        _dataFolder = new File(arenaFolder, id.toString());

        if (!_dataFolder.exists() && !_dataFolder.mkdirs()) {
            Msg.severe("Failed to create data folder '{0}' for arena '{1}'.", _dataFolder.getAbsolutePath(), name);
        }

        _permission = Permissions.register("pvstar.arena." + _id.toString(), PermissionDefault.TRUE);

        _arenaSettings = new PVArenaSettings(this);
        _lobbyManager = new PVLobbyManager(this);
        _gameManager = new PVGameManager(this);
        _spectatorManager = new PVSpectatorManager(this);
        _spawnManager = new PVSpawnManager(this);
        _teamManager = new PVTeamManager(this);
        _extensionManager = new PVArenaExtensionManager(this);

        getEventManager().register(this);

        onInit();

        getEventManager().call(this, new ArenaLoadedEvent(this));
    }

    /**
     * Get the arenas type name.
     */
    public final String getTypeName() {
        return _typeInfo.typeName();
    }

    /**
     * Get the arenas type description.
     */
    public final String getTypeDescription() {
        return _typeInfo.description();
    }

    @Override
    public final EventManager getEventManager() {
        return _eventManager;
    }

    @Override
    public final ILobbyManager getLobbyManager() {
        return _lobbyManager;
    }

    @Override
    public final IGameManager getGameManager() {
        return _gameManager;
    }

    @Override
    public final ISpectatorManager getSpectatorManager() {
        return _spectatorManager;
    }

    @Override
    public final ITeamManager getTeamManager() {
        return _teamManager;
    }

    @Override
    public final ISpawnManager getSpawnManager() {
        return _spawnManager;
    }

    @Override
    public final IArenaExtensionManager getExtensionManager() {
        return _extensionManager;
    }

    @Override
    public final IArenaSettings getSettings() {
        return _arenaSettings;
    }

    @Override
    public File getDataFolder(PVStarModule module) {
        return new File(_dataFolder, module.getName());
    }

    @Override
    public File getDataFolder(ArenaExtension module) {
        return new File(_dataFolder, module.getName());
    }

    @Override
    public final String getName() {
        return _name;
    }

    @Override
    public final void setName(String name) {
        PreCon.notNullOrEmpty(name);

        _name = name;
        _searchName = _name.toLowerCase();
    }

    @Override
    public final String getSearchName() {
        return _searchName;
    }

    @Override
    public final UUID getId() {
        return _id;
    }

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

    @Override
    public final IPermission getPermission() {
        return _permission;
    }

    @Override
    public final ArenaRegion getRegion() {
        return _region;
    }

    @Override
    public final boolean isBusy() {
        return _isBusy != 0;
    }

    @Override
    public final void setBusy() {
        _isBusy++;

        if (_isBusy == 1) {
            getEventManager().call(this, new ArenaBusyEvent(this));
        }
    }

    @Override
    public final void setIdle() {
        boolean initialBusyState = isBusy();

        _isBusy--;
        _isBusy = Math.max(0, _isBusy);

        if (initialBusyState && !isBusy()) {
            getEventManager().call(this, new ArenaIdleEvent(this));
        }
    }

    @Override
    public int getAvailableSlots() {

        int maxPlayers = getSettings().getMaxPlayers();

        // if spawns are reserved, the max players is the number of spawns
        if (getLobbyManager().getSettings().isPlayerSpawnsReserved()) {
            maxPlayers = getSpawnManager().getLobbyOrGameSpawns().size();
        }

        IArenaPlayerCollection players = getLobbyManager().getPlayers();
        if (players == null || players.isEmpty())
            return maxPlayers;

        return Math.max(0, maxPlayers - players.size());
    }

    @Override
    public boolean hasPlayer(IArenaPlayer player) {

        return getLobbyManager().hasPlayer(player) ||
               getGameManager().hasPlayer(player) ||
                getSpectatorManager().hasPlayer(player);
    }

    @Override
    public final boolean canJoin() {
        return getSettings().isEnabled() &&
                !isBusy() &&
                getAvailableSlots() > 0 &&
                onCanJoin();
    }

    @Override
    public boolean join(IArenaPlayer player) {
        PreCon.notNull(player);

        PlayerPreJoinEvent preJoinEvent = new PlayerPreJoinEvent(this, player);

        if (getEventManager().call(this, preJoinEvent).isCancelled()) {

            List<String> rejectionMessages = preJoinEvent.getRejectionMessages();

            for (String message : rejectionMessages) {
                Msg.tellError(player, message);
            }

            return false;
        }

        if (getLobbyManager().addPlayer(player, AddPlayerReason.PLAYER_JOIN)) {

            PlayerJoinedEvent joinEvent = new PlayerJoinedEvent(this, player, null);
            getEventManager().call(this, joinEvent);

            return true;
        }

        return false;
    }

    @Override
    public boolean remove(IArenaPlayer player, RemovePlayerReason reason) {
        PreCon.isValid(player instanceof PVArenaPlayer);
        PreCon.notNull(reason);
        PreCon.isValid(reason != RemovePlayerReason.ARENA_RELATION_CHANGE);
        PreCon.isValid(reason != RemovePlayerReason.FORWARDING);

        // make sure the player is in the game if they are being removed as a loss
        if (reason == RemovePlayerReason.LOSE && !getGameManager().hasPlayer(player))
            return false;

        IPlayerManager manager = player.getRelatedManager();
        if (manager != null && manager.hasPlayer(player)) {

            Result<Location> restoreLocation = manager.removePlayer(player, reason);
            if (restoreLocation.hasResult()) {

                PlayerLeaveEvent leaveEvent
                        = new PlayerLeaveEvent(this, player, manager, reason, restoreLocation.getResult());
                getEventManager().call(this, leaveEvent);

                if (leaveEvent.isRestoring() &&
                        leaveEvent.getRestoreLocation() != null) {

                    if (player.getPlayer().isDead()) {
                        ((PVArenaPlayer)player).setDeathRespawnLocation(leaveEvent.getRestoreLocation());
                    }
                    else {
                        player.getPlayer().teleport(leaveEvent.getRestoreLocation());
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public final boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public final void dispose() {

        getSettings().setEnabled(false);

        onDispose();

        Permissions.unregister("pvstar.arena." + _id.toString());

        getEventManager().call(this, new ArenaDisposeEvent(this));

        getEventManager().dispose();

        getRegion().dispose();

        _dataNode = null;
        _isDisposed = true;
    }

    @Override
    public String toString() {
        return _name;
    }

    /*
     * Invoked when the arena is initialized.
     */
    protected void onInit() {}

    /*
     * Invoked when the arena is enabled.
     */
    protected void onEnable() {}

    /*
     * Invoked when the arena is disabled.
     */
    protected void onDisable() {}

    /*
     * Invoked when the arena is disposed.
     */
    protected void onDispose() {}

    /*
     * Invoked when determining if players can join.
     */
    protected abstract boolean onCanJoin();
}
