package com.jcwhatever.bukkit.pvs.scripting.api;

import com.jcwhatever.bukkit.generic.player.PlayerHelper;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptApi;
import org.bukkit.entity.Player;

/**
 * Provides player object conversion to scripts.
 */
public class PlayerApi extends ScriptApi {

    private static ApiObject _api;

    @Override
    public String getVariableName() {
        return "player";
    }

    @Override
    protected IScriptApiObject onCreateApiObject(Arena arena, EvaluatedScript script) {
        if (_api == null)
            _api = new ApiObject();

        return _api;
    }

    public static class ApiObject implements IScriptApiObject {

        /**
         * Reset api and release resources.
         */
        @Override
        public void reset() {
            // do nothing
        }

        /**
         * Ensure an object that represents a player is returned
         * as an {@code ArenaPlayer} object.
         *
         * @param player  The player object.
         */
        public ArenaPlayer getArenaPlayer(Object player) {
            PreCon.notNull(player);

            return PVStarAPI.getArenaPlayer(player);
        }

        /**
         * Ensure an object that represents a player is returned
         * as an {@code Player} object.
         *
         * @param player  The player object.
         */
        public Player getPlayer(Object player) {
            PreCon.notNull(player);

            Player p = PlayerHelper.getPlayer(player);
            PreCon.notNull(p);

            return p;
        }
    }
}
