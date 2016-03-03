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


package com.jcwhatever.pvs.players;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.events.respacks.MissingRequiredResourcePackEvent;
import com.jcwhatever.nucleus.events.respacks.MissingRequiredResourcePackEvent.Action;
import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.managed.teleport.TeleportMode;
import com.jcwhatever.nucleus.managed.teleport.Teleporter;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.Npcs;
import com.jcwhatever.nucleus.providers.npc.events.NpcDeathEvent;
import com.jcwhatever.nucleus.utils.MetaStore;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Rand;
import com.jcwhatever.nucleus.utils.coords.LocationUtils;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.ArenaRegion;
import com.jcwhatever.pvs.api.arena.ArenaTeam;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.IArenaPlayerGroup;
import com.jcwhatever.pvs.api.arena.IBukkitPlayer;
import com.jcwhatever.pvs.api.arena.INpcPlayer;
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
import com.jcwhatever.pvs.api.events.players.PlayerArenaSpawnedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerContextChangeEvent;
import com.jcwhatever.pvs.api.events.players.PlayerLivesChangeEvent;
import com.jcwhatever.pvs.api.events.players.PlayerReadyEvent;
import com.jcwhatever.pvs.api.events.players.PlayerTeamChangedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerTeamPreChangeEvent;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.arenas.AbstractArena;
import com.jcwhatever.pvs.arenas.context.AbstractContextManager;
import com.jcwhatever.pvs.arenas.managers.SpawnManager;
import com.jcwhatever.pvs.stats.SessionStatTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * PVStar implementation of {@link IArenaPlayer}.
 */
public abstract class ArenaPlayer implements IArenaPlayer {

    private static final Map<UUID, ArenaPlayer> PLAYER_MAP = new HashMap<>(100);
    private static final Map<UUID, MetaStore> META = new HashMap<UUID, MetaStore>(100);
    private static BukkitPlayerListener LISTENER;
    private static DeathChecker DEATH_CHECKER;

    /*
     * Get a singleton wrapper instance.
     */
    @Nullable
    public static ArenaPlayer get(Player player) {

        if (Npcs.isNpc(player)) {
            if (!Npcs.hasProvider()) {
                Msg.debug("Npc provider not installed.");
                return null;
            }

            INpc npc = Npcs.getNpc(player);
            if (npc == null) {
                Msg.debug("Npc does not have an INpc instance.");
                return null;
            }

            return get(npc);
        }

        ArenaPlayer arenaPlayer = PLAYER_MAP.get(player.getUniqueId());
        if (arenaPlayer == null) {
            arenaPlayer = new BukkitPlayer(player);
            PLAYER_MAP.put(player.getUniqueId(), arenaPlayer);
        }

        init();
        return arenaPlayer;
    }

    /*
     * Get a singleton wrapper instance.
     */
    @Nullable
    public static ArenaPlayer get(INpc npc) {

        if (npc.isDisposed()) {
            Msg.debug("Cannot use disposed npc.");
            return null;
        }

        ArenaPlayer arenaPlayer = PLAYER_MAP.get(npc.getId());
        if (arenaPlayer == null) {
            arenaPlayer = new NpcPlayer(npc);
            PLAYER_MAP.put(npc.getId(), arenaPlayer);
        }

        init();

        return arenaPlayer;
    }

    /*
     * Dispose the singleton instance of an arena player.
     */
    public static void dispose(IArenaPlayer player) {
        PLAYER_MAP.remove(player.getUniqueId());
    }

    private static void init() {
        if (LISTENER == null) {
            LISTENER = new BukkitPlayerListener();
            Bukkit.getPluginManager().registerEvents(LISTENER, PVStarAPI.getPlugin());
        }

        if (DEATH_CHECKER == null) {
            DEATH_CHECKER = new DeathChecker();
            Scheduler.runTaskRepeat(PVStarAPI.getPlugin(), 1, 1, DEATH_CHECKER);
        }
    }

    public final Location IMMOBILIZE_LOCATION = new Location(null, 0, 0, 0);
    private final Location _deathRespawnLocation = new Location(null, 0, 0, 0);
    private final SessionStatTracker _sessionStats = new SessionStatTracker(this);

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
    private long _deathTick = -1;
    // player to blame code induced death on
    protected IArenaPlayer _deathBlamePlayer;
    // Meta data object to store extra meta. Disposed when the player starts a new game.
    private MetaStore _sessionMeta = new MetaStore();
    private MetaStore _globalMeta = new MetaStore();

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
        _sessionMeta = new MetaStore();

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
        META.clear();
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
    public SessionStatTracker getSessionStats() {
        return _sessionStats;
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

        MetaStore meta = META.get(arenaId);
        if (meta == null) {
            meta = new MetaStore();
            META.put(arenaId, meta);
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

    @Nullable
    @Override
    public Location respawn() {

        AbstractContextManager manager = getContextManager();
        if (manager == null)
            return null;

        Location location = SpawnManager.getRespawnLocation(manager, getContext());

        if (location == null)
            return null;

        PlayerArenaRespawnEvent respawnEvent = new PlayerArenaRespawnEvent(
                _arena, this, manager, location);
        _arena.getEventManager().call(this, respawnEvent);

        TeleportMode mode = manager.getSettings().getTeleportMode();

        if (teleport(respawnEvent.getRespawnLocation(), mode)) {
            PlayerArenaSpawnedEvent spawnEvent = new PlayerArenaSpawnedEvent(
                    _arena, this, manager, respawnEvent.getRespawnLocation());
            _arena.getEventManager().call(this, spawnEvent);
            return location;
        }

        return null;
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

    @Override
    public boolean teleport(Location location) {
        return teleport(location, TeleportMode.TARGET_ONLY);
    }

    @Override
    public boolean teleport(Location location, TeleportMode mode) {
        Entity entity = getEntity();
        return entity != null && Teleporter.teleport(entity, location, mode).isSuccess();
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

    private static class DeathChecker implements Runnable {

        final Map<ArenaPlayer, Void> dead = new WeakHashMap<>(10);
        long tickCount = 0;

        @Override
        public void run() {

            tickCount++;

            Iterator<Entry<ArenaPlayer, Void>> iterator = dead.entrySet().iterator();

            while (iterator.hasNext()) {

                ArenaPlayer player = iterator.next().getKey();
                if (player._deathTick < 0) {
                    iterator.remove();
                    continue;
                }

                if (!player.isDead()) {
                    player._deathTick = -1;
                    iterator.remove();
                    continue;
                }

                IArena arena = player.getArena();
                if (arena == null) {
                    player._deathTick = -1;
                    iterator.remove();
                    continue;
                }

                AbstractContextManager context = player.getContextManager();
                if (context == null) {
                    player._deathTick = -1;
                    iterator.remove();
                    continue;
                }

                int maxTicks = context.getSettings().getMaxDeathTicks();

                if (tickCount - player._deathTick > maxTicks) {
                    player.kick();
                }
            }
        }
    }

    /*
     * Bukkit event listener. Interprets Bukkit events into PV-Star player events.
     */
    private static class BukkitPlayerListener implements Listener {

        /*
         * Handle player deaths within arenas.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        private void onPlayerDeath(PlayerDeathEvent event) {

            final ArenaPlayer player = ArenaPlayer.get(event.getEntity());
            if (player == null)
                return;

            final AbstractArena arena = player.getArena();
            if (arena == null)
                return;

            double health = event.getEntity().getHealth();

            if (health > 0.0D)
                return;

            handleDeath(player, arena);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onNpcDeath(NpcDeathEvent event) {

            final ArenaPlayer player = ArenaPlayer.get(event.getNpc());
            if (player == null)
                return;

            final AbstractArena arena = player.getArena();
            if (arena == null)
                return;

            handleDeath(player, arena);
        }

        private void handleDeath(final ArenaPlayer player, final AbstractArena arena) {

            // decrement player lives
            player.setLives(player._lives - 1);
            player._deathBlamePlayer = null;

            // remove player from arena if no more lives
            if (player.getLives() < 1) {
                Scheduler.runTaskLater(PVStarAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        arena.remove(player, PlayerLeaveArenaReason.LOSE);
                    }
                });
            }
            else if (player instanceof IBukkitPlayer){
                // add to death checker
                AbstractContextManager context = player.getContextManager();
                if (context != null && context.getSettings().getMaxDeathTicks() > 0) {
                    player._deathTick = DEATH_CHECKER.tickCount;
                    DEATH_CHECKER.dead.put(player, null);
                }
            }
            else if (player instanceof INpcPlayer){
                // "respawn" npc
                Scheduler.runTaskLater(PVStarAPI.getPlugin(), 30, new Runnable() {
                    @Override
                    public void run() {

                        // get spawn location
                        Spawnpoint spawn = Rand.get(arena.getSpawns().getAll(player.getContext()));
                        if (spawn == null) {
                            arena.remove(player, PlayerLeaveArenaReason.LOSE);
                            return;
                        }

                        final PlayerArenaRespawnEvent respawnEvent = new PlayerArenaRespawnEvent(
                                arena, player, player.getContextManager(), spawn);
                        arena.getEventManager().call(this, respawnEvent);

                        // spawn npc
                        if (((INpcPlayer) player).getNpc().spawn(respawnEvent.getRespawnLocation())) {

                            Scheduler.runTaskLater(PVStarAPI.getPlugin(), new Runnable() {
                                @Override
                                public void run() {
                                    PlayerArenaSpawnedEvent spawnEvent = new PlayerArenaSpawnedEvent(
                                            arena, player, player.getContextManager(), respawnEvent.getRespawnLocation());
                                    arena.getEventManager().call(this, spawnEvent);
                                }
                            });
                        }
                        else {
                            arena.remove(player, PlayerLeaveArenaReason.LOSE);
                        }
                    }
                });
            }
        }

        /*
         * Handle player deaths within arenas.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        private void onNpcPlayerDeath(final NpcDeathEvent event) {

            final ArenaPlayer player = ArenaPlayer.get(event.getNpc());
            if (player == null)
                return;

            final AbstractArena arena = player.getArena();
            if (arena == null)
                return;

            // decrement player lives
            player.setLives(player._lives - 1);
            player._deathBlamePlayer = null;

            // remove player from arena if no more lives
            if (player.getLives() < 1) {
                Scheduler.runTaskLater(PVStarAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        arena.remove(player, PlayerLeaveArenaReason.LOSE);
                    }
                });
            }
            else {


            }
        }

        /*
         * Handle player damage and invulnerability.
         */
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        private void onPlayerDamage(EntityDamageEvent event) {

            Entity entity = event.getEntity();
            if (!(entity instanceof Player))
                return;

            IArenaPlayer player = ArenaPlayer.get((Player) entity);
            if (player == null || player.getArena() == null)
                return;

            if (player.isInvulnerable()) {
                event.setDamage(0.0D);
                event.setCancelled(true);
            }
        }

        /*
         * Make sure player is sent to remove location if required resource pack
         * is not loaded.
         */
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        private void onMissingResourcePack(MissingRequiredResourcePackEvent event) {

            IArenaPlayer player = ArenaPlayer.get(event.getPlayer());
            if (player == null)
                return;

            IArena arena = player.getArena();
            if (arena == null) {

                List<ArenaRegion> regions = Nucleus.getRegionManager().getRegions(
                        player.getLocation(), ArenaRegion.class);

                if (regions.size() > 0) {
                    arena = regions.get(0).getArena();

                    LocationUtils.copy(arena.getSettings().getRemoveLocation(), event.getRelocation());
                    event.setAction(Action.RELOCATE);
                }
                return;
            }

            player.kick();
            event.setAction(Action.IGNORE);
        }
    }
}
