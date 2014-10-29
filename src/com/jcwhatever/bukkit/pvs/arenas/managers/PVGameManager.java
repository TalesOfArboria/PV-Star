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


package com.jcwhatever.bukkit.pvs.arenas.managers;

import com.jcwhatever.bukkit.generic.events.GenericsEventHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventListener;
import com.jcwhatever.bukkit.generic.regions.BuildMethod;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.Scheduler;
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
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerLoseEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerRemovedEvent;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Game manager implementation
 */
public class PVGameManager extends AbstractPlayerManager implements GameManager, GenericsEventListener {

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
        getArena().getEventManager().register(this);
    }

    /*
     * Get the start time of the last game.
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

        if (getArena().getEventManager().call(new ArenaPreStartEvent(getArena(), reason)).isCancelled())
            return false;

        if (!transferPlayersFromLobby(reason))
            return false;

        _isRunning = true;
        _isGameOver = false;

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

        getArena().getEventManager().call(new ArenaEndedEvent(getArena()));

        _startTime = null;

        if (getSettings().hasPostGameEntityCleanup()) {
            getArena().getRegion().removeEntities(Projectile.class, Explosive.class, Item.class);
        }

        if (getArena().getSettings().isAutoRestoreEnabled()) {
            try {
                getArena().getRegion().restoreData(BuildMethod.PERFORMANCE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /*
     * Forward a player in the current game to another arena.
     */
    @Override
    public boolean forwardPlayer(ArenaPlayer player, Arena nextArena) {
        PreCon.notNull(player);
        PreCon.notNull(nextArena);

        return removePlayer(player, RemovePlayerReason.FORWARDING) &&
                nextArena.join(player, AddPlayerReason.FORWARDING);
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
                    callLoseEvent(player, null);
                }
            }
        } else {
            return setWinner(player.getTeam());
        }

        callWinEvent(player, player.getTeam().getTextColor() + player.getName() + " wins!");

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
                callWinEvent(player, null);
            }
            else {
                callLoseEvent(player, null);
            }
        }

        TeamWinEvent winEvent = new TeamWinEvent(getArena(), team, winningTeam,
                team.getTextColor() + team.getDisplay() + " wins!");

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

            String message = player.getTeam().getTextColor() + player.getName() + "{RED} is dead.";

            callLoseEvent(player, message);
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
     * Checks to see if a player should be declared the winner
     * after the specified player is removed.
     */
    @Nullable
    private ArenaPlayer checkForWinnerOnRemove(ArenaPlayer removedPlayer) {
        PreCon.notNull(removedPlayer);

        if (getPlayers().size() == 1) {
            ArenaPlayer winner = getPlayers().get(0);
            if (!winner.equals(removedPlayer)) {
                return winner;
            }
        }
        return null;
    }

    /*
     * Check for a winner when a player is removed.
     */
    @GenericsEventHandler // TODO : Not all game types will declare a winner with only 1 player left.
    private void onCheckForWinner(PlayerRemovedEvent event) {

        if (event.getReason() == RemovePlayerReason.ARENA_RELATION_CHANGE ||
                event.getReason() == RemovePlayerReason.FORWARDING)
            return;

        if (isRunning() && !isGameOver()) {

            // Check for winner
            ArenaPlayer winner = checkForWinnerOnRemove(event.getPlayer());
            if (winner != null) {
                setWinner(winner);
                end();
                return; // finish
            }
        }

        if (getPlayers().size() == 0) {
            end();
        }
    }

    /*
     *  Transfer the next group of players from the lobby to here.
     */
    private boolean transferPlayersFromLobby(ArenaStartReason reason) {

        LobbyManager lobbyManager = getArena().getLobbyManager();

        Collection<ArenaPlayer> players = reason == ArenaStartReason.AUTO
                ? lobbyManager.getNextGroup()
                : lobbyManager.getReadyGroup();

        if (players == null || players.isEmpty())
            return false;

        // transfer players from lobby
        for (ArenaPlayer player : players) {

            if (!lobbyManager.removePlayer(player, RemovePlayerReason.ARENA_RELATION_CHANGE))
                continue;

            addPlayer(player, AddPlayerReason.ARENA_RELATION_CHANGE);
        }

        return true;
    }

    /*
     * Call PlayerWinEvent and display message if any
     */
    private void callWinEvent(ArenaPlayer player, @Nullable String message) {
        PlayerWinEvent event = new PlayerWinEvent(getArena(), player, message);
        getArena().getEventManager().call(event);

        if (event.getWinMessage() != null)
            tell(event.getWinMessage());
    }


    /*
     * Call PlayerLoseEvent and display message if any
     */
    private void callLoseEvent(ArenaPlayer player, @Nullable String message) {
        PlayerLoseEvent event = new PlayerLoseEvent(getArena(), player, message);
        getArena().getEventManager().call(event);

        if (event.getLoseMessage() != null)
            tell(event.getLoseMessage());
    }


}
