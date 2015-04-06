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


package com.jcwhatever.pvs.arenas.managers;

import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Result;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.collections.IArenaPlayerCollection;
import com.jcwhatever.pvs.api.arena.ArenaTeam;
import com.jcwhatever.pvs.api.arena.managers.IGameManager;
import com.jcwhatever.pvs.api.arena.managers.ILobbyManager;
import com.jcwhatever.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.pvs.api.arena.options.ArenaStartReason;
import com.jcwhatever.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.pvs.api.arena.settings.IGameSettings;
import com.jcwhatever.pvs.api.events.ArenaEndedEvent;
import com.jcwhatever.pvs.api.events.ArenaPreStartEvent;
import com.jcwhatever.pvs.api.events.ArenaStartedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerJoinedEvent;
import com.jcwhatever.pvs.api.events.players.PlayerLoseEvent;
import com.jcwhatever.pvs.api.events.players.PlayerPreJoinEvent;
import com.jcwhatever.pvs.api.events.players.PlayerWinEvent;
import com.jcwhatever.pvs.api.events.team.TeamLoseEvent;
import com.jcwhatever.pvs.api.events.team.TeamWinEvent;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.api.utils.ArenaPlayerArrayList;
import com.jcwhatever.pvs.arenas.settings.PVGameSettings;

import org.bukkit.Location;

import java.util.Collection;
import java.util.Date;
import javax.annotation.Nullable;

/**
 * Game manager implementation
 */
public class PVGameManager extends AbstractPlayerManager implements IGameManager {

    private final IGameSettings _settings;

    private boolean _isRunning = false;
    private boolean _isGameOver = false;
    private Date _startTime;

    /*
     * Constructor.
     */
    public PVGameManager(IArena arena) {
        super(arena);

        _settings = new PVGameSettings(arena);
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

        ILobbyManager lobbyManager = getArena().getLobbyManager();

        // get default next group of players from lobby
        IArenaPlayerCollection players = reason == ArenaStartReason.AUTO
                ? lobbyManager.getNextGroup()
                : lobbyManager.getReadyGroup();

        // create pre-start event
        ArenaPreStartEvent preStartEvent = new ArenaPreStartEvent(getArena(),
                new ArenaPlayerArrayList(players, true), reason);

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

        getArena().getSpawnManager().clearReserved();

        _startTime = null;

        getArena().getEventManager().call(this, new ArenaEndedEvent(getArena()));

        for (IArenaPlayer player : getPlayers()) {
            getArena().remove(player, RemovePlayerReason.GAME_ENDED);
        }

        return true;
    }

    @Override
    public boolean forwardPlayer(IArenaPlayer player, IArena nextArena) {
        PreCon.notNull(player);
        PreCon.notNull(nextArena);

        if (!nextArena.getSettings().isEnabled())
            return false;

        Result<Location> result = removePlayer(player, RemovePlayerReason.FORWARDING);
        if (!result.isSuccess())
            return false;

        PlayerPreJoinEvent preJoin = new PlayerPreJoinEvent(nextArena, player);

        nextArena.getEventManager().call(this, preJoin);

        if (preJoin.isCancelled())
            return false;

        boolean isAdded = nextArena.getGameManager().isRunning()
                ? nextArena.getGameManager().addPlayer(player, AddPlayerReason.FORWARDING)
                : nextArena.getLobbyManager().addPlayer(player, AddPlayerReason.FORWARDING);

        if (isAdded) {
            PlayerJoinedEvent joined = new PlayerJoinedEvent(nextArena, player, player.getRelatedManager());

            nextArena.getEventManager().call(this, joined);
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

        IArenaPlayerCollection winningTeam = new ArenaPlayerArrayList(getPlayerCount());

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

        IArenaPlayerCollection losingTeam = new ArenaPlayerArrayList(getPlayerCount());

        for (IArenaPlayer player : getPlayers()) {
            if (player.getTeam() == team) {
                losingTeam.add(player);
                removePlayer(player, RemovePlayerReason.LOSE);
            }
        }

        getArena().getEventManager().call(this,
                new TeamLoseEvent(getArena(), team, losingTeam, null));

        return true;
    }

    @Nullable
    @Override
    protected Location onRespawnPlayer(IArenaPlayer player) {

        // get random spawn for the team
        Spawnpoint spawnpoint = getArena().getSpawnManager().getRandomGameSpawn(player.getTeam());
        if (spawnpoint == null) {
            Msg.warning("Failed to find a game spawn for a player in arena '{0}'.", getArena().getName());
            return null;
        }
        return spawnpoint;
    }

    @Override
    @Nullable
    protected Location onAddPlayer(IArenaPlayer player, AddPlayerReason reason) {

        if (!getArena().getSpawnManager().hasLobbySpawns())
            return null;

        // get random spawn for the team
        Spawnpoint spawnpoint = getArena().getSpawnManager().getRandomGameSpawn(player.getTeam());
        if (spawnpoint == null) {
            Msg.warning("Failed to find a game spawn for a player in arena '{0}'.", getArena().getName());
            return null;
        }

        return spawnpoint;
    }

    @Override
    protected void onPreRemovePlayer(IArenaPlayer player, RemovePlayerReason reason) {

        if (reason == RemovePlayerReason.LOSE ||
                reason == RemovePlayerReason.KICK ||
                reason == RemovePlayerReason.LOGOUT) {

            callLoseEvent(player);
        }
    }

    @Override
    protected Location onRemovePlayer(IArenaPlayer player, RemovePlayerReason reason) {

        Scheduler.runTaskLater(PVStarAPI.getPlugin(), 1, new Runnable() {
            @Override
            public void run() {
                if (_players.size() == 0)
                    end();
            }
        });

        return getArena().getSettings().getRemoveLocation();
    }

    /*
     *  Transfer the next group of players from the lobby to here.
     */
    private boolean transferPlayersFromLobby(Collection<IArenaPlayer> players) {

        ILobbyManager lobbyManager = getArena().getLobbyManager();

        // transfer players from lobby
        for (IArenaPlayer player : players) {

            if (!lobbyManager.hasPlayer(player))
                continue;

            lobbyManager.removePlayer(player, RemovePlayerReason.ARENA_RELATION_CHANGE);

            addPlayer(player, AddPlayerReason.ARENA_RELATION_CHANGE);
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
