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


package com.jcwhatever.bukkit.pvs.modules;

import com.jcwhatever.bukkit.generic.modules.JarModuleLoader;
import com.jcwhatever.bukkit.generic.utils.FileUtils;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.Scheduler;
import com.jcwhatever.bukkit.generic.utils.Scheduler.ScheduledTask;
import com.jcwhatever.bukkit.generic.utils.Scheduler.TaskHandler;
import com.jcwhatever.bukkit.pvs.PVStar;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.modules.ModuleInfo;
import com.jcwhatever.bukkit.pvs.api.modules.PVStarModule;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Loads and stores PV-Star modules.
 */
public class ModuleLoader {

    private final PVStar _pvStar;

    private Map<String, PVStarModule> _modules = new HashMap<>(50);
    private Map<String, UnloadedModuleContainer> _unloadedModules = new HashMap<>(50);
    private Map<PVStarModule, PVModuleInfo> _moduleInfo = new HashMap<>(50);

    private ScheduledTask _enablerTask;
    private Runnable _onModulesEnabled;
    private boolean _isModulesLoaded;
    private KickPlayersBukkitListener _kickPlayersListener;

    /*
     * Constructor.
     */
    public ModuleLoader(PVStar pvStar) {
        _pvStar = pvStar;

        _kickPlayersListener = new KickPlayersBukkitListener();
        Bukkit.getPluginManager().registerEvents(_kickPlayersListener, pvStar);
    }

    /*
     * Determine if modules are loaded.
     */
    public boolean isModulesLoaded() {
        return _isModulesLoaded;
    }

    /*
     * Enable modules. Can only be called once.
     */
    public void enable(Runnable onModulesEnabled) {
        PreCon.notNull(onModulesEnabled);
        PreCon.isValid(_enablerTask == null, "Enable method can only be called once.");

        _onModulesEnabled = onModulesEnabled;
        _enablerTask = Scheduler.runTaskRepeat(PVStarAPI.getPlugin(), 1, 10, new ModuleEnabler());

        Scheduler.runTaskLater(PVStarAPI.getPlugin(), 20 * 20, new Runnable() {

            @Override
            public void run() {
                if (!_enablerTask.isCancelled())
                    _enablerTask.cancel();
            }
        });
    }

    /*
     * Get a module by name
     */
    @Nullable
    public PVStarModule getModule(String moduleName) {
        PreCon.notNullOrEmpty(moduleName);

        return _modules.get(moduleName.toLowerCase());
    }

    /*
     * Get information about a module.
     */
    @Nullable
    public ModuleInfo getModuleInfo(PVStarModule module) {
        return _moduleInfo.get(module);
    }

    /*
     * Get all modules.
     */
    public List<PVStarModule> getModules() {
        return new ArrayList<>(_modules.values());
    }

    /*
     * Load module classes
     */
    public void loadModules() {

        JarModuleLoader<PVStarModule> moduleLoader = new JarModuleLoader<PVStarModule>();

        File moduleDir = new File(_pvStar.getDataFolder(), "modules");

        if (moduleDir.exists() || moduleDir.mkdirs()) {

            // load modules
            List<PVStarModule> modules = moduleLoader.loadModules(PVStarModule.class, moduleDir);

            Map<String, PVModuleInfo> namedModuleInfo = new HashMap<>(modules.size());

            for (PVStarModule module : modules) {

                // get "module.yml" file from module
                String moduleInfoString = FileUtils.scanTextFile(module.getClass(), "/module.yml", StandardCharsets.UTF_8);
                if (moduleInfoString == null) {

                    Msg.warning("Failed to load module '{0}' because its missing its module.yml file.",
                            module.getClass().getName());

                    continue;
                }

                // get module info from yaml string
                PVModuleInfo moduleInfo = new PVModuleInfo(moduleInfoString);
                if (!moduleInfo.isLoaded()) {

                    Msg.warning("Failed to load module '{0}' because its module.yml file is missing required information.",
                            module.getClass().getName());

                    continue;
                }

                PVStarModule current = _modules.get(moduleInfo.getName());
                PVModuleInfo currentInfo = namedModuleInfo.get(moduleInfo.getName());

                // see if module is already loaded and only replace if current is a lesser version.
                if (current != null && currentInfo.getLogicalVersion() >= moduleInfo.getLogicalVersion()) {
                    continue;
                }

                namedModuleInfo.put(moduleInfo.getSearchName(), moduleInfo);

                _modules.put(moduleInfo.getSearchName(), module);
                _moduleInfo.put(module, moduleInfo);
                _unloadedModules.put(moduleInfo.getSearchName(), new UnloadedModuleContainer(this, module, moduleInfo));
            }
        }
        else {
            Msg.warning("Failed to load PV-Star modules.");
        }
    }

    /*
     * module enabler task handler
     */
    private class ModuleEnabler extends TaskHandler {

        @Override
        public void run() {

            LinkedList<UnloadedModuleContainer> containers = new LinkedList<>(_unloadedModules.values());

            while (!containers.isEmpty()) {

                UnloadedModuleContainer container = containers.remove();

                if (container.isBukkitDependsLoaded() && container.isModuleDependsLoaded()) {

                    // Pre-enable module
                    container.getModule().preEnable();

                    _unloadedModules.remove(container.getSearchName());
                }
            }

            if (_unloadedModules.isEmpty())
                cancelTask();
        }

        /*
         * Called when all modules are pre-init or
         * time to pre-init expires.
         */
        @Override
        protected void onCancel() {

            // remove unloaded modules
            for (UnloadedModuleContainer container : _unloadedModules.values()) {
                _modules.remove(container.getSearchName());
                _moduleInfo.remove(container.getModule());

                Msg.warning("[{0}] Failed to load module because required dependencies are missing:", container.getName());

                List<String> bukkitDepends = container.getMissingBukkitDepends();
                for (String depend : bukkitDepends) {
                    Msg.warning("[{0}] Missing Bukkit Plugin: {1}", container.getName(), depend);
                }

                List<String> moduleDepends = container.getMissingModuleDepends();
                for (String depend : moduleDepends) {
                    Msg.warning("[{0}] Missing PV-Star Module: {1}", container.getName(), depend);
                }
            }

            _unloadedModules.clear();

            // Enable Modules
            for (PVStarModule module : _modules.values()) {
                module.enable();
                Msg.info("[{0}] Module enabled.", module.getName());
            }

            _isModulesLoaded = true;

            _onModulesEnabled.run();

            HandlerList.unregisterAll(_kickPlayersListener);
        }
    }

    /*
     * kick players to prevent actions taken on login events
     * from being missed while modules are loading.
     */
    private class KickPlayersBukkitListener implements Listener {

        /*
         * Ensure players do not log in before modules are loaded.
         */
        @EventHandler(priority = EventPriority.HIGHEST)
        private void onPlayerJoin(AsyncPlayerPreLoginEvent event) {

            if (!_isModulesLoaded)
                event.disallow(Result.KICK_OTHER, "PV-Star is still loading.");
        }
    }
}
