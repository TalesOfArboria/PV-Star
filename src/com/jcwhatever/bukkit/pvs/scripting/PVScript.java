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
import com.jcwhatever.bukkit.generic.scripting.IEvaluatedScript;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApi;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.Script;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;

import java.util.Collection;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * An unevaluated script which is used to produce
 * evaluated scripts.
 */
public class PVScript implements Script {

    private final String _name;
    private final String _type;
    private final String _script;

    /*
     * Constructor.
     */
    public PVScript(String name, String type, String script) {
        PreCon.notNullOrEmpty(name);
        PreCon.notNullOrEmpty(type);
        PreCon.notNull(script);

        _name = name;
        _type = type;
        _script = script;
    }

    /*
     * Get the name of the script.
     */
    @Override
    public String getName() {
        return _name;
    }

    /*
     * Get the script source.
     */
    @Override
    public String getScript() {
        return _script;
    }

    /*
     * Get the script type (the script file extension)
     */
    @Override
    public String getType() {
        return _type;
    }

    /*
     * Evaluate a script without an arena.
     */
    @Override
    public IEvaluatedScript evaluate(@Nullable Collection<? extends IScriptApi> apiCollection) {

        return eval(null, apiCollection);
    }

    /*
     * Evaluate the script for the specified arena and using the specified api.
     */
    @Nullable
    @Override
    public EvaluatedScript evaluate(Arena arena, @Nullable Collection<? extends IScriptApi> apiCollection) {
        PreCon.notNull(arena);

        IEvaluatedScript evaluatedScript = eval(arena, apiCollection);

        return (EvaluatedScript)evaluatedScript;
    }

    /*
     * Evaluate the script for an arena if provided, or
     * as a GenericsEvaluatedScript if no arena provided.
     */
    @Nullable
    private IEvaluatedScript eval(@Nullable Arena arena, @Nullable Collection<? extends IScriptApi> apiCollection) {

        // get a script engine using the script type
        ScriptEngine engine = PVStarAPI.getScriptManager().getEngineManager().getEngineByExtension(getType());
        if (engine == null) {

            Msg.warning("Failed to load script named '{0}' because a script engine was not found for type '{1}'.",
                    getName(),getType());

            return null;
        }

        // instantiate new evaluated script
        IEvaluatedScript evaluated = arena != null
                    ? new PVEvaluatedArenaScript(arena, engine, this, apiCollection)
                    : new GenericsEvaluatedScript(this, engine, apiCollection);

        try {
            // evaluate script
            engine.eval(getScript());

            return evaluated;

        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }
}
