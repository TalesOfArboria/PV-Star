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

import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.managed.teleport.TeleportMode;
import com.jcwhatever.nucleus.managed.teleport.Teleporter;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.events.NpcDeathEvent;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.INpcPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.util.UUID;

/*
 * 
 */
public class NpcPlayer extends ArenaPlayer implements INpcPlayer {

    private final INpc _npc;
    private boolean _isDisposed;

    NpcPlayer(INpc npc) {
        if (npc.isDisposed())
            throw new IllegalArgumentException("Cannot use a disposed npc.");

        _npc = npc;
    }

    @Override
    public INpc getNpc() {
        return _npc;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        checkDisposed();
        return _npc.getEntity();
    }

    @Override
    public UUID getUniqueId() {
        checkDisposed();
        return _npc.getId();
    }

    @Override
    public String getName() {
        checkDisposed();
        return _npc.getDisplayName();
    }

    @Override
    public String getDisplayName() {
        checkDisposed();
        return _npc.getDisplayName();
    }

    @Override
    public Location getLocation() {
        checkDisposed();
        return _npc.getLocation();
    }

    @Override
    public Location getLocation(Location output) {
        checkDisposed();
        return _npc.getLocation(output);
    }

    @Override
    public boolean isDead() {
        checkDisposed();
        return !_npc.isSpawned();
    }

    @Override
    public boolean isOnline() {
        checkDisposed();
        return true;
    }

    @Override
    public double getHealth() {
        checkDisposed();
        LivingEntity entity = livingEntity(false);
        if (entity == null)
            return 0.0D;

        return entity.getHealth();
    }

    @Override
    public void setHealth(double health) {
        checkDisposed();
        LivingEntity entity = livingEntity(health > 0.0D);
        if (entity == null)
            return;

        entity.setHealth(health);
    }

    @Override
    public double getMaxHealth() {
        checkDisposed();
        LivingEntity entity = livingEntity(false);
        if (entity == null)
            return 0.0D;

        return entity.getMaxHealth();
    }

    @Override
    public void setMaxHealth(double maxHealth) {
        checkDisposed();
        LivingEntity entity = livingEntity(false);
        if (entity == null)
            return;

        entity.setMaxHealth(maxHealth);
    }

    @Override
    public void kill() {
        checkDisposed();
        Entity entity = getEntity();
        if (entity == null)
            return;

        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).damage(((LivingEntity) entity).getMaxHealth());
        }
        else {
            _npc.despawn();
        }
    }

    @Override
    public void kill(@Nullable IArenaPlayer blame) {
        checkDisposed();
        kill();
        _deathBlamePlayer = blame;
    }

    @Override
    public boolean teleport(Location location, TeleportMode mode) {
        Entity entity = getEntity();
        return entity == null
                ? _npc.spawn(location)
                : Teleporter.teleport(entity, location, mode).isSuccess();
    }

    @Nullable
    private LivingEntity livingEntity(boolean spawn) {
        checkDisposed();
        Entity entity = getEntity();
        if (entity == null && spawn) {
            _npc.spawn(_npc.getLocation());
        }

        if (!(entity instanceof LivingEntity))
            return null;

        return (LivingEntity)entity;
    }

    private void checkDisposed() {
        if (!_isDisposed && _npc.isDisposed()) {
            _isDisposed = true;
            ArenaPlayer.dispose(this);
        }
    }

    private class BukkitListener implements Listener {

        @EventHandler
        private void onNpcDeath(NpcDeathEvent event) {

            IArenaPlayer player = ArenaPlayer.get(event.getNpc());
            if (player == null || player.getArena() == null)
                return;

            if (player.getLives() > 0) {
                Scheduler.runTaskLater(PVStarAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {


                    }
                });
            }
        }
    }
}
