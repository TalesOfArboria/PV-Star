package com.jcwhatever.bukkit.pvs.modules;

import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.modules.PVStarModule;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for an unloaded module with utilities
 * to check if dependencies are loaded.
 */
public class UnloadedModuleContainer {

    private final PVStarModule _module;
    private final PVModuleInfo _moduleInfo;
    private final ModuleLoader _loader;


    /*
     * Constructor.
     */
    public UnloadedModuleContainer(ModuleLoader loader, PVStarModule module, PVModuleInfo moduleInfo) {
        PreCon.notNull(loader);
        PreCon.notNull(module);
        PreCon.notNull(moduleInfo);

        _loader = loader;
        _module = module;
        _moduleInfo = moduleInfo;
    }

    /*
     * Get the name of the module.
     */
    public String getName() {
        return _moduleInfo.getName();
    }

    /*
     * Get the name of the module in lowercase.
     */
    public String getSearchName() {
        return _moduleInfo.getSearchName();
    }

    /*
     * Get the module.
     */
    public PVStarModule getModule() {
        return _module;
    }

    /*
     * Get the names of required Bukkit dependencies that are not
     * loaded yet.
     */
    public List<String> getMissingBukkitDepends() {

        List<String> result = new ArrayList<>(_moduleInfo.getBukkitDepends().size());

        for (String depend : _moduleInfo.getBukkitDepends()) {

            Plugin plugin = Bukkit.getPluginManager().getPlugin(depend);
            if (plugin == null || !plugin.isEnabled()) {
                result.add(depend);
            }
        }

        return result;
    }

    /*
     * Get the names of required PV-Star module dependencies that
     * are not loaded yet.
     */
    public List<String> getMissingModuleDepends() {

        List<String> result = new ArrayList<>(_moduleInfo.getModuleDepends().size());

        for (String depend : _moduleInfo.getModuleDepends()) {

            PVStarModule module = _loader.getModule(depend);
            if (module == null || !module.isEnabled()) {
                result.add(depend);
            }
        }

        return result;
    }

    /*
     * Determine if Bukkit dependencies are loaded.
     */
    public boolean isBukkitDependsLoaded() {
        for (String depend : _moduleInfo.getBukkitDepends()) {

            Plugin plugin = Bukkit.getPluginManager().getPlugin(depend);
            if (plugin == null || !plugin.isEnabled())
                return false;
        }

        for (String depend : _moduleInfo.getBukkitSoftDepends()) {

            Plugin plugin = Bukkit.getPluginManager().getPlugin(depend);
            if (plugin != null && !plugin.isEnabled())
                return false;
        }

        return true;
    }

    /*
     * Determine if PV-Star module dependencies are loaded.
     */
    public boolean isModuleDependsLoaded() {
        for (String depend : _moduleInfo.getModuleDepends()) {

            PVStarModule module = _loader.getModule(depend);
            if (module == null || !module.isEnabled())
                return false;
        }

        for (String depend : _moduleInfo.getModuleSoftDepends()) {

            PVStarModule module = _loader.getModule(depend);
            if (module != null && !module.isEnabled())
                return false;
        }

        return true;
    }

}
