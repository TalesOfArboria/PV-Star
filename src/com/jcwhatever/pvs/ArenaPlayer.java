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

import com.jcwhatever.nucleus.utils.MetaStore;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.coords.LocationUtils;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.ArenaTeam;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.IArenaPlayerGroup;
import com.jcwhatever.pvs.api.arena.context.IContextManager;
import com.jcwhatever.pvs.api.arena.options.AddToContextReason;
import com.jcwhatever.pvs.api.arena.options.ArenaContext;
import com.jcwhatever.pvs.api.arena.options.LivesBehavior;
import com.jcwhatever.pvs.api.arena.options.PlayerLeaveArenaReason;
import com.jcwhatever.pvs.api.arena.options.PointsBehavior;
import com.jcwhatever.pvs.api.arena.options.RemoveFromContextReason;
import com.jcwhatever.pvs.api.arena.options.TeamChangeReason;
import com.jcwhatever.pvs.api.arena.settings.IContextSettings;
import com.jcwhatever.pvs.api.arena.settings.IGameSettings;
import com.jcwhatever.pvs.api.events.players.PlayerArenaRespawnEvent;
import com.jcwhatever.pvs.api.events.players.PlayerContextChangeEvent;
import com.jcwhatever.pvs.api.events.players.PlayerLivesChangeEvent;
import com.jcwhatever.pvs.api.events.players.PlayerReadyEvent;
import com.jcwhatever.pvs.api.events.players.PlayerTeamChangedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerTeamPreChangeEvent;
import com.jcwhatever.pvs.arenas.AbstractArena;
import com.jcwhatever.pvs.arenas.context.AbstractContextManager;
import com.jcwhatever.pvs.arenas.managers.SpawnManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * PVStar implementation of {@link IArenaPlayer}.
 */
public class ArenaPlayer implements IArenaPlayer {

    private static final Map<UUID, ArenaPlayer> _playerMap = new HashMap<>(100);
    private static final Map<UUID, MetaStore> _meta = new HashMap<UUID, MetaStore>(100);
    private static BukkitPlayerListener _listener;

    /*
     * Get a singleton wrapper instance.
     */
    public static ArenaPlayer get(Player p) {

        ArenaPlayer player = _playerMap.get(p.getUniqueId());
        if (player == null) {
            player = new ArenaPlayer(p);
            _playerMap.put(p.getUniqueId(), player);
        }

        if (_listener == null) {
            _listener = new BukkitPlayerListener();
            Bukkit.getPluginManager().registerEvents(_listener, PVStarAPI.getPlugin());
        }

        return player;
    }

    /*
     * Dispose the singleton instance of an arena player.
     */
    public static void dispose(IArenaPlayer player) {
        _playerMap.remove(player.getUniqueId());
    }

    public final Location IMMOBILIZE_LOCATION = new Location(null, 0, 0, 0);
    private final Player _player;
    private final Location _deathRespawnLocation = new Location(null, 0, 0, 0);

    private boolean _isReady;
    private boolean _isImmobilized;
    private boolean _isInvulnerable;
    private AbstractArena _arena;
    private ArenaTeam _team = ArenaTeam.NONE;
    private IArenaPlayerGroup _playerGroup;
    private int _lives = 0;
    private int _totalPoints = 0;
    private int _points;
    private Date _lastJoin;
    // player to blame code induced death on
    private IArenaPlayer _deathBlamePlayer;
    // Meta data object to store extra meta. Disposed when the player starts a new game.
    private MetaStore _sessionMeta = new MetaStore();
    private MetaStore _globalMeta = new MetaStore();

    /**
     * Private Constructor.
     *
     * @param player  The player.
     */
    private ArenaPlayer(Player player) {
        _player = player;
    }

    /**
     * Set the current arena the player is in.
     *
     * @param arena  The arena.
     */
    public void setCurrentArena(AbstractArena arena) {
        PreCon.notNull(arena);

        if (arena.equals(_arena))
            return;

        _arena = arena;
        _lastJoin = new Date();

        IGameSettings settings = arena.getGame().getSettings();
        LivesBehavior livesBehavior = settings.getLivesBehavior();
        PointsBehavior pointsBehavior = settings.getPointsBehavior();

        // Handle lives behavior
        switch (livesBehavior) {
            case STATIC:
                // only use arena lives setting if lives have not been changed
                if (_lives == 0)
                    setLives(settings.getStartLives());
                break;

            case RESET:
                // always reset lives to current arena setting.
                setLives(settings.getStartLives());
                break;

            case ADDITIVE:
                // Add arena lives to current lives.
                setLives(_lives + settings.getStartLives());
                break;

            default:
                setLives(1);
                break;
        }

        // Handle points behavior
        switch (pointsBehavior) {
            case STATIC:
                // only use start points if points have not been changed
                if (_totalPoints == 0) {
                    _totalPoints = settings.getStartPoints();
                    _points = settings.getStartPoints();
                }
                break;

            case RESET:
                // always reset start points
                _totalPoints = settings.getStartPoints();
                _points = settings.getStartPoints();
                break;

            case ADDITIVE:
                // add start points to current points
                _totalPoints += settings.getStartPoints();
                _points += settings.getStartPoints();
                break;

            default:
                _totalPoints = 0;
                _points = 0;
                break;
        }
    }

    /**
     * Declare the player as no longer in an arena.
     */
    public void clearArena() {

        _arena = null;
        _meta.clear();
        _isReady = false;
        _isImmobilized = false;
        _isInvulnerable = false;
        _sessionMeta = new MetaStore();
        _lives = 0;
        _totalPoints = 0;
        _points = 0;
    }

    /**
     * Set the players current player group.
     *
     * @param playerGroup  The player group.
     */
    public void setPlayerGroup(@Nullable IArenaPlayerGroup playerGroup) {

        // null player group, check that player is not in any group
        if (playerGroup == null && _playerGroup != null) {
            if (_playerGroup.hasPlayer(this))
                throw new IllegalStateException(
                        "Cannot remove player group flag while a player is still in the group.");
        }

        // make sure player is in group being set
        if (playerGroup != null && !playerGroup.hasPlayer(this)) {
            throw new IllegalStateException(
                    "Cannot set a player group flag for a group that the player is not in.");
        }

        _playerGroup = playerGroup;
    }

    /**
     * Copy the values of the location the player should respawn at after death
     * into an output {@link Location}.
     *
     * <p>Retrieving the location also causes it to be reset.</p>
     *
     * @param output  The output {@link Location}.
     *
     * @return  The output {@link Location} or null if not set.
     */
    @Nullable
    public Location getDeathRespawnLocation(Location output) {
        PreCon.notNull(output);

        if (_deathRespawnLocation.getWorld() == null)
            return null;

        Location result = LocationUtils.copy(_deathRespawnLocation, output);

        // reset location by setting world to null
        _deathRespawnLocation.setWorld(null);

        return result;
    }

    /**
     * Set the location the player should respawn at when they next respawn
     * due to death.
     *
     * @param location  The location to set.
     */
    public void setDeathRespawnLocation(@Nullable Location location) {
        if (location == null) {
            // reset location by setting world to null
            _deathRespawnLocation.setWorld(null);
        }
        else {
            LocationUtils.copy(location, _deathRespawnLocation);
        }
    }

    @Override
    public Player getPlayer() {
        return _player;
    }

    @Override
    public UUID getUniqueId() {
        return _player.getUniqueId();
    }

    @Override
    public String getName() {
        return _player.getName();
    }

    @Override
    public String getDisplayName() {
        return _player.getDisplayName() != null
                ? _player.getDisplayName()
                : _player.getName();
    }

    @Override
    public Location getLocation() {
        return _player.getLocation();
    }

    @Override
    public Location getLocation(Location output) {
        return _player.getLocation(output);
    }

    @Override
    @Nullable
    public AbstractArena getArena() {
        return _arena;
    }

    @Override
    @Nullable
    public Date getJoinDate() {
        return _lastJoin;
    }

    @Override
    public boolean isReady() {
        return _isReady;
    }

    @Override
    public void setReady(boolean isReady) {

        if (isReady == _isReady || _arena == null)
            return;

        _isReady = isReady;

        if (_isReady) {

            PlayerReadyEvent event = new PlayerReadyEvent(_arena, this, _arena.getLobby(), null);
            _arena.getEventManager().call(this, event);

            if (event.getMessage() != null) {
                IContextManager manager = getContextManager();
                if (manager != null) {
                    manager.tell(event.getMessage());
                }
            }
        }
    }

    @Override
    public boolean isImmobilized() {

        // Lobby immobilize setting overrides player setting when true
        if (getContext() == ArenaContext.LOBBY) {
            if (_arena.getLobby().getSettings().isImmobilized())
                return true;
        }

        return _isImmobilized;
    }

    @Override
    public void setImmobilized(boolean isImmobilized) {
        _isImmobilized =  isImmobilized;
    }

    @Override
    public boolean isInvulnerable() {
        return _isInvulnerable;
    }

    @Override
    public void setInvulnerable(boolean isInvulnerable) {
        _isInvulnerable = isInvulnerable;
    }

    @Override
    public ArenaTeam getTeam() {
        return _team;
    }

    @Override
    public void setTeam(ArenaTeam team, TeamChangeReason reason) {
        PreCon.notNull(team);
        PreCon.notNull(reason);

        if (_team == team)
            return;

        if (_arena == null)
            throw new RuntimeException("Cannot set team on a player that isn't in an arena.");

        IContextManager manager = getContextManager();
        if (manager == null)
            return;

        PlayerTeamPreChangeEvent preEvent = new PlayerTeamPreChangeEvent(
                _arena, this, manager, team, reason);
        _arena.getEventManager().call(this, preEvent);

        if (preEvent.isCancelled())
            return;

        ArenaTeam previousTeam = _team;
        _team = preEvent.getNewTeam();

        PlayerTeamChangedEvent postEvent = new PlayerTeamChangedEvent(
                _arena, this, manager, previousTeam, reason);
        _arena.getEventManager().call(this, postEvent);
    }

    @Override
    public int getLives() {
        return _lives;
    }

    @Override
    public int getTotalPoints() {
        return _totalPoints;
    }

    @Override
    public int getPoints() {
        return _points;
    }

    @Override
    public int incrementPoints(int amount) {
        if (amount > 0) {
            _totalPoints += amount;
        }
        _points += amount;
        return _points;
    }

    @Override
    @Nullable
    public IArenaPlayerGroup getPlayerGroup() {
        return _playerGroup;
    }

    @Override
    public ArenaContext getContext() {
        if (_arena == null)
            return ArenaContext.NONE;

        if (_arena.getLobby().getPlayers().contains(this))
            return ArenaContext.LOBBY;

        if (_arena.getGame().getPlayers().contains(this))
            return ArenaContext.GAME;

        if (_arena.getSpectators().getPlayers().contains(this))
            return ArenaContext.SPECTATOR;

        return ArenaContext.NONE;
    }

    @Override
    @Nullable
    public AbstractContextManager getContextManager() {
        if (_arena == null)
            return null;

        switch (getContext()) {
            case LOBBY:
                return _arena.getLobby();

            case SPECTATOR:
                return _arena.getSpectators();

            case GAME:
                return _arena.getGame();

            default:
                return null;
        }
    }

    @Override
    @Nullable
    public IContextSettings getContextSettings() {
        if (_arena == null)
            return null;

        switch (getContext()) {
            case LOBBY:
                return _arena.getLobby().getSettings();

            case SPECTATOR:
                return _arena.getSpectators().getSettings();

            case GAME:
                return _arena.getGame().getSettings();

            default:
                return null;
        }
    }

    @Override
    public boolean changeContext(ArenaContext context) {

        if (context == getContext())
            return false;

        IContextManager manager = getContextManager();
        if (manager == null)
            return false;

        ArenaContext previousContext = getContext();
        ArenaContext newContext;

        IContextManager target;

        switch (context) {
            case LOBBY:
                newContext = ArenaContext.LOBBY;
                target = _arena.getLobby();
                break;
            case GAME:
                newContext = ArenaContext.GAME;
                target = _arena.getGame();
                break;
            case SPECTATOR:
                newContext = ArenaContext.SPECTATOR;
                target = _arena.getSpectators();
                break;
            default:
                return false;
        }

        ((AbstractContextManager)manager)
                .removePlayer(this, RemoveFromContextReason.CONTEXT_CHANGE);

        boolean isSuccess = ((AbstractContextManager)target)
                .addPlayer(this, AddToContextReason.CONTEXT_CHANGE);

        if (isSuccess) {

            PlayerContextChangeEvent event = new PlayerContextChangeEvent(
                    _arena, this, previousContext, newContext);

            _arena.getEventManager().call(this, event);

            return true;
        }

        return false;
    }

    @Override
    public MetaStore getMeta(UUID arenaId) {
        PreCon.notNull(arenaId);

        MetaStore meta = _meta.get(arenaId);
        if (meta == null) {
            meta = new MetaStore();
            _meta.put(arenaId, meta);
        }

        return meta;
    }

    @Override
    public MetaStore getMeta() {
        return _globalMeta;
    }

    @Override
    public MetaStore getSessionMeta() {
        return _sessionMeta;
    }

    @Override
    public void kill() {
        _player.damage(_player.getMaxHealth());
    }

    @Override
    public void kill(@Nullable IArenaPlayer blame) {
        _deathBlamePlayer = blame;
        _player.damage(_player.getMaxHealth());
    }

    @Nullable
    @Override
    public Location respawn() {

        AbstractContextManager manager = getContextManager();
        if (manager == null)
            return null;

        Location location = SpawnManager.getRespawnLocation(
                manager, getContext(), new Location(null, 0, 0, 0));

        if (location == null)
            return null;

        PlayerArenaRespawnEvent event = new PlayerArenaRespawnEvent(_arena, this, manager, location);
        _arena.getEventManager().call(this, event);

        _player.teleport(event.getRespawnLocation());
        return location;
    }

    @Override
    public boolean leaveArena() {
        AbstractArena arena = getArena();
        return arena != null && arena.remove(this, PlayerLeaveArenaReason.PLAYER_LEAVE);
    }

    @Override
    public boolean kick() {
        AbstractArena arena = getArena();
        return arena != null && arena.remove(this, PlayerLeaveArenaReason.KICK);
    }

    @Override
    public boolean loseGame() {
        if (getContext() != ArenaContext.GAME)
            return false;

        AbstractArena arena = getArena();
        return arena != null && arena.remove(this, PlayerLeaveArenaReason.LOSE);
    }

    /*
     *  Set the player lives and run event
     */
    private void setLives(int lives) {

        if (_lives == lives || _arena == null)
            return;

        IContextManager manager = getContextManager();
        if (manager == null)
            return;

        PlayerLivesChangeEvent event = new PlayerLivesChangeEvent(
                _arena, this, manager, _lives, lives);

        _arena.getEventManager().call(this, event);

        if (event.isCancelled())
            return;

        _lives = event.getNewLives();
    }

    /*
     * Bukkit event listener. Interprets Bukkit events into PV-Star player events.
     */
    private static class BukkitPlayerListener implements Listener {

        /*
         * Handle player deaths within arenas.
         */
        @EventHandler(priority = EventPriority.LOWEST)
        private void onPlayerDeath(PlayerDeathEvent event) {

            ArenaPlayer player = ArenaPlayer.get(event.getEntity());

            AbstractArena arena = player.getArena();
            if (arena == null)
                return;

            double health = event.getEntity().getHealth();

            if (health > 0.0D)
                return;

            // decrement player lives
            player.setLives(player._lives - 1);

            player._deathBlamePlayer = null;

            // remove player from arena if no more lives
            if (player.getLives() < 1) {
                arena.remove(player, PlayerLeaveArenaReason.LOSE);
            }
        }

        /*
         * Handle player damage and invulnerability.
         */
        @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
        private void onPlayerDamage(EntityDamageEvent event) {

            Entity entity = event.getEntity();
            if (!(entity instanceof Player))
                return;

            IArenaPlayer player = ArenaPlayer.get((Player) entity);
            if (player.getArena() == null)
                return;

            if (player.isInvulnerable()) {
                event.setDamage(0.0D);
                event.setCancelled(true);
            }
        }

    }
}
