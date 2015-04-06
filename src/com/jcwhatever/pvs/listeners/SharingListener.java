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

import com.jcwhatever.pvs.PVArenaPlayer;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.settings.IPlayerSettings;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class SharingListener implements Listener {

    /*
     Handle chest sharing
    */
    @EventHandler(priority= EventPriority.HIGHEST)
    private void onInventorySharing(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity == null)
            return;

        if (!(humanEntity instanceof Player))
            return;

        final Player p = (Player)humanEntity;
        IArenaPlayer player = PVArenaPlayer.get(p);
        IArena arena = player.getArena();

        if (arena == null)
            return;

        IPlayerSettings settings = player.getRelatedSettings();
        if (settings == null)
            return;

        if (settings.isSharingEnabled())
            return;

        // prevent sharing
        if ((event.getAction() == InventoryAction.PLACE_ALL ||
                event.getAction() == InventoryAction.PLACE_ONE ||
                event.getAction() == InventoryAction.PLACE_SOME)) {

            if (event.getInventory().getHolder() instanceof Chest ||
                    event.getInventory().getHolder() instanceof DoubleChest) {

                if (event.getRawSlot() >= event.getInventory().getContents().length)
                    return;

                event.setCancelled(true);
                event.setResult(Result.DENY);

                // close chest to prevent sharing bug
                // i.e. If you try enough, eventually the event wont fire but the item
                // will make it into chest.
                Bukkit.getScheduler().runTask(PVStarAPI.getPlugin(), new Runnable() {

                    @Override
                    public void run() {
                        p.closeInventory();
                    }

                });
            }
        }
    }

    /*
     Handle drop item sharing
    */
    @EventHandler(priority=EventPriority.HIGHEST)
    private void onPlayerDropItem(PlayerDropItemEvent event) {

        IArenaPlayer player =PVArenaPlayer.get(event.getPlayer());

        IArena arena = player.getArena();
        if (arena == null)
            return;

        IPlayerSettings settings = player.getRelatedSettings();
        if (settings == null)
            return;

        if (settings.isSharingEnabled())
            return;

        event.setCancelled(true);
    }
}
