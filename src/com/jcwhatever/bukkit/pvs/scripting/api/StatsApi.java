package com.jcwhatever.bukkit.pvs.scripting.api;

import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptApi;
import com.jcwhatever.bukkit.pvs.api.stats.ArenaStats;
import com.jcwhatever.bukkit.pvs.api.stats.StatType;

/**
 * Provide scripts with access to arena statistics.
 */
public class StatsApi extends ScriptApi {

    @Override
    public String getVariableName() {
        return "stats";
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
            // do nothing
        }

        /**
         * Get a statistic type by name.
         *
         * @param typeName  The name of the statistic.
         */
        public StatType getType(String typeName) {
            PreCon.notNullOrEmpty(typeName);

            return PVStarAPI.getStatsManager().getType(typeName);
        }

        /**
         * Get statistics for the arena.
         */
        public ArenaStats getStats() {
            return PVStarAPI.getStatsManager().getArenaStats(_arena.getId());
        }
    }
}
