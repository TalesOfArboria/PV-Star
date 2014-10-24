package com.jcwhatever.bukkit.pvs.scripting;

import com.jcwhatever.bukkit.generic.scripting.GenericsScriptManager;
import com.jcwhatever.bukkit.generic.scripting.IScript;
import com.jcwhatever.bukkit.generic.scripting.ScriptHelper.ScriptConstructor;
import com.jcwhatever.bukkit.generic.scripting.api.IScriptApi;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiEconomy;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiInclude;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiInventory;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiItemBank;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiLoader;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiMsg;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiPermissions;
import com.jcwhatever.bukkit.generic.scripting.api.ScriptApiSounds;
import com.jcwhatever.bukkit.generic.utils.FileUtils.DirectoryTraversal;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.events.AbstractArenaEvent;
import com.jcwhatever.bukkit.pvs.api.modules.PVStarModule;
import com.jcwhatever.bukkit.pvs.api.scripting.Script;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptManager;
import com.jcwhatever.bukkit.pvs.scripting.api.EventsApi;
import com.jcwhatever.bukkit.pvs.scripting.api.PlayerApi;
import com.jcwhatever.bukkit.pvs.scripting.api.SchedulerApi;
import com.jcwhatever.bukkit.pvs.scripting.api.SpawnApi;
import com.jcwhatever.bukkit.pvs.scripting.api.StatsApi;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A central repository of unevaluated scripts which can be used for one or more arenas.
 */
public class PVScriptManager implements ScriptManager {

    private final GenericsScriptManager _scriptRepository;
    private final Map<String, IScriptApi> _apiMap = new HashMap<>(30);
    private final File _scriptFolder;
    private final EventsApi _eventsApi;

    /*
     * Constructor.
     */
    public PVScriptManager(Plugin plugin, File scriptFolder, ScriptEngineManager engineManager) {
        PreCon.notNull(plugin);
        PreCon.notNull(scriptFolder);
        PreCon.notNull(engineManager);

        _eventsApi = new EventsApi();

        _scriptFolder = scriptFolder;
        _scriptRepository = new GenericsScriptManager(plugin, engineManager) {

            @Override
            public ScriptConstructor<IScript> getScriptConstructor() {
                return new ScriptConstructor<IScript>() {
                    @Override
                    public IScript construct(String name, String type, String script) {
                        return new PVScript(name, type, script);
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
        registerApiType(new ScriptApiLoader(PVStarAPI.getPlugin()));

        // register PV-Star script api
        registerApiType(_eventsApi);
        registerApiType(new PlayerApi());
        registerApiType(new SchedulerApi());
        registerApiType(new SpawnApi());
        registerApiType(new StatsApi());
    }


    /*
     * Get the engine manager used to get script engines.
     */
    @Override
    public ScriptEngineManager getEngineManager() {

        return _scriptRepository.getEngineManager();
    }

    /*
     * Register an event so scripts can easily attach event handlers to it.
     * For internal registrations of PV-Star events which have no module prefix.
     */
    public void registerEventType(Class<? extends AbstractArenaEvent> eventClass) {
        PreCon.notNull(eventClass);

        _eventsApi.registerEventType("", eventClass);
    }

    /*
     * Register  an event so scripts can easily attach event handlers to it.
     * The event name used by scripts is ModuleName:EventClassName, non case sensitive.
     */
    @Override
    public void registerEventType(PVStarModule module, Class<? extends AbstractArenaEvent> eventClass) {
        PreCon.notNull(module);
        PreCon.notNull(eventClass);

        _eventsApi.registerEventType(module.getName().toLowerCase() + ':', eventClass);
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
