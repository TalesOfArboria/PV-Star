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


package com.jcwhatever.pvs.arenas.context;

import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Result;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.ArenaTeam;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.collections.IArenaPlayerCollection;
import com.jcwhatever.pvs.api.arena.context.IGameContext;
import com.jcwhatever.pvs.api.arena.context.ILobbyContext;
import com.jcwhatever.pvs.api.arena.options.AddToContextReason;
import com.jcwhatever.pvs.api.arena.options.ArenaContext;
import com.jcwhatever.pvs.api.arena.options.ArenaStartReason;
import com.jcwhatever.pvs.api.arena.options.PlayerJoinArenaReason;
import com.jcwhatever.pvs.api.arena.options.PlayerLeaveArenaReason;
import com.jcwhatever.pvs.api.arena.options.RemoveFromContextReason;
import com.jcwhatever.pvs.api.arena.settings.IGameSettings;
import com.jcwhatever.pvs.api.events.ArenaEndedEvent;
import com.jcwhatever.pvs.api.events.ArenaPreStartEvent;
import com.jcwhatever.pvs.api.events.ArenaStartedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerAddedToContextEvent;
import com.jcwhatever.pvs.api.events.players.PlayerAddedToGameEvent;
import com.jcwhatever.pvs.api.events.players.PlayerJoinedArenaEvent;
import com.jcwhatever.pvs.api.events.players.PlayerLoseEvent;
import com.jcwhatever.pvs.api.events.players.PlayerPreJoinArenaEvent;
import com.jcwhatever.pvs.api.events.players.PlayerRemovedFromGameEvent;
import com.jcwhatever.pvs.api.events.players.PlayerWinEvent;
import com.jcwhatever.pvs.api.events.team.TeamLoseEvent;
import com.jcwhatever.pvs.api.events.team.TeamWinEvent;
import com.jcwhatever.pvs.api.utils.ArenaPlayerArrayList;
import com.jcwhatever.pvs.arenas.AbstractArena;
import com.jcwhatever.pvs.arenas.Arena;
import com.jcwhatever.pvs.arenas.managers.SpawnManager;
import com.jcwhatever.pvs.arenas.settings.PVGameSettings;

import org.bukkit.Location;

import java.util.Collection;
import java.util.Date;
import javax.annotation.Nullable;

/**
 * Game manager implementation
 */
public class GameContext extends AbstractContextManager implements IGameContext {

    private final IGameSettings _settings;

    private boolean _isRunning = false;
    private boolean _isGameOver = false;
    private Date _startTime;

    /*
     * Constructor.
     */
    public GameContext(AbstractArena arena) {
        super(arena);

        _settings = new PVGameSettings(arena);
    }

    @Override
    public ArenaContext getContext() {
        return ArenaContext.GAME;
    }

    @Override
    @Nullable
    public Date getStartTime() {
        return _startTime;
    }

    @Override
    public boolean isRunning() {
        return _isRunning;
    }

    @Override
    public boolean isGameOver() {
        return _isGameOver;
    }

    @Override
    public IGameSettings getSettings() {
        return _settings;
    }

    @Override
    public final boolean canStart() {
        return getArena().getSettings().isEnabled() &&
                !getArena().isBusy();
    }

    @Override
    public final boolean start(ArenaStartReason reason) {

        if (!canStart())
            return false;

        _startTime = new Date();

        ILobbyContext lobbyManager = getArena().getLobby();

        // get default next group of players from lobby
        IArenaPlayerCollection players = reason == ArenaStartReason.AUTO
                ? lobbyManager.getNextGroup()
                : lobbyManager.getReadyGroup();

        // create pre-start event
        ArenaPreStartEvent preStartEvent = new ArenaPreStartEvent(getArena(),
                new ArenaPlayerArrayList(players, false), reason);

        // call pre-start event
        if (getArena().getEventManager().call(this, preStartEvent).isCancelled())
            return false;

        // determine if there are players joining
        if (preStartEvent.getJoiningPlayers().isEmpty())
            return false;

        // transfer players from lobby
        if (!transferPlayersFromLobby(preStartEvent.getJoiningPlayers()))
            return false;

        _isRunning = true;
        _isGameOver = false;

        // call arena started event
        getArena().getEventManager().call(this,
                new ArenaStartedEvent(getArena(), reason));

        return true;
    }

    @Override
    public boolean end() {

        if (!isRunning())
            return false;

        _isRunning = false;
        _isGameOver = false;

        getArena().getSpawns().clearReserved();

        _startTime = null;

        getArena().getEventManager().call(this, new ArenaEndedEvent(getArena()));

        for (IArenaPlayer player : getPlayers()) {
            getArena().remove(player, PlayerLeaveArenaReason.GAME_ENDED);
        }

        return true;
    }

    @Override
    public boolean forwardPlayer(IArenaPlayer player, IArena nextArena) {
        PreCon.notNull(player);
        PreCon.isValid(nextArena instanceof Arena);

        Arena arena = (Arena)nextArena;

        if (!arena.getSettings().isEnabled())
            return false;

        Result<Location> result = removePlayer(player, RemoveFromContextReason.FORWARDING);
        if (!result.isSuccess())
            return false;

        PlayerPreJoinArenaEvent preJoin = new PlayerPreJoinArenaEvent(
                arena, player, PlayerJoinArenaReason.FORWARDING);

        arena.getEventManager().call(this, preJoin);

        if (preJoin.isCancelled())
            return false;

        boolean isAdded = nextArena.getGame().isRunning()
                ? arena.getGame().addPlayer(player, AddToContextReason.FORWARDING)
                : arena.getLobby().addPlayer(player, AddToContextReason.FORWARDING);

        if (isAdded) {
            PlayerJoinedArenaEvent joined = new PlayerJoinedArenaEvent(
                    arena, player, PlayerJoinArenaReason.FORWARDING, player.getContextManager());

            arena.getEventManager().call(this, joined);
        }

        return true;
    }

    @Override
    public boolean setWinner(IArenaPlayer player) {
        PreCon.notNull(player);

        if (!isRunning())
            return false;

        if (player.getTeam() == ArenaTeam.NONE) {
            for (IArenaPlayer otherPlayer : getPlayers()) {
                if (!player.equals(otherPlayer)) {
                    callLoseEvent(player);
                }
            }
        } else {
            return setWinner(player.getTeam());
        }

        callWinEvent(player);

        end();
        return true;
    }

    @Override
    public boolean setWinner(ArenaTeam team) {
        PreCon.notNull(team);

        if (!isRunning())
            return false;

        IArenaPlayerCollection winningTeam = new ArenaPlayerArrayList(getPlayers().size());

        for (IArenaPlayer player : getPlayers()) {
            if (player.getTeam() == team) {
                winningTeam.add(player);
                callWinEvent(player);
            } else {
                callLoseEvent(player);
            }
        }

        TeamWinEvent winEvent = new TeamWinEvent(getArena(), team, winningTeam, null);

        getArena().getEventManager().call(this, winEvent);

        if (winEvent.getWinMessage() != null)
            tell(winEvent.getWinMessage());

        end();
        return true;
    }

    @Override
    public boolean setLoser(ArenaTeam team) {

        if (!isRunning())
            return false;

        IArenaPlayerCollection losingTeam = new ArenaPlayerArrayList(getPlayers().size());

        for (IArenaPlayer player : getPlayers()) {
            if (player.getTeam() == team) {
                losingTeam.add(player);
                removePlayer(player, RemoveFromContextReason.LOSE);
            }
        }

        getArena().getEventManager().call(this,
                new TeamLoseEvent(getArena(), team, losingTeam, null));

        return true;
    }

    @Override
    @Nullable
    protected Location onPrePlayerAdd(IArenaPlayer player, AddToContextReason reason) {

        // lobby and game spawns are the same if there are no game spawns.
        if (!getArena().getSpawns().hasLobbySpawns())
            return null;

        // get a game spawn location
        return SpawnManager.getRespawnLocation(
                this, ArenaContext.GAME, new Location(null, 0, 0, 0));
    }

    @Nullable
    @Override
    protected PlayerAddedToContextEvent onPlayerAdded(
            IArenaPlayer player, AddToContextReason reason, PlayerAddedToContextEvent contextEvent) {

        PlayerAddedToGameEvent event = new PlayerAddedToGameEvent(contextEvent);
        getArena().getEventManager().call(this, event);

        return event;
    }

    @Override
    protected void onPreRemovePlayer(IArenaPlayer player, RemoveFromContextReason reason) {

        if (reason == RemoveFromContextReason.LOSE ||
                reason == RemoveFromContextReason.KICK ||
                reason == RemoveFromContextReason.LOGOUT) {

            callLoseEvent(player);
        }
    }

    @Override
    protected Location onRemovePlayer(IArenaPlayer player, RemoveFromContextReason reason) {

        Scheduler.runTaskLater(PVStarAPI.getPlugin(), 1, new Runnable() {
            @Override
            public void run() {
                if (_players.size() == 0)
                    end();
            }
        });

        IArena arena = getArena();

        PlayerRemovedFromGameEvent event = new PlayerRemovedFromGameEvent(
                arena, player, this, getContext(), reason);

        arena.getEventManager().call(this, event);

        return arena.getSettings().getRemoveLocation();
    }

    /*
     *  Transfer the next group of players from the lobby to here.
     */
    private boolean transferPlayersFromLobby(Collection<IArenaPlayer> players) {

        LobbyContext lobbyManager = getArena().getLobby();

        // transfer players from lobby
        for (IArenaPlayer player : players) {

            if (!lobbyManager.getPlayers().contains(player))
                continue;

            lobbyManager.removePlayer(player, RemoveFromContextReason.CONTEXT_CHANGE);

            addPlayer(player, AddToContextReason.CONTEXT_CHANGE);
        }

        return true;
    }

    /*
     * Call PlayerWinEvent and display message if any
     */
    private void callWinEvent(IArenaPlayer player) {
        PlayerWinEvent event = new PlayerWinEvent(getArena(), player, this, null);
        getArena().getEventManager().call(this, event);

        if (event.getWinMessage() != null)
            tell(event.getWinMessage());
    }

    /*
     * Call PlayerLoseEvent and display message if any
     */
    private void callLoseEvent(IArenaPlayer player) {
        PlayerLoseEvent event = new PlayerLoseEvent(getArena(), player, this, null);
        getArena().getEventManager().call(this, event);

        if (event.getLoseMessage() != null)
            tell(event.getLoseMessage());
    }
}
