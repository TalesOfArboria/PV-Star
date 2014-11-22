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

import com.jcwhatever.bukkit.generic.events.IEventHandler;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Forward global events to the appropriate arena.
 */
public class BukkitEventForwarder implements IEventHandler {

    @Override
    public void call(Object e) {

        if (!(e instanceof Event))
            return;

        Event event = (Event)e;

        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent)event;

            if (interactEvent.hasBlock()) {
                callEvent(interactEvent.getClickedBlock(), event);
            }
            else {
                callEvent(interactEvent.getPlayer(), event);
            }
        }

        else if (event instanceof PlayerEvent) {
            callEvent(((PlayerEvent) event).getPlayer(), event);
        }

        else if (event instanceof BlockEvent) {
            callEvent(((BlockEvent) event).getBlock(), event);
        }

        else if (event instanceof EnchantItemEvent) {
            callEvent(((EnchantItemEvent) event).getEnchanter(), event);
        }

        else if (event instanceof PrepareItemEnchantEvent) {

            callEvent(((PrepareItemEnchantEvent) event).getEnchanter(), event);
        }

        else if (event instanceof HangingEvent) {
            callEvent(((HangingEvent) event).getEntity(), event);
        }

        else if (event instanceof InventoryEvent) {

            InventoryHolder holder = ((InventoryEvent) event).getInventory().getHolder();

            if (holder instanceof Player) {
                callEvent((Player)holder, event);
            }
        }

        else if (event instanceof VehicleEvent) {

            callEvent(((VehicleEvent) event).getVehicle(), event);
        }

        else if (event instanceof EntityEvent) {

            Entity entity = ((EntityEvent) event).getEntity();
            if (entity != null) {
                callEvent(entity, event);
            }
            else if (event instanceof EntityExplodeEvent) {
                callEvent(((EntityExplodeEvent) event).getLocation(), event);
            }
            else {
                Msg.debug("Failed to forward bukkit EntityEvent event because it has no entity.");
            }
        }

    }

    private <T extends Event> void callEvent(Block block, T event) {
        callEvent(block.getLocation(), event);
    }

    private <T extends Event> void callEvent(Player p, T event) {
        ArenaPlayer player = PVArenaPlayer.get(p);
        if (player.getArena() == null)
            return;

        player.getArena().getEventManager().call(event);
    }

    private <T extends Event> void callEvent(Entity entity, T event) {
        if (entity instanceof Player) {
            callEvent((Player)entity, event);
        }
        else {
            callEvent(entity.getLocation(), event);
        }
    }

    private <T extends Event> void callEvent(Location location, T event) {

        Arena arena = PVStarAPI.getArenaManager().getArena(location);
        if (arena == null)
            return;

        arena.getEventManager().call(event);
    }




}
