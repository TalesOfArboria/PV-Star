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

import com.jcwhatever.bukkit.generic.modules.IModuleFactory;
import com.jcwhatever.bukkit.generic.modules.IModuleInfo;
import com.jcwhatever.bukkit.generic.modules.IModuleInfoFactory;
import com.jcwhatever.bukkit.generic.modules.JarModuleLoader;
import com.jcwhatever.bukkit.generic.modules.JarModuleLoaderSettings;
import com.jcwhatever.bukkit.generic.scheduler.ScheduledTask;
import com.jcwhatever.bukkit.generic.scheduler.TaskHandler;
import com.jcwhatever.bukkit.generic.utils.EntryValidator;
import com.jcwhatever.bukkit.generic.utils.FileUtils;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.Scheduler;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.modules.PVStarModule;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads and stores PV-Star modules.
 */
public class ModuleLoader extends JarModuleLoader<PVStarModule> {

    private static final String MODULE_MANIFEST = "module.yml";

    private final ModuleLoader _moduleLoaderInstance;

    private Map<String, UnloadedModuleContainer> _unloadedModules = new HashMap<>(50);

    private ScheduledTask _enablerTask;
    private Runnable _onModulesEnabled;
    private boolean _isModulesLoaded;
    private KickPlayersBukkitListener _kickPlayersListener;

    /*
     * Constructor.
     */
    public ModuleLoader(Class<PVStarModule> moduleClass,
                        JarModuleLoaderSettings<PVStarModule> settings) {
        super(moduleClass, settings);

        _moduleLoaderInstance = this;

        _kickPlayersListener = new KickPlayersBukkitListener();
        Bukkit.getPluginManager().registerEvents(_kickPlayersListener, PVStarAPI.getPlugin());
    }

    /*
     * Determine if modules are loaded and enabled.
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

    @Override
    public EntryValidator<JarFile> getDefaultJarValidator() {
        return new EntryValidator<JarFile>() {
            @Override
            public boolean isValid(JarFile entry) {
                JarEntry moduleEntry = entry.getJarEntry(MODULE_MANIFEST);
                if (moduleEntry == null) {
                    Msg.warning("Failed to load {0} because its missing its {1} file.",
                            entry.getName(), MODULE_MANIFEST);
                    return false;
                }

                return true;
            }
        };
    }

    @Override
    public IModuleFactory<PVStarModule> getDefaultModuleFactory() {

        return new IModuleFactory<PVStarModule>() {
            @Override
            public PVStarModule create(Class<PVStarModule> clazz)
                    throws InstantiationException, IllegalAccessException,
                    NoSuchMethodException, InvocationTargetException {

                Constructor<PVStarModule> constructor = clazz.getConstructor();
                return constructor.newInstance();
            }
        };
    }

    @Override
    public IModuleInfoFactory<PVStarModule> getDefaultModuleInfoFactory() {
        return new IModuleInfoFactory<PVStarModule>() {

            private Map<String, PVModuleInfo> namedModuleInfo = new HashMap<>(20);

            @Override
            public IModuleInfo create(PVStarModule module) {

                // get "module.yml" file from module
                String moduleInfoString = FileUtils.scanTextFile(module.getClass(),
                        '/' + MODULE_MANIFEST, StandardCharsets.UTF_8);

                if (moduleInfoString == null) {
                    // jar validator prevents loading jars with no module.yml file
                    throw new AssertionError();
                }

                // get module info from yaml string
                PVModuleInfo moduleInfo = new PVModuleInfo(moduleInfoString);
                if (!moduleInfo.isLoaded()) {

                    Msg.warning("Failed to load module '{0}' because its {1} file is missing " +
                                    "required information.",
                            module.getClass().getName(), MODULE_MANIFEST);

                    return null;
                }

                PVStarModule current = getModule(moduleInfo.getName());
                PVModuleInfo currentInfo = namedModuleInfo.get(moduleInfo.getSearchName());

                // see if module is already loaded and only replace if current is a lesser version.
                if (current != null && currentInfo != null &&
                        currentInfo.getLogicalVersion() >= moduleInfo.getLogicalVersion()) {
                    return null;
                }

                namedModuleInfo.put(moduleInfo.getSearchName(), moduleInfo);

                return moduleInfo;
            }
        };
    }

    @Override
    protected void addModule(IModuleInfo info, PVStarModule instance) {
        super.addModule(info, instance);

        _unloadedModules.put(info.getSearchName(),
                new UnloadedModuleContainer(_moduleLoaderInstance, instance, (PVModuleInfo)info));
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
                removeModule(container.getSearchName());

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
            for (PVStarModule module : getModules()) {
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
