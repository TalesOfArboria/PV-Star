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
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.IArenaPlayerGroup;
import com.jcwhatever.pvs.api.arena.collections.IArenaPlayerCollection;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.api.utils.ArenaPlayerArrayList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * PVStar implementation of {@link IArenaPlayerGroup}.
 */
public class PVArenaPlayerGroup implements IArenaPlayerGroup {

    private final Set<IArenaPlayer> _players = new HashSet<>(20);

    @Override
    public boolean hasPlayer(IArenaPlayer player) {
        PreCon.notNull(player);

        return _players.contains(player);
    }

    @Override
    public int size() {
        return _players.size();
    }

    @Override
    public int size(IArena arena) {
        return getPlayers(arena).size();
    }

    @Override
    public boolean isReady(Collection<IArenaPlayer> players) {
        PreCon.notNull(players);

        for (IArenaPlayer player : players) {
            if (!_players.contains(player))
                continue;

            if (!player.isReady())
                return false;
        }
        return true;
    }

    @Override
    public IArenaPlayerCollection filterPlayers(Collection<IArenaPlayer> players) {

        IArenaPlayerCollection result = new ArenaPlayerArrayList(players.size());

        for (IArenaPlayer player : players) {
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

        for (IArenaPlayer player : _players) {
            Msg.tell(player.getPlayer(), message, params);
        }
    }

    @Override
    public void addPlayer(IArenaPlayer player) {
        PreCon.isValid(player instanceof PVArenaPlayer);

        _players.add(player);

        // remove the player from their current group, if any
        IArenaPlayerGroup currentGroup = player.getPlayerGroup();
        if (currentGroup != null)
            currentGroup.removePlayer(player);

        ((PVArenaPlayer)player).setPlayerGroup(this);
    }

    @Override
    public void removePlayer(IArenaPlayer player) {
        PreCon.isValid(player instanceof PVArenaPlayer);

        _players.remove(player);

        // make sure the player is in the group before
        // setting player group to null on player
        IArenaPlayerGroup currentGroup = player.getPlayerGroup();
        if (currentGroup != null && !currentGroup.equals(this))
            return;

        ((PVArenaPlayer)player).setPlayerGroup(null);
    }

    @Override
    public IArenaPlayerCollection getPlayers() {
        return new ArenaPlayerArrayList(_players, false);
    }

    @Override
    public IArenaPlayerCollection getPlayers(IArena arena) {
        PreCon.notNull(arena);

        IArenaPlayerCollection result = new ArenaPlayerArrayList(_players.size());

        for (IArenaPlayer player : _players) {
            if (arena.equals(player.getArena())) {
                result.add(player);
            }
        }
        return result;
    }
}
