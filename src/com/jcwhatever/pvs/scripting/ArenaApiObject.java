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

package com.jcwhatever.pvs.scripting;

import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.pvs.api.arena.IArena;

/**
 *Arena sub API.
 */
public class ArenaApiObject implements IDisposable {

    private boolean _isDisposed;

    public final IArena arena;
    public final ArenaEventsApiObject events;
    public final ArenaSchedulerApiObject scheduler;
    public final ArenaSpawnsApiObject spawns;
    public final ArenaStatsApiObject stats;
    public final ArenaPointsApiObject points;
    public final ArenaSoundsApiObject sounds;

    public ArenaApiObject(IArena arena) {
        this.arena = arena;

        events = new ArenaEventsApiObject(arena);
        scheduler = new ArenaSchedulerApiObject(arena);
        spawns = new ArenaSpawnsApiObject(arena);
        stats = new ArenaStatsApiObject(arena);
        points = new ArenaPointsApiObject(arena);
        sounds = new ArenaSoundsApiObject(arena);
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {

        events.dispose();
        scheduler.dispose();
        spawns.dispose();
        stats.dispose();
        points.dispose();
        sounds.dispose();

        _isDisposed = true;
    }
}
