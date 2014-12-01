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

import com.jcwhatever.bukkit.generic.events.GenericsEventHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventPriority;
import com.jcwhatever.bukkit.generic.events.IEventHandler;
import com.jcwhatever.bukkit.generic.events.IGenericsEventListener;
import com.jcwhatever.bukkit.generic.scripting.AbstractScriptManager;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApi;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.ScriptUtils.ScriptConstructor;
import com.jcwhatever.bukkit.pvs.PVStar;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.events.ArenaDisabledEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaEnabledEvent;
import com.jcwhatever.bukkit.pvs.api.events.PVStarLoadedEvent;
import com.jcwhatever.bukkit.pvs.api.scripting.ArenaScriptManager;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
import com.jcwhatever.bukkit.pvs.api.scripting.Script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Arena script manager implementation.
 */
public class PVArenaScriptManager extends AbstractScriptManager<Script, EvaluatedScript> implements ArenaScriptManager, IGenericsEventListener {

    private final Arena _arena;
    private final IDataNode _dataNode;

    /*
     * Constructor.
     */
    public PVArenaScriptManager (Arena arena) {
        super(PVStarAPI.getPlugin());

        _arena = arena;
        _dataNode = arena.getDataNode("scripts");

        loadScripts();

        _arena.getEventManager().register(this);
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

        if (super.addScript(script)) {

            reload();

            _dataNode.set("names", new ArrayList<>(getScriptNames()));
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
            super.addScript(script);
        }

        reload();

        _dataNode.set("names", new ArrayList<>(getScriptNames()));
        _dataNode.saveAsync(null);

        return true;
    }

    /*
     * Remove a collection of scripts from the arena.
     */
    @Override
    public boolean removeScripts(Collection<Script> scripts) {
        PreCon.notNull(scripts);

        for (Script script : scripts) {
            super.removeScript(script);
        }

        _dataNode.set("names", new ArrayList<>(getScriptNames()));
        _dataNode.saveAsync(null);

        return true;
    }

    @Override
    public void loadScripts() {

        clearScripts();
        clearScriptApi();

        addScriptApi(PVStarAPI.getScriptManager().getScriptApis());

        // add scripts
        List<String> scriptNames = _dataNode.getStringList("names", null);
        if (scriptNames != null && !scriptNames.isEmpty()) {
            for (String scriptName : scriptNames) {

                Script script = PVStarAPI.getScriptManager().getScript(scriptName);
                if (script == null)
                    continue;

                super.addScript(script);
            }
        }

        if (((PVStar)PVStarAPI.getPlugin()).isLoaded()) {
            evaluate();
        }
        else {
            PVStarAPI.getEventManager().register(PVStarLoadedEvent.class,
                    GenericsEventPriority.NORMAL, new IEventHandler() {

                        @Override
                        public void call(Object event) {
                            evaluate();
                        }
                    });
        }
    }

    /**
     * Arena Script Manager does not construct scripts, it only evaluates the ones
     * it's assigned by the PV-Star Script Manager.
     */
    @Override
    @Nullable
    public ScriptConstructor<Script> getScriptConstructor() {
        return null; // does not create script instances
    }

    @Override
    @Nullable
    protected EvaluatedScript callEvaluate(Script script, Collection<IScriptApi> api) {
        return script.evaluate(_arena, api);
    }

    @GenericsEventHandler
    private void onArenaEnable(@SuppressWarnings("unused") ArenaEnabledEvent event) {
        evaluate();
    }

    @GenericsEventHandler
    private void onArenaDisable(@SuppressWarnings("unused") ArenaDisabledEvent event) {
        evaluate();
    }
}