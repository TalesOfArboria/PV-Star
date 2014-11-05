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


package com.jcwhatever.bukkit.pvs.scripting.api;

import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.Scheduler.ScheduledTask;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptApi;
import com.jcwhatever.bukkit.pvs.api.utils.ArenaScheduler;

/**
 * Provide scripts with access to {@code ArenaScheduler}.
 */
public class SchedulerApi extends ScriptApi {

    @Override
    public String getVariableName() {
        return "scheduler";
    }

    @Override
    protected IScriptApiObject onCreateApiObject(Arena arena, EvaluatedScript script) {
        return new ApiObject(arena);
    }

    public static class ApiObject implements IScriptApiObject {

        private final Arena _arena;

        /**
         * Constructor.
         *
         * @param arena  The owning arena.
         */
        ApiObject(Arena arena) {
            _arena = arena;
        }

        /**
         * Reset api and release resources.
         */
        @Override
        public void reset() {
            // Do nothing
        }

        /**
         * Run a task later. Task will run if arena is not running but will be cancelled
         * when the arena ends.
         *
         * @param delayTicks  Delay in ticks before executing the task
         * @param runnable    The task to run
         *
         * @return {@code ScheduledTask}
         */
        public ScheduledTask runTaskLater(int delayTicks, Runnable runnable) {
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
         * @return {@code ScheduledTask}
         */
        public ScheduledTask runTaskRepeat(int initialDelay, int interval, Runnable runnable) {
            return ArenaScheduler.runTaskRepeat(_arena, initialDelay, interval, runnable);
        }
    }

}
