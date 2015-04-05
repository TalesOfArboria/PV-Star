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

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.pvs.api.arena.ArenaPlayerGroup;
import com.jcwhatever.pvs.api.arena.collections.ArenaPlayerCollection;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.api.utils.ArenaPlayerArrayList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PVArenaPlayerGroup implements ArenaPlayerGroup {

    private final Set<ArenaPlayer> _players = new HashSet<>(20);

    @Override
    public boolean hasPlayer(ArenaPlayer player) {
        PreCon.notNull(player);

        return _players.contains(player);
    }

    @Override
    public int size() {
        return _players.size();
    }

    @Override
    public int size(Arena arena) {
        return getPlayers(arena).size();
    }

    @Override
    public boolean isReady(Collection<ArenaPlayer> players) {
        PreCon.notNull(players);

        for (ArenaPlayer player : players) {
            if (!_players.contains(player))
                continue;

            if (!player.isReady())
                return false;
        }
        return true;
    }

    @Override
    public ArenaPlayerCollection filterPlayers(Collection<ArenaPlayer> players) {

        ArenaPlayerCollection result = new ArenaPlayerArrayList(players.size());

        for (ArenaPlayer player : players) {
            if (!_players.contains(player))
                continue;

            result.add(player);
        }

        return result;
    }

    @Override
    public void tell(String message, Object... params) {
        PreCon.notNullOrEmpty(message);
        PreCon.notNull(params);

        for (ArenaPlayer player : _players) {
            Msg.tell(player.getPlayer(), message, params);
        }
    }

    @Override
    public void addPlayer(ArenaPlayer player) {
        PreCon.isValid(player instanceof PVArenaPlayer);

        _players.add(player);

        // remove the player from their current group, if any
        ArenaPlayerGroup currentGroup = player.getPlayerGroup();
        if (currentGroup != null)
            currentGroup.removePlayer(player);

        player.setPlayerGroup(this);
    }

    @Override
    public void removePlayer(ArenaPlayer player) {
        PreCon.isValid(player instanceof PVArenaPlayer);

        _players.remove(player);

        // make sure the player is in the group before
        // setting player group to null on player
        ArenaPlayerGroup currentGroup = player.getPlayerGroup();
        if (currentGroup != null && !currentGroup.equals(this))
            return;

        player.setPlayerGroup(null);
    }

    @Override
    public ArenaPlayerCollection getPlayers() {
        return new ArenaPlayerArrayList(_players, false);
    }

    @Override
    public ArenaPlayerCollection getPlayers(Arena arena) {
        PreCon.notNull(arena);

        ArenaPlayerCollection result = new ArenaPlayerArrayList(_players.size());

        for (ArenaPlayer player : _players) {
            if (arena.equals(player.getArena())) {
                result.add(player);
            }
        }
        return result;
    }
}
