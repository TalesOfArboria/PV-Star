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
