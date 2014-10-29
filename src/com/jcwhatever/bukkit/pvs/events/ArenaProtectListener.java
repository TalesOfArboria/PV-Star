/* This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.pvs.events;

import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.events.players.ArenaBlockDamagePreventEvent;
import com.jcwhatever.bukkit.pvs.api.events.players.PlayerBlockInteractEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ArenaProtectListener implements Listener {

    /*
      Handle arena damage (Item Frames and Paintings)
     */
    @EventHandler(priority= EventPriority.HIGHEST)
    private void onItemFrameDamage(EntityDamageEvent event) {

        Entity damagee = event.getEntity();

        if (event.getEntityType() == EntityType.ITEM_FRAME || event.getEntityType() == EntityType.PAINTING) {

            Arena arena = PVStarAPI.getArenaManager().getArena(damagee.getLocation());

            if (arena != null && !arena.getSettings().isArenaDamageEnabled()) {
                event.setDamage(0.0);
                event.setCancelled(true);
            }
        }
    }

    /*
      Handle arena damage (Item Frames and Paintings)
     */
    @EventHandler(priority=EventPriority.HIGHEST)
    private void onItemFrameBreak(HangingBreakEvent event) {

        // do not prevent physics break
        if (event.getCause() == RemoveCause.PHYSICS)
            return;

        Hanging damagee = event.getEntity();

        Arena arena = PVStarAPI.getArenaManager().getArena(damagee.getLocation());

        if (arena != null && !arena.getSettings().isArenaDamageEnabled()) {
            event.setCancelled(true);
        }
    }

    /*
      Handle arena damage (Item Frames and Paintings)
     */
    @EventHandler
    private void onPlayerInteractItemFrame(PlayerInteractEntityEvent event) {

        if (!event.getRightClicked().getType().equals(EntityType.ITEM_FRAME))
            return;

        ArenaPlayer player = PVArenaPlayer.get(event.getPlayer());
        Arena arena = player.getArena();
        if (arena == null)
            return;

        if (arena.getSettings().isArenaDamageEnabled())
            return;

        event.setCancelled(true);
    }

    /*
       Handle arena damage (Blocks)
     */
    @EventHandler
    private void onProtectArenaBlocks(PlayerInteractEvent event) {

        if (!event.hasBlock())
            return;

        ArenaPlayer player =PVArenaPlayer.get(event.getPlayer());
        Arena arena = player.getArena();
        if (arena == null)
            return;

        PlayerBlockInteractEvent interactEvent = new PlayerBlockInteractEvent(arena, player, event);
        arena.getEventManager().call(interactEvent);

        if (interactEvent.isCancelled()) {
            event.setCancelled(true);
        }
        else if (!arena.getSettings().isArenaDamageEnabled()) {

            // deny interaction

            ArenaBlockDamagePreventEvent preventEvent = new ArenaBlockDamagePreventEvent(
                    arena, player, event);

            arena.getEventManager().call(preventEvent);

            if (!preventEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }

    }

}
