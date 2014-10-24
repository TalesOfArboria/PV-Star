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
