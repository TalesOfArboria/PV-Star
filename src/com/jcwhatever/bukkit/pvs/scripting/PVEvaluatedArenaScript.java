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


package com.jcwhatever.bukkit.pvs.scripting;

import com.jcwhatever.bukkit.generic.scripting.GenericsEvaluatedScript;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApi;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.Script;

import java.util.Collection;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;

/*
 * A script that has been evaluated for an arena.
 */
public class PVEvaluatedArenaScript extends GenericsEvaluatedScript implements EvaluatedScript {

    private final Arena _arena;

    /*
     * Constructor.
     */
    public PVEvaluatedArenaScript(Arena arena, ScriptEngine engine, Script parentScript,
                                  @Nullable Collection<? extends IScriptApi> apiCollection) {
        super(parentScript, engine, apiCollection);

        PreCon.notNull(arena);

        _arena = arena;

        Object engineArena = engine.get("_arena");

        if (engineArena == null) {
            engine.put("_arena", arena);
        }
        else if (!engineArena.equals(arena)) {
            throw new IllegalArgumentException("The engine provided is already reserved for a different arena.");
        }
    }

    /*
     * Get the arena the script was evaluated for.
     */
    @Override
    public Arena getArena() {
        return _arena;
    }
}
