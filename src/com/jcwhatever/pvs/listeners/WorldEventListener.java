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

package com.jcwhatever.pvs.listeners;

import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;

import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.List;

/*
 * World listener
 */
public class WorldEventListener implements Listener {

    private void onWorldLoad(WorldLoadEvent event) {
        List<IArena> arenas = PVStarAPI.getArenaManager().getArenas();

        for (IArena arena : arenas) {
            if (arena.getRegion().isDefined()) {
                String worldName = arena.getRegion().getWorldName();

                if (event.getWorld().getName().equals(worldName) &&
                        arena.getSettings().isConfigEnabled()) {

                    arena.getSettings().setTransientEnabled(true);
                }
            }
        }
    }

    private void onWorldUnload(WorldUnloadEvent event) {
        List<IArena> arenas = PVStarAPI.getArenaManager().getArenas();

        for (IArena arena : arenas) {
            if (arena.getRegion().isDefined()) {
                World world = arena.getRegion().getWorld();

                if (event.getWorld().equals(world)) {
                    arena.getSettings().setTransientEnabled(false);
                }
            }
        }
    }
}
