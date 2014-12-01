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

package com.jcwhatever.bukkit.pvs.scripting.repo;

import com.jcwhatever.bukkit.generic.scripting.IEvaluatedScript;
import com.jcwhatever.bukkit.generic.scripting.ScriptApiInfo;
import com.jcwhatever.bukkit.generic.scripting.api.GenericsScriptApi;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApiObject;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

@ScriptApiInfo(
        variableName = "pvPlayers",
        description = "Get arena player from player object.")
public class PVPlayersRepoApi extends GenericsScriptApi {

    private static ApiObject _apiObject;

    public PVPlayersRepoApi(Plugin plugin) {
        super(plugin);

        _apiObject = new ApiObject();
    }

    @Override
    public IScriptApiObject getApiObject(IEvaluatedScript script) {
        return _apiObject;
    }

    public static class ApiObject implements IScriptApiObject {

        @Override
        public void dispose() {
            // do nothing
        }

        /**
         * Get an {@code ArenaPlayer} object from a player object.
         *
         * @param player  The player object.
         */
        @Nullable
        public ArenaPlayer getArenaPlayer(Object player) {
            PreCon.notNull(player);

            return PVStarAPI.getArenaPlayer(player);
        }
    }
}
