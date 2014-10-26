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
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.Scheduler;
import com.jcwhatever.bukkit.generic.utils.Scheduler.ScheduledTask;
import com.jcwhatever.bukkit.generic.utils.Scheduler.TaskHandler;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.managers.GameManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.LobbyManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.ArenaStartReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.settings.LobbyManagerSettings;
import com.jcwhatever.bukkit.pvs.api.events.ArenaCountdownPreStartEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaCountdownStartedEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerReadyEvent;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.arenas.settings.PVLobbySettings;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.List;

public class PVLobbyManager extends AbstractPlayerManager implements LobbyManager, GenericsEventListener {

    @Localizable static final String _JOINED = "{0} has joined.";
    @Localizable static final String _STARTING_COUNTDOWN = "Starting in {0} seconds...";
    @Localizable static final String _MOD_10_SECONDS = "{0} seconds...";
    @Localizable static final String _SECONDS = "{0}...";
    @Localizable static final String _GO = "{GREEN}Go!";
    @Localizable static final String _AUTO_START_INFO =
            "{YELLOW}Countdown to start will begin once {0} or more players " +
                    "have joined. Type '/pv vote' if you would like to start the countdown now. All players " +
                    "must vote in order to start the countdown early.";


    private ScheduledTask _countdownTask;
    private LobbyManagerSettings _settings;

    /*
     * Constructor.
     */
    public PVLobbyManager(Arena arena) {
        super(arena);

        _settings = new PVLobbySettings(arena);
        arena.getEventManager().register(this);
    }

    /*
     * Determine if the countdown till the next game is running.
     */
    @Override
    public boolean isCountdownRunning() {
        return _countdownTask != null && !_countdownTask.isCancelled();
    }

    /*
     * Cancel the countdown.
     */
    @Override
    public void cancelCountdown() {
        if (!isCountdownRunning())
            return;

        _countdownTask.cancel();
    }

    /*
     * Get lobby settings.
     */
    @Override
    public LobbyManagerSettings getSettings() {
        return _settings;
    }

    /*
     * Get the next group of players that are all ready.
     */
    @Override
    @Nullable
    public List<ArenaPlayer> getReadyGroup() {

        return _players.getReadyGroup(getArena().getSettings().getMinPlayers());
    }

    /*
     * Get the next group of players that meat minimum players
     * and minimum auto start players settings.
     */
    @Override
    @Nullable
    public List<ArenaPlayer> getNextGroup() {

        int minSize = Math.max(
                getArena().getSettings().getMinPlayers(),
                getSettings().getMinAutoStartPlayers());

        return _players.getNextGroup(minSize);
    }

    /*
     * Called to get a respawn location.
     */
    @Nullable
    @Override
    protected Location onRespawnPlayer(ArenaPlayer player) {
        return getSpawnLocation(player);
    }

    /*
     * Called to get a spawn location on a player added to the arena.
     */
    @Override
    protected Location onAddPlayer(ArenaPlayer player, AddPlayerReason reason) {

        tell(Lang.get(_JOINED), player.getName());

        if (getSettings().hasAutoStart() &&
                !isCountdownRunning() &&
                !tryAutoStart()) {

            Msg.tell(player, Lang.get(_AUTO_START_INFO, getSettings().getMinAutoStartPlayers()));
        }

        return getSpawnLocation(player);
    }

    /*
     * Called before a player is removed from the lobby.
     */
    @Override
    protected void onPreRemovePlayer(ArenaPlayer player, RemovePlayerReason reason) {
        // do nothing
    }

    /*
     * Called after a player is removed from the lobby in
     * order to get a location to send the player.
     */
    @Override
    protected Location onRemovePlayer(ArenaPlayer player, RemovePlayerReason reason) {
        return getArena().getSettings().getRemoveLocation();
    }

    /*
     * Attempt to start a game automatically.
     */
    private boolean tryAutoStart() {

        GameManager gameManager = getArena().getGameManager();

        // make sure game isn't already running
        if (gameManager.isRunning())
            return false;

        // make sure arena isn't busy
        if (getArena().isBusy())
            return false;

        // make sure arena has auto start enabled
        // and count down isn't already running.
        if (getSettings().hasAutoStart() && !isCountdownRunning()) {


            // get the next group
            List<ArenaPlayer> nextGroup = getNextGroup();

            // group not found
            if (nextGroup == null || nextGroup.isEmpty())
                return false;

            // make sure group meets the minimum auto start player size setting
            if (nextGroup.size() < getSettings().getMinAutoStartPlayers())
                return false;

            // start the countdown
            startCountdown(ArenaStartReason.AUTO);
            return true;
        }

        return false;
    }

    /*
     * Attempt to start a game for ready players.
     */
    private boolean tryReadyStart() {

        // make sure game isn't running
        if (getArena().getGameManager().isRunning())
            return false;

        // make sure arena isn't busy
        if (getArena().isBusy())
            return false;

        // make sure the countdown isn't already running
        if (isCountdownRunning())
            return false;

        // check to see if there is a group that is ready
        List<ArenaPlayer> ready = getArena().getLobbyManager().getReadyGroup();
        if (ready == null || ready.isEmpty())
            return false;

        // make sure the size of the group meets the min
        // players requirement
        if (ready.size() < getArena().getSettings().getMinPlayers())
            return false;

        startCountdown(ArenaStartReason.PLAYERS_READY);
        return true;
    }

    /*
     * Begin the game start countdown.
     */
    private void startCountdown(final ArenaStartReason reason) {

        final GameManager gameManager = getArena().getGameManager();

        // make sure countdown isn't already running and
        // the game isn't in progress.
        if (isCountdownRunning() || gameManager.isRunning())
            return;

        if (getArena().getEventManager().call(new ArenaCountdownPreStartEvent(getArena())).isCancelled())
            return;

        // get players to send to the game.
        List<ArenaPlayer> players = reason == ArenaStartReason.AUTO
                ? getNextGroup()
                : getReadyGroup();

        // don't start if there are no players
        if (players == null || players.isEmpty())
            return;

        // tell players the countdown is starting.
        Msg.tell(players, Lang.get(_STARTING_COUNTDOWN, getSettings().getStartCountdownSeconds()));

        // schedule countdown task
        _countdownTask = Scheduler.runTaskRepeat(PVStarAPI.getPlugin(), 20, 20, new TaskHandler() {

            private int elapsedSeconds = 0;

            @Override
            public void run() {

                elapsedSeconds++;

                long remaining = getSettings().getStartCountdownSeconds() - elapsedSeconds;
                List<ArenaPlayer> group = reason == ArenaStartReason.AUTO
                        ? getNextGroup()
                        : getReadyGroup();

                // cancel countdown if there is no longer a group of players to start the game
                if (group == null || group.isEmpty()) {
                    cancelTask();
                }
                // cancel countdown task once countdown is completed.
                else if (remaining <= 0) {
                    cancelTask();
                    getArena().getGameManager().start(reason);
                    Msg.tell(group, Lang.get(_GO));
                }
                // tell current time left at 10 seconds intervals
                else if (remaining > 5) {

                    if (remaining % 10 == 0) {
                        Msg.tell(group, Lang.get(_MOD_10_SECONDS, remaining));
                    }

                }
                // tell current time left at 1 seconds intervals
                else {
                    Msg.tell(group, Lang.get(_SECONDS, remaining));
                }
            }

        });

        // call countdown started event
        getArena().getEventManager().call(new ArenaCountdownStartedEvent(getArena()));
    }


    /*
     * Get a lobby spawn location for a player.
     */
    private Location getSpawnLocation(ArenaPlayer player) {
        Spawnpoint spawnpoint = getArena().getSpawnManager().getRandomLobbySpawn(player.getTeam());
        if (spawnpoint == null) {

            spawnpoint = getArena().getSpawnManager().getRandomGameSpawn(player.getTeam());
            if (spawnpoint == null) {

                Msg.warning("Failed to find a lobby spawn for a player in arena '{0}'.", getArena().getName());
                return null;
            }
        }

        return spawnpoint;
    }


    // try ready start when a player is ready
    @GenericsEventHandler
    private void onPlayerReady(PlayerReadyEvent event) {

        tell(event.getPlayer().getTeam().getTextColor() + event.getPlayer().getName() + "{WHITE} is ready to start.");

        tryReadyStart();
    }
}
