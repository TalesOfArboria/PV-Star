/* This file is part of ${MODULE_NAME}, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.pvs.scripting.repo;

import com.jcwhatever.bukkit.generic.scripting.IScriptApiInfo;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.NameMatchMode;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptApi;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@IScriptApiInfo(
        variableName = "pvarena",
        description = "Access arenas through scripts.")
public class ArenaRepoApi extends ScriptApi {

    private static ApiObject _apiObject;

    public ArenaRepoApi(Plugin plugin) {
        PreCon.notNull(plugin);

        // plugin constructor is for ScriptApiRepo
    }

    @Override
    public String getVariableName() {
        return "pvarena";
    }

    @Override
    protected IScriptApiObject onCreateApiObject(Arena arena, EvaluatedScript script) {
        if (_apiObject == null)
            _apiObject = new ApiObject();

        return _apiObject;
    }

    public static class ApiObject implements IScriptApiObject {

        @Override
        public void reset() {
            // do nothing
        }

        /**
         * Get an arena object by name.
         *
         * @param name  The name of the arena.
         */
        @Nullable
        public Arena getArenaByName(String name) {
            PreCon.notNullOrEmpty(name);

            List<Arena> arenas = PVStarAPI.getArenaManager().getArena(name, NameMatchMode.CASE_INSENSITIVE);
            return arenas.size() == 1 ? arenas.get(0) : null;
        }

        /**
         * Get an arena object by unique id.
         *
         * @param arenaId  The id of the arena.
         */
        @Nullable
        public Arena getArenaById(UUID arenaId) {
            PreCon.notNull(arenaId);

            return PVStarAPI.getArenaManager().getArena(arenaId);
        }

        /**
         * Get an arena object by location.
         *
         * @param location  The location of the arena.
         */
        @Nullable
        public Arena getArenaByLocation(Location location) {
            PreCon.notNull(location);

            return PVStarAPI.getArenaManager().getArena(location);
        }

        /**
         * Join a player to the specified arena.
         *
         * @param arena   The arena.
         * @param player  The player.
         *
         * @return  True if player joined.
         */
        public boolean join(Arena arena, Object player) {
            PreCon.notNull(arena);
            PreCon.notNull(player);

            ArenaPlayer p = PVStarAPI.getArenaPlayer(player);

            return arena.join(p, AddPlayerReason.PLAYER_JOIN);
        }

    }

}
