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

import com.jcwhatever.bukkit.generic.events.EventHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventPriority;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApi;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.PVStar;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.events.PVStarLoadedEvent;
import com.jcwhatever.bukkit.pvs.api.scripting.ArenaScriptManager;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.Script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Arena script manager implementation.
 */
public class PVArenaScriptManager implements ArenaScriptManager {

    private final Arena _arena;
    private final IDataNode _dataNode;
    private final Set<String> _scriptNames = new HashSet<>(30);
    private final Map<String, EvaluatedScript> _evaluatedScripts = new HashMap<>(30);

    /*
     * Constructor.
     */
    public PVArenaScriptManager (Arena arena) {
        _arena = arena;
        _dataNode = arena.getDataNode("scripts");

        loadSettings();
    }

    /*
     * Get the owning arena.
     */
    @Override
    public Arena getArena() {
        return _arena;
    }

    /*
     * Add a script to the arena.
     */
    @Override
    public boolean addScript(Script script) {
        PreCon.notNull(script);

        String key = script.getName().toLowerCase();

        if (!_scriptNames.contains(key)) {

            _scriptNames.add(key);

            reload();

            _dataNode.set("names", new ArrayList<>(_scriptNames));
            _dataNode.saveAsync(null);

            return true;
        }
        return false;
    }

    /*
     * Add a collection of scripts to the arena
     */
    @Override
    public boolean addScripts(Collection<Script> scripts) {
        PreCon.notNull(scripts);

        for (Script script : scripts) {

            String key = script.getName().toLowerCase();

            if (!_scriptNames.contains(key)) {

                _scriptNames.add(key);
            }
        }

        reload();

        _dataNode.set("names", new ArrayList<>(_scriptNames));
        _dataNode.saveAsync(null);

        return true;
    }

    /*
     * Remove a script from the arena.
     */
    @Override
    public boolean removeScript(Script script) {
        PreCon.notNull(script);

        return removeScript(script.getName());
    }

    /*
     * Remove a script by name from the arena.
     */
    @Override
    public boolean removeScript(String scriptName) {
        PreCon.notNullOrEmpty(scriptName);

        scriptName = scriptName.toLowerCase();

        if (_scriptNames.contains(scriptName)) {

            resetApi();

            _scriptNames.remove(scriptName);
            _evaluatedScripts.remove(scriptName);

            reload();

            _dataNode.set("names", new ArrayList<>(_scriptNames));
            _dataNode.saveAsync(null);

            return true;
        }

        return false;
    }

    /*
     * Remove a collection of scripts from the arena.
     */
    @Override
    public boolean removeScripts(Collection<Script> scripts) {
        PreCon.notNull(scripts);

        resetApi();

        for (Script script : scripts) {

            String key = script.getName().toLowerCase();

            _scriptNames.remove(key);
            _evaluatedScripts.remove(key);
        }

        reload();

        _dataNode.set("names", new ArrayList<>(_scriptNames));
        _dataNode.saveAsync(null);

        return true;
    }

    /**
     * Get the arenas evaluated scripts.
     * scriptName parameters is the relative path of the script using dots instead of dashes and no extension.
     */
    @Nullable
    @Override
    public EvaluatedScript getEvaluatedScript(String scriptName) {
        PreCon.notNullOrEmpty(scriptName);

        return _evaluatedScripts.get(scriptName.toLowerCase());
    }

    /*
     * Get the names of the scripts added to the arena.
     */
    @Override
    public List<String> getScriptNames() {
        return new ArrayList<>(_evaluatedScripts.keySet());
    }

    /*
     * Get the arenas evaluated scripts.
     */
    @Override
    public List<EvaluatedScript> getEvaluatedScripts() {
        return new ArrayList<>(_evaluatedScripts.values());
    }

    /*
     * Reload the arenas scripts.
     */
    @Override
    public void reload() {
        evaluate();
    }

    /*
     * Reset the arenas script api
     */
    public void resetApi() {

        for (EvaluatedScript script : _evaluatedScripts.values()) {
            script.resetApi();
        }
    }

    /*
     * Evaluate the arenas scripts
     */
    private void evaluate() {

        // reset api
        resetApi();

        _evaluatedScripts.clear();

        List<IScriptApi> api = PVStarAPI.getScriptManager().getScriptApis();

        // iterate and evaluate scripts
        for (String scriptName : _scriptNames) {

            Script script = PVStarAPI.getScriptManager().getScript(scriptName);
            if (script == null)
                continue;

            EvaluatedScript evaluated = script.evaluate(getArena(), api);
            if (evaluated == null)
                continue;

            _evaluatedScripts.put(script.getName().toLowerCase(), evaluated);
        }
    }

    /*
     * Initial load arenas script settings
     */
    private void loadSettings() {

        // add scripts
        List<String> scriptNames = _dataNode.getStringList("names", null);
        if (scriptNames != null && !scriptNames.isEmpty()) {
            for (String scriptName : scriptNames) {

                Script script = PVStarAPI.getScriptManager().getScript(scriptName);
                if (script == null)
                    continue;

                addScript(script);
            }
        }

        if (((PVStar)PVStarAPI.getPlugin()).isLoaded()) {
            evaluate();
        }
        else {
            PVStarAPI.getEventManager().register(PVStarLoadedEvent.class,
                    GenericsEventPriority.NORMAL, new EventHandler() {

                        @Override
                        public void call(Object event) {
                            evaluate();
                        }
                    });
        }
    }
}