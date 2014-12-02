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

import com.jcwhatever.bukkit.generic.scripting.AbstractScriptManager;
import com.jcwhatever.bukkit.generic.scripting.ScriptApiRepo;
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
import com.jcwhatever.bukkit.generic.utils.ScriptUtils.IScriptFactory;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.scripting.EvaluatedScript;
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
import javax.annotation.Nullable;

/**
 * A central repository of unevaluated scripts which can be used for one or more arenas.
 */
public class PVScriptManager
        extends AbstractScriptManager<Script, EvaluatedScript>
        implements ScriptManager {

    private static IScriptFactory<Script> _scriptFactory = new IScriptFactory<Script>() {
        @Override
        public Script construct(String name, @Nullable File file, String type, String script) {
            return new PVScript(name, file, type, script);
        }
    };

    /*
     * Constructor.
     */
    public PVScriptManager(Plugin plugin, File scriptFolder) {
        super(plugin, scriptFolder, DirectoryTraversal.RECURSIVE);

        loadScripts();

        // register Generics script api
        registerApiType(new ScriptApiEconomy(PVStarAPI.getPlugin()));
        registerApiType(new ScriptApiInclude(PVStarAPI.getPlugin(), this));
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

    @Override
    public IScriptFactory<Script> getScriptConstructor() {
        return _scriptFactory;
    }

    /*
         * Register an api to be used in all evaluated scripts.
         */
    @Override
    public void registerApiType(IScriptApi api) {
        PreCon.notNull(api);

        addScriptApi(api);
    }

    /*
     * Do not evaluate scripts
     */
    @Override
    public void evaluate() {
        // do nothing
    }
}
