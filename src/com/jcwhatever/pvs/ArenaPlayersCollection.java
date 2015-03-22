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


import com.jcwhatever.nucleus.utils.performance.EntryCache;
import com.jcwhatever.nucleus.utils.CollectionUtils;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.pvs.api.arena.ArenaPlayerGroup;
import com.jcwhatever.pvs.api.arena.ArenaTeam;
import com.jcwhatever.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.pvs.api.exceptions.PlayerGroupExpectedException;
import com.jcwhatever.pvs.api.utils.Msg;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * {@link ArenaPlayerGroup} collection.
 */
public class ArenaPlayersCollection {

    private final Arena _arena;
    private final Set<ArenaPlayerGroup> _groups = new HashSet<>(5);
    private final Set<ArenaPlayer> _players = new HashSet<>(30);
    private final EntryCache<Integer, List<ArenaPlayer>> _cachedNextGroup = new EntryCache<>();

    private List<ArenaPlayer> _cachedReadyGroup;

    /*
     * Constructor.
     */
    public ArenaPlayersCollection(Arena arena) {
        _arena = arena;
    }

    /*
     * Get the total number of players in the collection.
     */
    public int size() {
        return _players.size();
    }

    /*
     * Add player as well as the players group, if any.
     *
     * If the player is not in a group, the player is added to one
     * or a new one is created.
     */
    public void addPlayer(ArenaPlayer player) {
        PreCon.notNull(player);

        _cachedNextGroup.reset();
        _cachedReadyGroup = null;
        _players.add(player);

        if (player.getPlayerGroup() != null) {

            // add existing player group
            _groups.add(player.getPlayerGroup());
        }
        else {

            // Add new player to an existing group if available
            for (ArenaPlayerGroup group : _groups) {
                if (group.size() < _arena.getSettings().getMaxPlayers()) {
                    group.addPlayer(player);
                    return;
                }
            }

            // add new player to new group
            ArenaPlayerGroup group = new PVArenaPlayerGroup();
            group.addPlayer(player);
            _groups.add(group);
        }
    }

    /*
     * Remove a player from the collection.
     *
     * If no more player from the removed players group are present, the players
     * group is also removed.
     */
    public void removePlayer(ArenaPlayer player, RemovePlayerReason reason) {
        PreCon.notNull(player);

        _cachedNextGroup.reset();
        _cachedReadyGroup = null;

        _players.remove(player);

        ArenaPlayerGroup group = player.getPlayerGroup();
        if (group == null) {
            throw new PlayerGroupExpectedException();
        }

        if (reason != RemovePlayerReason.FORWARDING &&
                reason != RemovePlayerReason.ARENA_RELATION_CHANGE) {
            group.removePlayer(player);
        }

        List<ArenaPlayer> groupPlayers = group.getPlayers();

        // check to see if any other players are in the group
        for (ArenaPlayer groupPlayer : groupPlayers) {
            if (_players.contains(groupPlayer)) {
                return; // finished
            }
        }

        // no players in group are in the collection so remove group
        _groups.remove(group);
    }

    /*
     * Determine if a player is in the collection.
     */
    public boolean hasPlayer(ArenaPlayer player) {
        PreCon.notNull(player);

        return _players.contains(player);
    }

    /*
     * Tell all players in the collection a message.
     */
    public void tell(String message, Object... params) {
        PreCon.notNullOrEmpty(message);

        for (ArenaPlayer player : _players) {
            Msg.tell(player.getPlayer(), message, params);
        }
    }

    /*
     * Get all player groups in the collection.
     */
    public Set<ArenaPlayerGroup> getGroups() {
        return CollectionUtils.unmodifiableSet(_groups);
    }

    /*
     * Get all players in the collection.
     */
    public List<ArenaPlayer> getPlayers() {
        return new ArrayList<>(_players);
    }

    /*
     * Get all players in the group collection who are
     * in the PlayerGroups arena and who are on the specified
     * team.
     */
    public List<ArenaPlayer> getTeam(ArenaTeam team) {
        PreCon.notNull(team);

        List<ArenaPlayer> results = new ArrayList<>(20);

        for (ArenaPlayer player : _players) {
            if (player.getTeam() == team) {
                results.add(player);
            }
        }
        return results;
    }

    /*
     * Get all teams in the group collection who are
     * in the PlayerGroups arena.
     */
    public Set<ArenaTeam> getTeams() {
        Set<ArenaTeam> results = EnumSet.noneOf(ArenaTeam.class);

        for (ArenaPlayer player : _players) {
            results.add(player.getTeam());
        }
        return results;
    }

    /*
     * Get a group from the group collection whose players
     * that are in the PlayerGroups arena are all ready.
     */
    public List<ArenaPlayer> getReadyGroup() {

        if (_cachedReadyGroup != null)
            return _cachedReadyGroup;

        for (ArenaPlayerGroup group : _groups) {
            if (group.isReady(_players)) {
                _cachedReadyGroup = group.filterPlayers(_players);
                return _cachedReadyGroup;
            }
        }

        return new ArrayList<>(0);
    }

    /*
     * Get a group from the group collection whose players
     * that are in the PlayerGroups arena are all ready and
     * the number of ready players meets a minimum amount.
     */
    public List<ArenaPlayer> getReadyGroup(int minGroupSize) {

        if (_cachedReadyGroup != null)
            return _cachedReadyGroup;

        for (ArenaPlayerGroup group : _groups) {
            if (group.isReady(_players) && group.size(_arena) >= minGroupSize) {

                _cachedReadyGroup = group.filterPlayers(_players);
                return _cachedReadyGroup;

            }

        }
        return new ArrayList<>(0);
    }

    /*
     * Determine if there is a group that is ready to
     * play in the PlayerGroups arena.
     */
    public boolean hasReadyGroup() {
        return getReadyGroup() != null;
    }

    /*
     * Determine if there is a group that is ready to
     * play in the PlayerGroups arena who meets the minimum
     * size specified.
     */
    public boolean hasReadyGroup(int minGroupSize) {
        return getReadyGroup(minGroupSize) != null;
    }

    /*
     * Get the next group to play in the PlayerGroups arena who has players in the lobby
     * and meets the minimum number specified.
     *
     * If no valid group is found, a ready group that does not meet the minimum amount is selected.
     */
    @Nullable
    public List<ArenaPlayer> getNextGroup(int minSize) {

        if (_cachedNextGroup.keyEquals(minSize))
            return _cachedNextGroup.getValue();

        for (ArenaPlayerGroup group : _groups) {

            // find a group with  players who meet the minimum group size
            List<ArenaPlayer> players = group.filterPlayers(_players);
            if (players.size() >= minSize) {
                _cachedNextGroup.set(minSize, players);
                return new ArrayList<>(players);
            }
        }

        // return a ready group regardless of size
        // to ensure groups that get split into smaller
        // groups across multiple arenas can still play.
        List<ArenaPlayer> result = getReadyGroup();
        _cachedNextGroup.set(minSize, result);
        return result;
    }
}
