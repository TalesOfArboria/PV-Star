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

import com.jcwhatever.bukkit.generic.scripting.GenericsScriptManager;
import com.jcwhatever.bukkit.generic.scripting.IScript;
import com.jcwhatever.bukkit.generic.scripting.ScriptApiRepo;
import com.jcwhatever.bukkit.generic.scripting.ScriptHelper.ScriptConstructor;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApi;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiDepends;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiEconomy;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiInclude;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiInventory;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiItemBank;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiMsg;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiPermissions;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiRand;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiScheduler;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiSounds;
import com.jcwhatever.bukkit.generic.utils.FileUtils.DirectoryTraversal;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.scripting.Script;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptManager;
import com.jcwhatever.bukkit.pvs.scripting.api.EventsApi;
import com.jcwhatever.bukkit.pvs.scripting.api.PlayerApi;
import com.jcwhatever.bukkit.pvs.scripting.api.SchedulerApi;
import com.jcwhatever.bukkit.pvs.scripting.api.SpawnApi;
import com.jcwhatever.bukkit.pvs.scripting.api.StatsApi;
import com.jcwhatever.bukkit.pvs.scripting.repo.PVArenasRepoApi;
import com.jcwhatever.bukkit.pvs.scripting.repo.PVEventsRepoApi;
import com.jcwhatever.bukkit.pvs.scripting.repo.PVPlayersRepoApi;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.script.ScriptEngineManager;

/**
 * A central repository of unevaluated scripts which can be used for one or more arenas.
 */
public class PVScriptManager implements ScriptManager {

    private final GenericsScriptManager _scriptRepository;
    private final Map<String, IScriptApi> _apiMap = new HashMap<>(30);
    private final File _scriptFolder;

    /*
     * Constructor.
     */
    public PVScriptManager(Plugin plugin, File scriptFolder, ScriptEngineManager engineManager) {
        PreCon.notNull(plugin);
        PreCon.notNull(scriptFolder);
        PreCon.notNull(engineManager);

        _scriptFolder = scriptFolder;
        _scriptRepository = new GenericsScriptManager(plugin, engineManager) {

            @Override
            public ScriptConstructor<IScript> getScriptConstructor() {
                return new ScriptConstructor<IScript>() {
                    @Override
                    public IScript construct(String name, @Nullable String filename, String type, String script) {
                        return new PVScript(name, filename, type, script);
                    }
                };
            }
        };

        _scriptRepository.loadScripts(_scriptFolder, DirectoryTraversal.RECURSIVE);

        // register Generics script api
        registerApiType(new ScriptApiEconomy(PVStarAPI.getPlugin()));
        registerApiType(new ScriptApiInclude(PVStarAPI.getPlugin(), _scriptRepository));
        registerApiType(new ScriptApiInventory(PVStarAPI.getPlugin()));
        registerApiType(new ScriptApiItemBank(PVStarAPI.getPlugin()));
        registerApiType(new ScriptApiMsg(PVStarAPI.getPlugin()));
        registerApiType(new ScriptApiPermissions(PVStarAPI.getPlugin()));
        registerApiType(new ScriptApiSounds(PVStarAPI.getPlugin()));
        registerApiType(new ScriptApiDepends(PVStarAPI.getPlugin()));
        registerApiType(new ScriptApiScheduler(PVStarAPI.getPlugin()));
        registerApiType(new ScriptApiRand(PVStarAPI.getPlugin()));

        // register PV-Star script api
        registerApiType(new EventsApi());
        registerApiType(new PlayerApi());
        registerApiType(new SchedulerApi());
        registerApiType(new SpawnApi());
        registerApiType(new StatsApi());

        // Register scripts in global script api repository
        ScriptApiRepo.registerApiType(PVStarAPI.getPlugin(), PVArenasRepoApi.class);
        ScriptApiRepo.registerApiType(PVStarAPI.getPlugin(), PVEventsRepoApi.class);
        ScriptApiRepo.registerApiType(PVStarAPI.getPlugin(), PVPlayersRepoApi.class);
    }


    /*
     * Get the engine manager used to get script engines.
     */
    @Override
    public ScriptEngineManager getEngineManager() {

        return _scriptRepository.getEngineManager();
    }

    /*
     * Add a script to the script repository
     */
    @Override
    public void addScript(Script script) {
        PreCon.notNull(script);

        _scriptRepository.addScript(script);
    }

    /*
     * Remove a script from the repository by script name.
     */
    @Override
    public void removeScript(String scriptName) {
        PreCon.notNullOrEmpty(scriptName);

        _scriptRepository.removeScript(scriptName);
    }

    /*
     * Register an api to be used in all evaluated scripts.
     */
    @Override
    public void registerApiType(IScriptApi api) {
        PreCon.notNull(api);

        _apiMap.put(api.getVariableName(), api);
    }

    /*
     *  Get a script api by its variable name.
     */
    @Override
    public IScriptApi getScriptApi(String apiVariableName) {
        PreCon.notNullOrEmpty(apiVariableName);

        return _apiMap.get(apiVariableName);
    }

    /*
     *  Get all script api.
     */
    @Override
    public List<IScriptApi> getScriptApis() {

        return new ArrayList<>(_apiMap.values());
    }

    /*
     * Get a script by name
     */
    @Nullable
    @Override
    public Script getScript(String scriptName) {
        PreCon.notNullOrEmpty(scriptName);

        IScript iScript = _scriptRepository.getScript(scriptName);
        return iScript instanceof Script ? (Script) iScript : null;
    }

    /*
     * Get the names of all scripts.
     */
    @Override
    public List<String> getScriptNames() {

        return _scriptRepository.getScriptNames();
    }

    /*
     * Get all scripts.
     */
    @Override
    public List<Script> getScripts() {

        List<IScript> iScripts = _scriptRepository.getScripts();
        List<Script> scripts = new ArrayList<>(iScripts.size());

        for (IScript iScript : iScripts) {
            scripts.add((Script)iScript);
        }

        return scripts;
    }

    /*
     * Reload repository of scripts from script directory.
     */
    @Override
    public void reload() {

        _scriptRepository.clearScripts();
        _scriptRepository.loadScripts(_scriptFolder, DirectoryTraversal.RECURSIVE);
    }
}
