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

import com.jcwhatever.nucleus.events.manager.AbstractBukkitForwarder;
import com.jcwhatever.nucleus.events.manager.EventManager;
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
 * Forward Bukkit events to the appropriate arena.
 */
public class BukkitEventForwarder extends AbstractBukkitForwarder {

    /**
     * Constructor.
     *
     * <p>Automatically attaches forwarder to the specified source event manager.</p>
     *
     * @param source The event manager source that Bukkit events are handled from.
     *               Typically this will be the global event manager since it is the
     *               only manager that receives bukkit events as part of its normal
     *               operation.
     */
    public BukkitEventForwarder(EventManager source) {
        super(PVStarAPI.getPlugin(), source);
    }

    @Override
    protected void onBlockEvent(BlockEvent event) {
        callEvent(event.getBlock(), event);
    }

    @Override
    protected void onPlayerEvent(PlayerEvent event) {

        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;

            if (interactEvent.hasBlock()) {
                callEvent(interactEvent.getClickedBlock(), event);
            } else {
                callEvent(interactEvent.getPlayer(), event);
            }
        }
        else {
            callEvent(event.getPlayer(), event);
        }
    }

    @Override
    protected void onInventoryEvent(InventoryEvent event) {
        if (event instanceof EnchantItemEvent) {
            callEvent(((EnchantItemEvent) event).getEnchanter(), event);
        }
        else if (event instanceof PrepareItemEnchantEvent) {

            callEvent(((PrepareItemEnchantEvent) event).getEnchanter(), event);
        }
        else {
            InventoryHolder holder = event.getInventory().getHolder();

            if (holder instanceof Player) {
                callEvent((Player)holder, event);
            }
        }
    }

    @Override
    protected void onHangingEvent(HangingEvent event) {

    }

    @Override
    protected void onVehicleEvent(VehicleEvent event) {
        callEvent(event.getVehicle(), event);
    }

    @Override
    protected void onEntityEvent(EntityEvent event) {
        Entity entity = event.getEntity();
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

    @Override
    protected void onOtherEvent(Event event) {
        // do nothing
    }

    private <T extends Event> void callEvent(Block block, T event) {
        callEvent(block.getLocation(), event);
    }

    private <T extends Event> void callEvent(Player p, T event) {
        ArenaPlayer player = PVArenaPlayer.get(p);
        if (player.getArena() == null)
            return;

        player.getArena().getEventManager().call(this, event);
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

        arena.getEventManager().call(this, event);
    }
}
