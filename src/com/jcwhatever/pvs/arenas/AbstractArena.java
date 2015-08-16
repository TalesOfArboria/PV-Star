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
import com.jcwhatever.pvs.ArenaExtensionManager;
import com.jcwhatever.pvs.ArenaManager;
import com.jcwhatever.pvs.ArenaPlayer;
import com.jcwhatever.pvs.PVEventManager;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.ArenaRegion;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.collections.IArenaPlayerCollection;
import com.jcwhatever.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.pvs.api.arena.options.AddToContextReason;
import com.jcwhatever.pvs.api.arena.options.ArenaContext;
import com.jcwhatever.pvs.api.arena.options.PlayerJoinArenaReason;
import com.jcwhatever.pvs.api.arena.options.PlayerLeaveArenaReason;
import com.jcwhatever.pvs.api.events.ArenaBusyEvent;
import com.jcwhatever.pvs.api.events.ArenaDisposeEvent;
import com.jcwhatever.pvs.api.events.ArenaIdleEvent;
import com.jcwhatever.pvs.api.events.ArenaLoadedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerJoinedArenaEvent;
import com.jcwhatever.pvs.api.events.players.PlayerLeaveArenaEvent;
import com.jcwhatever.pvs.api.events.players.PlayerPreJoinArenaEvent;
import com.jcwhatever.pvs.api.modules.PVStarModule;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.arenas.context.AbstractContextManager;
import com.jcwhatever.pvs.arenas.context.GameContext;
import com.jcwhatever.pvs.arenas.context.LobbyContext;
import com.jcwhatever.pvs.arenas.context.SpectatorContext;
import com.jcwhatever.pvs.arenas.managers.SpawnManager;
import com.jcwhatever.pvs.arenas.managers.TeamManager;
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

    private PVEventManager _eventManager;
    private ArenaRegion _region;
    private IPermission _permission;

    private Map<String, IDataNode> _nodeMap =
            new MapMaker().weakValues().concurrencyLevel(1).initialCapacity(20).makeMap();

    private GameContext _gameManager;
    private LobbyContext _lobbyManager;
    private SpectatorContext _spectatorManager;
    private SpawnManager _spawnManager;
    private TeamManager _teamManager;
    private ArenaExtensionManager _extensionManager;
    private PVArenaSettings _arenaSettings;

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

        _eventManager = new PVEventManager(PVStarAPI.getEventManager());

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
        _lobbyManager = new LobbyContext(this);
        _gameManager = new GameContext(this);
        _spectatorManager = new SpectatorContext(this);
        _spawnManager = new SpawnManager(this);
        _teamManager = new TeamManager(this);
        _extensionManager = new ArenaExtensionManager(this);

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
    public final PVEventManager getEventManager() {
        return _eventManager;
    }

    @Override
    public final LobbyContext getLobby() {
        return _lobbyManager;
    }

    @Override
    public final GameContext getGame() {
        return _gameManager;
    }

    @Override
    public final SpectatorContext getSpectators() {
        return _spectatorManager;
    }

    @Override
    public final TeamManager getTeams() {
        return _teamManager;
    }

    @Override
    public final SpawnManager getSpawns() {
        return _spawnManager;
    }

    @Override
    public final ArenaExtensionManager getExtensions() {
        return _extensionManager;
    }

    @Override
    public final PVArenaSettings getSettings() {
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
        ((ArenaManager)PVStarAPI.getArenaManager()).saveArenaName(this);
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
        if (getLobby().getSettings().isPlayerSpawnsReserved()) {
            int slotsLeft = getSpawns().getAll(ArenaContext.LOBBY).size();

            if (slotsLeft == 0 &&
                    getSpawns().totalReserved(
                            PVStarAPI.getSpawnTypeManager().getLobbySpawnType()) == 0) {
                slotsLeft = getSpawns().getAll(ArenaContext.GAME).size();
            }

            return slotsLeft;
        }

        IArenaPlayerCollection players = getLobby().getPlayers();
        if (players == null || players.isEmpty())
            return maxPlayers;

        return Math.max(0, maxPlayers - players.size());
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

        PlayerPreJoinArenaEvent preJoinEvent = new PlayerPreJoinArenaEvent(
                this, player, PlayerJoinArenaReason.PLAYER_JOIN);

        if (getEventManager().call(this, preJoinEvent).isCancelled()) {

            List<String> rejectionMessages = preJoinEvent.getRejectionMessages();

            for (String message : rejectionMessages) {
                Msg.tellError(player, message);
            }

            return false;
        }

        if (getLobby().addPlayer(player, AddToContextReason.PLAYER_JOIN)) {

            PlayerJoinedArenaEvent joinEvent = new PlayerJoinedArenaEvent(
                    this, player, PlayerJoinArenaReason.PLAYER_JOIN, null);

            getEventManager().call(this, joinEvent);

            return true;
        }

        return false;
    }

    public boolean remove(IArenaPlayer player, PlayerLeaveArenaReason reason) {
        PreCon.isValid(player instanceof ArenaPlayer);
        PreCon.notNull(reason);
        PreCon.isValid(reason != PlayerLeaveArenaReason.FORWARDING);

        // make sure the player is in the game if they are being removed as a loss
        if (reason == PlayerLeaveArenaReason.LOSE && player.getContext() != ArenaContext.GAME)
            return false;

        AbstractContextManager manager = (AbstractContextManager)player.getContextManager();
        if (manager != null) {

            Result<Location> restoreLocation = manager.removePlayer(player, reason.getContextEquivalent());
            if (restoreLocation.hasResult()) {

                PlayerLeaveArenaEvent leaveEvent = new PlayerLeaveArenaEvent(
                        this, player, manager, reason, restoreLocation.getResult());

                getEventManager().call(this, leaveEvent);

                ((ArenaPlayer)player).clearArena();

                if (leaveEvent.isRestoring() &&
                        leaveEvent.getRestoreLocation() != null) {

                    if (player.getPlayer().isDead()) {
                        ((ArenaPlayer)player).setDeathRespawnLocation(leaveEvent.getRestoreLocation());
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

        getEventManager().forceDispose();

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
