/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
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

package com.jcwhatever.pvs.scripting;

import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.pvs.api.arena.options.NameMatchMode;
import com.jcwhatever.pvs.api.arena.options.RemovePlayerReason;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public class PVStarScriptApi implements IDisposable {

    private Map<Arena, ArenaApiObject> _arenaApis = new HashMap<>(25);
    private boolean _isDisposed;

    public final PlayersApiObject players = new PlayersApiObject();

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {

        for (ArenaApiObject api : _arenaApis.values()) {
            api.dispose();
        }

        _arenaApis.clear();

        _isDisposed = true;
    }

    /**
     * Get an arena api by arena name.
     *
     * @param arenaName  The arena name.
     *
     * @return  Null if the arena is not found.
     */
    @Nullable
    public ArenaApiObject getArenaApi(String arenaName) {
        PreCon.notNullOrEmpty(arenaName);

        Arena arena = getArenaByName(arenaName);
        if (arena == null)
            return null;

        ArenaApiObject api = _arenaApis.get(arena);
        if (api != null)
            return api;

        api = new ArenaApiObject(arena);
        _arenaApis.put(arena, api);

        return api;
    }

    /**
     * Get an arena object by name.
     *
     * @param name  The name of the arena.
     */
    @Nullable
    public Arena getArenaByName(String name) {
        PreCon.notNullOrEmpty(name);

        List<Arena> arenas = PVStarAPI.getArenaManager().getArena(name, NameMatchMode.CASE_INSENSITIVE);
        return arenas.size() == 1 ? arenas.get(0) : null;
    }

    /**
     * Get an arena object by unique id.
     *
     * @param arenaId  The id of the arena.
     */
    @Nullable
    public Arena getArenaById(UUID arenaId) {
        PreCon.notNull(arenaId);

        return PVStarAPI.getArenaManager().getArena(arenaId);
    }

    /**
     * Get an arena object by location.
     *
     * @param location  The location of the arena.
     */
    @Nullable
    public Arena getArenaByLocation(Location location) {
        PreCon.notNull(location);

        return PVStarAPI.getArenaManager().getArena(location);
    }

    /**
     * Join a player to the specified arena.
     *
     * @param arena   The arena.
     * @param player  The player.
     *
     * @return  True if player joined.
     */
    public boolean join(Arena arena, Object player) {
        PreCon.notNull(arena);
        PreCon.notNull(player);

        ArenaPlayer p = PVStarAPI.getArenaPlayer(player);

        return arena.join(p);
    }

    /**
     * Remove a player from the specified arena.
     *
     * @param arena   The arena.
     * @param player  The player.
     *
     * @return  True if player was removed.
     */
    public boolean leave(Arena arena, Object player) {
        PreCon.notNull(arena);
        PreCon.notNull(player);

        ArenaPlayer p = PVStarAPI.getArenaPlayer(player);

        return arena.remove(p, RemovePlayerReason.PLAYER_LEAVE);
    }

    /**
     * Forward the player from their current arena to the specified
     * arena.
     *
     * @param toArena  The arena to forward to.
     * @param player   The player to forward.
     *
     * @return  True if the player was forwarded.
     */
    public boolean forward(Arena toArena, Object player) {
        PreCon.notNull(toArena);
        PreCon.notNull(player);


        ArenaPlayer p = PVStarAPI.getArenaPlayer(player);
        return p.getArena() != null &&
                p.getArena().getGameManager().forwardPlayer(p, toArena);
    }
}

