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


package com.jcwhatever.bukkit.pvs.arenas.managers;

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Result;
import com.jcwhatever.nucleus.utils.Scheduler;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaTeam;
import com.jcwhatever.bukkit.pvs.api.arena.managers.GameManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.LobbyManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.ArenaStartReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.settings.GameManagerSettings;
import com.jcwhatever.bukkit.pvs.api.events.ArenaEndedEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaPreStartEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaStartedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerJoinedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerLoseEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerPreJoinEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerWinEvent;
import com.jcwhatever.bukkit.pvs.api.events.team.TeamLoseEvent;
import com.jcwhatever.bukkit.pvs.api.events.team.TeamWinEvent;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.arenas.settings.PVGameSettings;

import org.bukkit.Location;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Item;
import org.bukkit.entity.Projectile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Game manager implementation
 */
public class PVGameManager extends AbstractPlayerManager implements GameManager {

    private final GameManagerSettings _settings;
    private boolean _isRunning = false;
    private boolean _isGameOver = false;
    private Date _startTime;

    /*
     * Constructor.
     */
    public PVGameManager(Arena arena) {
        super(arena);

        _settings = new PVGameSettings(arena);
    }

    /*
     * Get the start time of the most recent game.
     */
    @Override
    @Nullable
    public Date getStartTime() {
        return _startTime;
    }

    /*
     * Determine if the game is running.
     */
    @Override
    public boolean isRunning() {
        return _isRunning;
    }

    /*
     * Determine if the game is over but still running.
     * Should always return false if isRunning() returns false.
     */
    @Override
    public boolean isGameOver() {
        return _isGameOver;
    }

    /*
     * Get game manager settings.
     */
    @Override
    public GameManagerSettings getSettings() {
        return _settings;
    }

    /*
     * Determine if the arena can be started.
     */
    @Override
    public final boolean canStart() {
        return getArena().getSettings().isEnabled() &&
                !getArena().isBusy();
    }

    /*
     * Start the game. Transfers the next group of players from the lobby.
     */
    @Override
    public final boolean start(ArenaStartReason reason) {

        if (!canStart())
            return false;

        _startTime = new Date();

        LobbyManager lobbyManager = getArena().getLobbyManager();

        // get default next group of players from lobby
        List<ArenaPlayer> players = reason == ArenaStartReason.AUTO
                ? lobbyManager.getNextGroup()
                : lobbyManager.getReadyGroup();

        // create pre-start event
        ArenaPreStartEvent preStartEvent = new ArenaPreStartEvent(getArena(), new HashSet<>(players), reason);

        // call pre-start event
        if (getArena().getEventManager().call(preStartEvent).isCancelled())
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
        getArena().getEventManager().call(
                new ArenaStartedEvent(getArena(), reason));

        return true;
    }


    /*
     * End the current game.
     */
    @Override
    public boolean end() {

        if (!isRunning())
            return false;

        _isRunning = false;
        _isGameOver = false;

        getArena().getSpawnManager().clearReserved();

        _startTime = null;

        if (getSettings().hasPostGameEntityCleanup()) {
            getArena().getRegion().removeEntities(Projectile.class, Explosive.class, Item.class);
        }

        getArena().getEventManager().call(new ArenaEndedEvent(getArena()));

        return true;
    }

    /*
     * Forward a player in the current game to another arena.
     */
    @Override
    public boolean forwardPlayer(ArenaPlayer player, Arena nextArena) {
        PreCon.notNull(player);
        PreCon.notNull(nextArena);

        if (!nextArena.getSettings().isEnabled())
            return false;

        Result<Location> result = removePlayer(player, RemovePlayerReason.FORWARDING);
        if (!result.isSuccess())
            return false;

        PlayerPreJoinEvent preJoin = new PlayerPreJoinEvent(nextArena, player);

        nextArena.getEventManager().call(preJoin);

        if (preJoin.isCancelled())
            return false;

        boolean isAdded = nextArena.getGameManager().isRunning()
                ? nextArena.getGameManager().addPlayer(player, AddPlayerReason.FORWARDING)
                : nextArena.getLobbyManager().addPlayer(player, AddPlayerReason.FORWARDING);

        if (isAdded) {
            PlayerJoinedEvent joined = new PlayerJoinedEvent(nextArena, player, player.getRelatedManager());

            nextArena.getEventManager().call(joined);
        }

        return true;
    }

    /*
     * Declare a winner.
     */
    @Override
    public boolean setWinner(ArenaPlayer player) {
        PreCon.notNull(player);

        if (!isRunning())
            return false;

        if (player.getTeam() == ArenaTeam.NONE) {
            for (ArenaPlayer otherPlayer : getPlayers()) {
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

    /*
     * Declare a team as the winner.
     */
    @Override
    public boolean setWinner(ArenaTeam team) {
        PreCon.notNull(team);

        if (!isRunning())
            return false;

        List<ArenaPlayer> winningTeam = new ArrayList<>(getPlayerCount());

        for (ArenaPlayer player : getPlayers()) {
            if (player.getTeam() == team) {
                winningTeam.add(player);
                callWinEvent(player);
            }
            else {
                callLoseEvent(player);
            }
        }

        TeamWinEvent winEvent = new TeamWinEvent(getArena(), team, winningTeam, null);

        getArena().getEventManager().call(winEvent);

        if (winEvent.getWinMessage() != null)
            tell(winEvent.getWinMessage());

        end();
        return true;
    }

    /*
     * Declare a losing team.
     */
    @Override
    public boolean setLoser(ArenaTeam team) {

        if (!isRunning())
            return false;

        List<ArenaPlayer> losingTeam = new ArrayList<>(getPlayerCount());

        for (ArenaPlayer player : getPlayers()) {
            if (player.getTeam() == team) {
                losingTeam.add(player);
                removePlayer(player, RemovePlayerReason.LOSE);
            }
        }

        getArena().getEventManager().call(
                new TeamLoseEvent(getArena(), team, losingTeam, null));

        return true;
    }

    /*
     * Called to get the respawn location for a player.
     */
    @Nullable
    @Override
    protected Location onRespawnPlayer(ArenaPlayer player) {

        // get random spawn for the team
        Spawnpoint spawnpoint = getArena().getSpawnManager().getRandomGameSpawn(player.getTeam());
        if (spawnpoint == null) {
            Msg.warning("Failed to find a game spawn for a player in arena '{0}'.", getArena().getName());
            return null;
        }
        return spawnpoint;
    }

    /*
     * Called to get the spawn location for an added player.
     */
    @Override
    @Nullable
    protected Location onAddPlayer(ArenaPlayer player, AddPlayerReason reason) {

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

    /*
     * Called before a player is removed.
     */
    @Override
    protected void onPreRemovePlayer(ArenaPlayer player, RemovePlayerReason reason) {

        if (reason == RemovePlayerReason.LOSE ||
                reason == RemovePlayerReason.KICK ||
                reason == RemovePlayerReason.LOGOUT) {

            callLoseEvent(player);
        }
    }

    /*
     * Called after a player is removed in order to get a location the
     * removed player should be sent.
     */
    @Override
    protected Location onRemovePlayer(ArenaPlayer player, RemovePlayerReason reason) {

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
    private boolean transferPlayersFromLobby(Collection<ArenaPlayer> players) {

        LobbyManager lobbyManager = getArena().getLobbyManager();

        // transfer players from lobby
        for (ArenaPlayer player : players) {

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
    private void callWinEvent(ArenaPlayer player) {
        PlayerWinEvent event = new PlayerWinEvent(getArena(), player, this, null);
        getArena().getEventManager().call(event);

        if (event.getWinMessage() != null)
            tell(event.getWinMessage());
    }


    /*
     * Call PlayerLoseEvent and display message if any
     */
    private void callLoseEvent(ArenaPlayer player) {
        PlayerLoseEvent event = new PlayerLoseEvent(getArena(), player, this, null);
        getArena().getEventManager().call(event);

        if (event.getLoseMessage() != null)
            tell(event.getLoseMessage());
    }


}
