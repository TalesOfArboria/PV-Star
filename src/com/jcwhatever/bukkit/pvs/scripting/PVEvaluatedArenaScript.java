package com.jcwhatever.bukkit.pvs.scripting;

import com.jcwhatever.bukkit.generic.scripting.IScript;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApi;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.Script;

import javax.annotation.Nullable;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * A script that has been evaluated for an arena.
 */
public class PVEvaluatedArenaScript implements EvaluatedScript {

    private final Arena _arena;
    private final ScriptEngine _engine;
    private final Script _parentScript;
    private final Map<String, IScriptApi> _scriptApis;

    /*
     * Constructor.
     */
    public PVEvaluatedArenaScript(Arena arena, ScriptEngine engine, Script parentScript, @Nullable Collection<? extends IScriptApi> apiCollection) {
        PreCon.notNull(arena);
        PreCon.notNull(engine);
        PreCon.notNull(parentScript);

        _arena = arena;
        _engine = engine;
        _parentScript = parentScript;
        _scriptApis = new HashMap<>(apiCollection == null ? 10 : apiCollection.size());

        if (apiCollection != null) {
            for (IScriptApi api : apiCollection) {
                _scriptApis.put(api.getVariableName(), api);
            }
        }
    }

    /*
     * Get the arena the script was evaluated for.
     */
    @Override
    public Arena getArena() {
        return _arena;
    }

    /*
     * Get the script that was evaluated.
     */
    @Override
    public Script getParentScript() {
        return _parentScript;
    }

    /*
     * Get the script engine used to evaluate.
     */
    @Override
    public ScriptEngine getScriptEngine() {
        return _engine;
    }

    /*
     * Get the api included in the evaluated script.
     */
    @Override
    public List<IScriptApi> getScriptApi() {
        return _scriptApis == null
                ? new ArrayList<IScriptApi>(0)
                : new ArrayList<>(_scriptApis.values());
    }

    @Override
    public void addScriptApi(IScriptApi scriptApi) {

        if (_scriptApis.containsKey(scriptApi.getVariableName()))
            return;

        _scriptApis.put(scriptApi.getVariableName(), scriptApi);

        _engine.put(scriptApi.getVariableName(), scriptApi.getApiObject(this));
    }

    /*
     * Invoke a script function in the evaluated script.
     */
    @Override
    public Object invokeFunction(String functionName, Object... parameters) {
        Invocable inv = (Invocable)_engine;

        try {
            return inv.invokeFunction(functionName, parameters);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Evaluate another script into this scripts engine.
     */
    @Override
    public Object evaluate(IScript script) {

        try {
            return _engine.eval(script.getScript());
        }
        catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Reset the evaluated scripts api.
     */
    @Override
    public void resetApi() {

        if (_scriptApis == null)
            return;

        for (IScriptApi api : _scriptApis.values()) {
            api.reset();
        }
    }
}
