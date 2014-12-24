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

import com.jcwhatever.bukkit.generic.modules.ClassLoadMethod;
import com.jcwhatever.bukkit.generic.modules.IModuleInfo;
import com.jcwhatever.bukkit.generic.modules.JarModuleLoader;
import com.jcwhatever.bukkit.generic.utils.DependencyRunner;
import com.jcwhatever.bukkit.generic.utils.DependencyRunner.IFinishHandler;
import com.jcwhatever.bukkit.generic.utils.FileUtils.DirectoryTraversal;
import com.jcwhatever.bukkit.generic.utils.PreCon;
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
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.annotation.Nullable;

/**
 * Loads and stores PV-Star modules.
 */
public class ModuleLoader extends JarModuleLoader<PVStarModule> {

    private static final String MODULE_MANIFEST = "module.yml";

    private final File _moduleFolder;
    private final Map<String, PVModuleInfo> _moduleInfo = new HashMap<>(50);

    private Map<String, UnloadedModuleContainer> _unloadedModules = new HashMap<>(50);
    private boolean _isModulesLoaded;
    private KickPlayersBukkitListener _kickPlayersListener;

    /*
     * Constructor.
     */
    public ModuleLoader(Plugin plugin) {
        super(plugin, PVStarModule.class);

        _moduleFolder = new File(PVStarAPI.getPlugin().getDataFolder(), "modules");
        if (!_moduleFolder.exists() && !_moduleFolder.mkdirs()) {
            throw new RuntimeException("Failed to create PV-Star modules folder.");
        }

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
    public void enable(final Runnable onModulesEnabled) {
        PreCon.notNull(onModulesEnabled);
        PreCon.isValid(!_isModulesLoaded, "Modules can only be enabled once.");

        DependencyRunner<UnloadedModuleContainer> moduleEnabler =
                new DependencyRunner<UnloadedModuleContainer>(PVStarAPI.getPlugin());

        moduleEnabler.addAll(_unloadedModules.values());
        moduleEnabler.onFinish(new IFinishHandler<UnloadedModuleContainer>() {
            @Override
            public void onFinish(List<UnloadedModuleContainer> notRun) {

                // remove unloaded modules
                for (UnloadedModuleContainer container : notRun) {
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

                onModulesEnabled.run();

                HandlerList.unregisterAll(_kickPlayersListener);
            }
        });

        moduleEnabler.start();
    }

    @Override
    public File getModuleFolder() {
        return _moduleFolder;
    }

    @Override
    public DirectoryTraversal getDirectoryTraversal() {
        return DirectoryTraversal.NONE;
    }

    @Override
    protected void addModule(IModuleInfo info, PVStarModule instance) {
        super.addModule(info, instance);

        _unloadedModules.put(info.getSearchName(),
                new UnloadedModuleContainer(this, instance, (PVModuleInfo)info));
    }

    @Override
    protected ClassLoadMethod getLoadMethod(File file) {
        return ClassLoadMethod.DIRECT;
    }

    @Override
    protected String getModuleClassName(JarFile jarFile) {

        PVModuleInfo info = new PVModuleInfo(jarFile);
        if (!info.isValid())
            return null;

        _moduleInfo.put(info.getModuleClassName(), info);

        return info.getModuleClassName();
    }

    @Nullable
    @Override
    protected IModuleInfo createModuleInfo(PVStarModule moduleInstance) {
        return _moduleInfo.get(moduleInstance.getClass().getCanonicalName());
    }

    @Nullable
    @Override
    protected PVStarModule instantiateModule(Class<PVStarModule> clazz) {

        try {
            Constructor<PVStarModule> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException |
                InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected boolean isValidJarFile(@SuppressWarnings("unused") JarFile jarFile) {
        JarEntry moduleEntry = jarFile.getJarEntry(MODULE_MANIFEST);
        if (moduleEntry == null) {
            Msg.warning("Failed to load {0} because its missing its {1} file.",
                    jarFile.getName(), MODULE_MANIFEST);
            return false;
        }

        return true;
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
