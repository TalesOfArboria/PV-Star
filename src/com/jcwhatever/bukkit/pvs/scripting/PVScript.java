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

import com.jcwhatever.bukkit.generic.scripting.GenericsScript;
import com.jcwhatever.bukkit.generic.scripting.IEvaluatedScript;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApi;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.Script;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;

import java.io.File;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;

/**
 * An unevaluated script which is used to produce
 * evaluated scripts.
 */
public class PVScript extends GenericsScript implements Script {

    /*
     * Constructor.
     */
    public PVScript(String name, @Nullable File file, String type, String script) {
        super(name, file, type, script);
    }

    /*
     * Evaluate a script without an arena.
     */
    @Override
    @Nullable
    public IEvaluatedScript evaluate(@Nullable Collection<? extends IScriptApi> apiCollection) {
        return super.evaluate(apiCollection);
    }

    /*
     * Evaluate the script for the specified arena and using the specified api.
     */
    @Nullable
    @Override
    public EvaluatedScript evaluate(Arena arena, @Nullable Collection<? extends IScriptApi> apiCollection) {
        PreCon.notNull(arena);

        // get a script engine using the script type
        ScriptEngine engine = getScriptEngine();
        if (engine == null)
            return null;

        // instantiate new evaluated script
        EvaluatedScript evaluated = new PVEvaluatedArenaScript(arena, engine, this, apiCollection);

        // evaluate
        if (!eval(engine, evaluated.getContext())) {
            return null;
        }

        return evaluated;
    }


    @Override
    @Nullable
    protected ScriptEngine getScriptEngine() {
        ScriptEngine engine = PVStarAPI.getScriptManager().getEngineManager().getEngineByExtension(getType());

        if (engine == null) {

            Msg.warning("Failed to load script named '{0}' because a script engine was not found for type '{1}'.",
                    getName(),getType());
        }

        return engine;
    }
}
