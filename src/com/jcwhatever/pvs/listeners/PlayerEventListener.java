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


package com.jcwhatever.pvs.listeners;

import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.utils.Rand;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.pvs.ArenaPlayer;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.options.PlayerLeaveArenaReason;
import com.jcwhatever.pvs.api.arena.settings.IContextSettings;
import com.jcwhatever.pvs.api.events.players.PlayerArenaRespawnEvent;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.arenas.AbstractArena;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Set;
import java.util.regex.Matcher;

public class PlayerEventListener implements Listener {

    @Localizable static final String _COMMAND_NOT_IN_ARENA =
            "{RED}You can't use that command in the arena!";

    private static final Location DEATH_RESPAWN_LOCATION = new Location(null, 0, 0, 0);

    /**
     * Ensure player data disposal on join.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerJoin(PlayerJoinEvent event) {

        ArenaPlayer player = ArenaPlayer.get(event.getPlayer());
        if (player.getArena() != null) {
            player.getArena().remove(player, PlayerLeaveArenaReason.LOGOUT);
        }

        // dispose player in case connection issues cause
        // player to disconnect without throwing a player leave event.
        ArenaPlayer.dispose(player);
    }

    @EventHandler
    private void onPlayerKick(PlayerKickEvent event) {

        final ArenaPlayer player = ArenaPlayer.get(event.getPlayer());
        final AbstractArena arena = player.getArena();

        if (arena != null) {
            arena.remove(player, PlayerLeaveArenaReason.KICK);
        }

        ArenaPlayer.dispose(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {

        ArenaPlayer player = ArenaPlayer.get(event.getPlayer());

        AbstractArena arena = player.getArena();
        if (arena == null)
            return;

        arena.remove(player, PlayerLeaveArenaReason.LOGOUT);

        ArenaPlayer.dispose(player);
    }

    /*
        Handle arena respawning
     */
    @EventHandler(priority=EventPriority.HIGHEST)
    private void onPlayerRespawn(PlayerRespawnEvent event) {

        ArenaPlayer player = ArenaPlayer.get(event.getPlayer());
        IArena arena = player.getArena();

        // respawn player in appropriate arena area.
        if (arena != null) {
            Spawnpoint spawn = Rand.get(arena.getSpawns().getAll(player.getContext()));
            if (spawn == null)
                return;

            PlayerArenaRespawnEvent respawnEvent =
                    new PlayerArenaRespawnEvent(arena, player, player.getContextManager(), spawn);

            arena.getEventManager().call(this, respawnEvent);

            event.setRespawnLocation(respawnEvent.getRespawnLocation());
        }
        else {
            Location respawnLocation = player.getDeathRespawnLocation(DEATH_RESPAWN_LOCATION);
            if (respawnLocation != null) {
                event.setRespawnLocation(respawnLocation);
            }
        }
    }

    /*
      Prevent commands inside the arena
     */
    @EventHandler(priority=EventPriority.LOWEST)
    private void onPlayerCommand(PlayerCommandPreprocessEvent event) {

        IArenaPlayer player = ArenaPlayer.get(event.getPlayer());
        IArena arena = player.getArena();

        if (arena == null)
            return;

        Set<String> pvStarCommands = PVStarAPI.getPlugin().getDescription().getCommands().keySet();

        String[] comp = TextUtils.PATTERN_SPACE.split(event.getMessage());

        Matcher matcher = TextUtils.PATTERN_FILEPATH_SLASH.matcher(comp[0]);

        String command = matcher.replaceFirst("").toLowerCase();

        if (!pvStarCommands.contains(command)) {
            event.setMessage("/");
            event.setCancelled(true);
            Msg.tell(event.getPlayer(), Lang.get(_COMMAND_NOT_IN_ARENA));
        }
    }


    /*
      Handle player hunger
     */
    @EventHandler
    private void onPlayerHunger(FoodLevelChangeEvent event) {

        if (!(event.getEntity() instanceof Player))
            return;

        Player p = (Player)event.getEntity();
        IArenaPlayer player = ArenaPlayer.get(p);

        IArena arena = player.getArena();
        if (arena == null)
            return;

        // get settings
        IContextSettings settings = player.getContextSettings();
        if (settings == null)
            return;

        // prevent hunger
        if (!settings.isHungerEnabled()) {
            event.setFoodLevel(20);
            event.setCancelled(true);
        }
    }

    /*
      Handle player fall damage
     */
    @EventHandler
    private void onPlayerFall(EntityDamageEvent event) {
        if (event.getCause() != DamageCause.FALL)
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        IArenaPlayer player = ArenaPlayer.get((Player) event.getEntity());

        IArena arena = player.getArena();
        if (arena == null)
            return;

        IContextSettings settings = player.getContextSettings();
        if (settings == null)
            return;

        if (!settings.hasFallDamage()) {
            event.setDamage(0.0D);
            event.setCancelled(true);
        }
    }

    /*
     * Handle player immobilization
     */
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {

        ArenaPlayer player = ArenaPlayer.get(event.getPlayer());
        IArena arena = player.getArena();
        if (arena == null)
            return;

        // player immobilization
        if (player.isImmobilized()) {
            Location fr = event.getFrom();
            Location to = player.IMMOBILIZE_LOCATION;
            to.setWorld(fr.getWorld());
            to.setX(fr.getX());
            to.setY(fr.getY());
            to.setZ(fr.getZ());
            to.setYaw(event.getTo().getYaw());
            to.setPitch(event.getTo().getPitch());
            event.setTo(to);
        }
    }
}
