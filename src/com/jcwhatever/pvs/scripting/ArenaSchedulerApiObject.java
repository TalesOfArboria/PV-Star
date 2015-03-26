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
import com.jcwhatever.nucleus.utils.scheduler.IScheduledTask;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.utils.ArenaScheduler;

/**
 * Script API to give access to the scheduler.
 */
public class ArenaSchedulerApiObject implements IDisposable {

    private final Arena _arena;

    /**
     * Constructor.
     */
    ArenaSchedulerApiObject(Arena arena) {
        _arena = arena;
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    /**
     * Reset api and release resources.
     */
    @Override
    public void dispose() {
        // do nothing
    }

    /**
     * Run a task later. Task will run if arena is not running but will be cancelled
     * when the arena ends.
     *
     * @param delayTicks  Delay in ticks before executing the task
     * @param runnable    The task to run
     *
     * @return {@link IScheduledTask}
     */
    public IScheduledTask runTaskLater(int delayTicks, Runnable runnable) {
        return ArenaScheduler.runTaskLater(_arena, delayTicks, runnable);
    }

    /**
     * Repeat a task at interval. Task will NOT run unless the arena is running. Task
     * is cancelled when the arena ends.
     *
     * @param initialDelay  The initial delay in ticks before running the first task
     * @param interval      The delay in ticks between tasks
     * @param runnable      The task to run
     *
     * @return {@link IScheduledTask}
     */
    public IScheduledTask runTaskRepeat(int initialDelay, int interval, Runnable runnable) {
        return ArenaScheduler.runTaskRepeat(_arena, initialDelay, interval, runnable);
    }
}
