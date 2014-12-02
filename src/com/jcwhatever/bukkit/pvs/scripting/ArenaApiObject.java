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

package com.jcwhatever.bukkit.pvs.scripting;

import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.options.NameMatchMode;

import java.util.List;
import javax.annotation.Nullable;

/*
 *
 */
public class ArenaApiObject implements IScriptApiObject {

    private Arena _arena;
    private boolean _isDisposed;

    public final ArenaEventsApiObject events = new ArenaEventsApiObject();
    public final ArenaSchedulerApiObject scheduler = new ArenaSchedulerApiObject();
    public final ArenaSpawnsApiObject spawns = new ArenaSpawnsApiObject();
    public final ArenaStatsApiObject stats = new ArenaStatsApiObject();

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {
        _arena = null;

        events.dispose();

        _isDisposed = true;
    }

    @Nullable
    public Arena setArenaByName(String name) {
        PreCon.notNullOrEmpty(name);

        List<Arena> arenas = PVStarAPI.getArenaManager().getArena(name, NameMatchMode.CASE_INSENSITIVE);
        Arena arena = arenas.size() == 1 ? arenas.get(0) : null;
        if (arena != null) {
            setArena(arena);
        }

        return _arena;
    }

    public void setArena(Arena arena) {
        PreCon.notNull(arena);

        _arena = arena;
        events.setArena(arena);
        scheduler.setArena(arena);
        spawns.setArena(arena);
    }
}
