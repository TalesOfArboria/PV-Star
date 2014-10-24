package com.jcwhatever.bukkit.pvs;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtensionInfo;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtensionManager;
import com.jcwhatever.bukkit.pvs.api.exceptions.MissingExtensionAnnotationException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class PVArenaExtensionManager extends ArenaExtensionManager {

    private final Arena _arena;
    private final Set<ArenaExtension> _extensions = new HashSet<ArenaExtension>(15);
    private final Map<String, ArenaExtension> _loadedMap = new HashMap<String, ArenaExtension>(15);
    private final IDataNode _dataNode;

    public PVArenaExtensionManager(Arena arena) {
        PreCon.notNull(arena);

        _arena = arena;
        _dataNode = arena.getDataNode("extensions");

        load();
    }

    @Override
    public Arena getArena() {
        return _arena;
    }

    @Override
    public boolean has(String typeName) {
        PreCon.notNullOrEmpty(typeName);

        return _loadedMap.containsKey(typeName);
    }

    @Override
    public boolean has(Class<? extends ArenaExtension> clazz) {
        PreCon.notNull(clazz);

        ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotationException(clazz);

        return _loadedMap.containsKey(info.name().toLowerCase());
    }

    @Override
    public Set<ArenaExtension> getAll() {
        return new HashSet<ArenaExtension>(_extensions);
    }

    @Nullable
    @Override
    public ArenaExtension get(String name) {
        PreCon.notNullOrEmpty(name);

        return _loadedMap.get(name.toLowerCase());
    }


    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ArenaExtension> T get(Class<T> clazz) {
        PreCon.notNull(clazz);

        ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotationException(clazz);

        ArenaExtension module = _loadedMap.get(info.name().toLowerCase());
        if (module == null)
            return null;

        return (T)module;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ArenaExtension> T add(String name) {
        PreCon.notNullOrEmpty(name);

        name = name.toLowerCase();

        Class<? extends ArenaExtension> clazz = PVStarAPI.getExtensionManager().getExtensionClass(name);
        if (clazz == null)
            return null;

        ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotationException(clazz);

        ArenaExtension extension = loadExtension(clazz);
        if (extension == null)
            return null;

        IDataNode extNode = _dataNode.getNode(name);
        extNode.set("enabled", true);
        extNode.saveAsync(null);

        extension.enable();

        return (T)extension;
    }

    @Override
    public boolean enableExtension(String name) {
        PreCon.notNullOrEmpty(name);

        ArenaExtension extension = get(name);
        if (extension == null)
            return false;

        extension.enable();
        return true;
    }

    @Override
    public boolean disableExtension(String name) {
        PreCon.notNullOrEmpty(name);

        ArenaExtension extension = get(name);
        if (extension == null)
            return false;

        extension.disable();
        return true;
    }

    @Override
    public boolean remove(Class<? extends ArenaExtension> clazz) {
        PreCon.notNull(clazz);

        ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotationException(clazz);

        return remove(info.name());
    }

    @Override
    public boolean remove(String name) {
        PreCon.notNullOrEmpty(name);

        name = name.toLowerCase();

        ArenaExtension module = _loadedMap.remove(name);
        if (module == null)
            return false;

        module.disable();
        module.dispose();
        _extensions.remove(module);

        return true;
    }

    @Nullable
    private ArenaExtension loadExtension(Class<? extends ArenaExtension> clazz) {

        ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotationException(clazz);

        ArenaExtension extension = _loadedMap.get(info.name().toLowerCase());
        if (extension != null)
            return extension;

        try {
            extension = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        initExtension(extension, info);

        _extensions.add(extension);
        _loadedMap.put(info.name().toLowerCase(), extension);

        return extension;
    }

    private void load() {
        Set<String> extNames = _dataNode.getSubNodeNames();

        if (extNames != null && !extNames.isEmpty()) {
            for (String name : extNames) {

                boolean isEnabled = _dataNode.getNode(name).getBoolean("enabled");



                Class<? extends ArenaExtension> clazz = PVStarAPI.getExtensionManager().getExtensionClass(name);
                if (clazz == null)
                    continue;

                ArenaExtension extension = loadExtension(clazz);
                if (extension == null)
                    continue;

                if (isEnabled)
                    extension.enable();
            }
        }
    }

}
