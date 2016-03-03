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

package com.jcwhatever.pvs.players;

import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.IBukkitPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/*
 * 
 */
public class BukkitPlayer extends ArenaPlayer implements IBukkitPlayer {

    private final Player _player;

    /**
     * Private Constructor.
     *
     * @param player The player.
     */
    BukkitPlayer(Player player) {
        _player = player;
    }

    @Override
    public Player getPlayer() {
        return _player;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return _player;
    }

    @Override
    public UUID getUniqueId() {
        return _player.getUniqueId();
    }

    @Override
    public String getName() {
        return _player.getName();
    }

    @Override
    public String getDisplayName() {
        return _player.getDisplayName() != null
                ? _player.getDisplayName()
                : _player.getName();
    }

    @Override
    public Location getLocation() {
        return _player.getLocation();
    }

    @Override
    public Location getLocation(Location output) {
        return _player.getLocation(output);
    }

    @Override
    public boolean isDead() {
        return _player.isDead();
    }

    @Override
    public boolean isOnline() {
        return _player.isOnline();
    }

    @Override
    public double getHealth() {
        return _player.getHealth();
    }

    @Override
    public void setHealth(double health) {
        _player.setHealth(health);
    }

    @Override
    public double getMaxHealth() {
        return _player.getMaxHealth();
    }

    @Override
    public void setMaxHealth(double maxHealth) {
        _player.setMaxHealth(maxHealth);
    }

    @Override
    public void kill() {
        _player.damage(_player.getMaxHealth());
    }

    @Override
    public void kill(@Nullable IArenaPlayer blame) {
        _deathBlamePlayer = blame;
        _player.damage(_player.getMaxHealth());
    }
}
