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


package com.jcwhatever.bukkit.pvs.listeners;

import com.jcwhatever.bukkit.generic.extended.MaterialExt;
import com.jcwhatever.bukkit.generic.items.ItemStackHelper;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaTeam;
import com.jcwhatever.bukkit.pvs.api.arena.settings.PlayerManagerSettings;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class PvpListener implements Listener {

    /*
        Handle player item repair
     */
    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {

        if (!event.hasBlock())
            return;

        ArenaPlayer player = PVArenaPlayer.get(event.getPlayer());
        Arena arena = player.getArena();
        if (arena == null)
            return;

        ItemStack inHand = event.getPlayer().getItemInHand();
        if (inHand == null)
            return;

        PlayerManagerSettings settings = player.getRelatedSettings();
        if (settings == null)
            return;

        MaterialExt materialExt = MaterialExt.from(inHand.getType());

        if ((materialExt.isTool() && !settings.isToolsDamageable())
                || (materialExt.isWeapon() && !settings.isWeaponsDamageable())) {

            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onArmorDamage(EntityDamageEvent event) {

        Entity entity = event.getEntity();

        if (!(entity instanceof Player))
            return;

        Player p = (Player)entity;

        ArenaPlayer player =PVArenaPlayer.get(p);

        Arena arena = player.getArena();
        if (arena == null)
            return;

        // get settings
        PlayerManagerSettings settings = player.getRelatedSettings();
        if (settings == null)
            return;

        // prevent armor damage
        if (!settings.isArmorDamageable()) {
            ItemStackHelper.repair(p.getInventory().getArmorContents());
        }
    }


    /*
      Handle PVP
    */
    @EventHandler(priority= EventPriority.NORMAL)
    private void onPVP(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();

        if (!(entity instanceof Player))
            return;

        Player p = (Player)entity;

        ArenaPlayer player =PVArenaPlayer.get(p);

        Arena arena = player.getArena();
        if (arena == null)
            return;

        // no damage when game is over
        if (arena.getGameManager().isGameOver()) {
            event.setDamage(0.0);
            event.setCancelled(true);
            return;
        }


        // get settings
        PlayerManagerSettings settings = player.getRelatedSettings();
        if (settings == null)
            return;

        // prevent pvp
        if (!settings.isPvpEnabled() || !settings.isTeamPvpEnabled()) {

            Entity damagerEntity = event.getDamager();
            Player damager = null;

            // get damager
            if (damagerEntity instanceof Projectile) {
                ProjectileSource source = ((Projectile) damagerEntity).getShooter();

                if (source instanceof Player) {
                    damager = (Player) source;
                }
            } else if (damagerEntity instanceof Player) {
                damager = (Player) damagerEntity;
            }

            // handle player on player damage
            if (damager != null) {

                // check for pvp
                if (!settings.isPvpEnabled()) {
                    event.setDamage(0.0);
                    event.setCancelled(true);
                }
                // check for team pvp
                else //noinspection ConstantConditions
                    if (!settings.isTeamPvpEnabled()) {  // always true, statement is for readability
                    ArenaPlayer damagerPlayer =PVArenaPlayer.get(p);

                    // prevent team pvp
                    if (damagerPlayer.getTeam() == player.getTeam() &&
                            damagerPlayer.getTeam() != ArenaTeam.NONE ||
                            player.getTeam() != ArenaTeam.NONE) {

                        event.setDamage(0.0D);
                        event.setCancelled(true);
                    }

                }
            }


        }
    }

}
