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

import com.jcwhatever.nucleus.managed.sounds.Sounds;
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import com.jcwhatever.pvs.api.arena.IArena;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Script Api to handle sending sound effects to players in the arena.
 */
public class ArenaSoundsApiObject  implements IDisposable {

    private static final Location LOCATION = new Location(null, 0, 0, 0);
    private static final List<Player> PLAYERS = new ArrayList<>(10);

    private final IArena _arena;

    private boolean _isDisposed;

    public ArenaSoundsApiObject(IArena arena) {
        _arena = arena;
    }

    /**
     * Play a sound effect at the location.
     *
     * <p>Plays to all players within range who are in some way part of the arena.</p>
     *
     * @param locationObj  The location of the object.
     * @param soundName    The name of the sound to play.
     */
    public void playEffect(Object locationObj, String soundName) {
        PreCon.notNull(locationObj);
        PreCon.notNullOrEmpty(soundName);

        _arena.getGame().getPlayers().toBukkit(PLAYERS);
        _arena.getLobby().getPlayers().toBukkit(PLAYERS);
        _arena.getSpectators().getPlayers().toBukkit(PLAYERS);

        playEffect(locationObj, soundName, PLAYERS);
        PLAYERS.clear();
    }

    /**
     * Play a sound effect at the location of the specified location.
     *
     * <p>Plays to all players within range who are in game or are spectators.</p>
     *
     * @param locationObj  The location of the object.
     * @param soundName    The name of the sound to play.
     */
    public void playGameEffect(Object locationObj, String soundName) {
        PreCon.notNull(locationObj);
        PreCon.notNullOrEmpty(soundName);

        _arena.getGame().getPlayers().toBukkit(PLAYERS);
        _arena.getSpectators().getPlayers().toBukkit(PLAYERS);

        playEffect(locationObj, soundName, PLAYERS);
        PLAYERS.clear();
    }

    /**
     * Play a sound effect at the location of the specified location.
     *
     * <p>Plays to all players within range who are in the lobby or are spectators.</p>
     *
     * @param locationObj  The location of the object.
     * @param soundName    The name of the sound to play.
     */
    public void playLobbyEffect(Object locationObj, String soundName) {
        PreCon.notNull(locationObj);
        PreCon.notNullOrEmpty(soundName);

        _arena.getGame().getPlayers().toBukkit(PLAYERS);
        _arena.getSpectators().getPlayers().toBukkit(PLAYERS);

        playEffect(locationObj, soundName, PLAYERS);
        PLAYERS.clear();
    }

    /**
     * Play a sound effect at the location of the specified location.
     *
     * <p>Plays to all players within range who are spectators.</p>
     *
     * @param locationObj  The location of the object.
     * @param soundName    The name of the sound to play.
     */
    public void playSpectatorEffect(Object locationObj, String soundName) {
        PreCon.notNull(locationObj);
        PreCon.notNullOrEmpty(soundName);

        playEffect(locationObj, soundName, _arena.getGame().getPlayers().toBukkit(PLAYERS));
        PLAYERS.clear();
    }

    private void playEffect(Object locationObj, String soundName, Collection<Player> players) {

        Location location = null;

        if (locationObj instanceof Location) {
            location = (Location)locationObj;
        }
        else {
            Player player = PlayerUtils.getPlayer(locationObj);
            if (player != null) {
                location = player.getLocation(LOCATION);
            }
            else if (locationObj instanceof Entity) {
                location = ((Entity) locationObj).getLocation(LOCATION);
            }
        }

        if (location == null) {
            throw new IllegalArgumentException("Invalid location object.");
        }

        Sounds.playEffect(soundName, players, location);
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {
        _isDisposed = true;
    }
}
