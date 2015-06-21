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


package com.jcwhatever.pvs;

import com.jcwhatever.nucleus.events.manager.IEventListener;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.CollectionUtils;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.pvs.api.arena.extensions.ArenaExtension.ArenaExtensionRegistration;
import com.jcwhatever.pvs.api.arena.extensions.ArenaExtensionInfo;
import com.jcwhatever.pvs.api.arena.extensions.IArenaExtensionManager;
import com.jcwhatever.pvs.exceptions.MissingExtensionAnnotation;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * PVStar implementation of {@link IArenaExtensionManager}.
 */
public class ArenaExtensionManager implements IArenaExtensionManager, IEventListener {

    private static final ArenaExtensionRegistration REGISTRATION = new ArenaExtensionRegistration();

    private final IArena _arena;
    private final Set<ArenaExtension> _extensions = new HashSet<ArenaExtension>(15);
    private final Map<String, ArenaExtension> _loadedMap = new HashMap<String, ArenaExtension>(15);
    private final IDataNode _dataNode;

    /**
     * Constructor.
     *
     * @param arena  The owning arena.
     */
    public ArenaExtensionManager(IArena arena) {
        PreCon.notNull(arena);

        _arena = arena;
        _dataNode = arena.getDataNode("extensions");

        load();

        arena.getEventManager().register(this);
    }

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    public IArena getArena() {
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
            throw new MissingExtensionAnnotation(clazz);

        return _loadedMap.containsKey(info.name().toLowerCase());
    }

    @Override
    public Set<ArenaExtension> getAll() {
        return CollectionUtils.unmodifiableSet(_extensions);
    }

    @Nullable
    @Override
    public ArenaExtension get(String name) {
        PreCon.notNullOrEmpty(name);

        return _loadedMap.get(name.toLowerCase());
    }


    @Nullable
    @Override
    public <T extends ArenaExtension> T get(Class<T> clazz) {
        PreCon.notNull(clazz);

        ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotation(clazz);

        ArenaExtension module = _loadedMap.get(info.name().toLowerCase());
        if (module == null)
            return null;

        @SuppressWarnings("unchecked")
        T result = (T)module;

        return result;
    }

    @Nullable
    @Override
    public <T extends ArenaExtension> T add(String name) {
        PreCon.notNullOrEmpty(name);

        name = name.toLowerCase();

        Class<? extends ArenaExtension> clazz = PVStarAPI.getExtensionManager().getExtensionClass(name);
        if (clazz == null)
            return null;

        ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotation(clazz);

        ArenaExtension extension = loadExtension(clazz, true);
        if (extension == null)
            return null;

        IDataNode extNode = _dataNode.getNode(name);
        extNode.set("enabled", true);
        extNode.save();

        @SuppressWarnings("unchecked")
        T result = (T)extension;

        return result;
    }

    @Override
    public boolean remove(Class<? extends ArenaExtension> clazz) {
        PreCon.notNull(clazz);

        ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotation(clazz);

        return remove(info.name());
    }

    @Override
    public boolean remove(String name) {
        PreCon.notNullOrEmpty(name);

        name = name.toLowerCase();

        ArenaExtension extension = _loadedMap.remove(name);
        if (extension == null)
            return false;

        REGISTRATION.remove(extension);
        _extensions.remove(extension);

        IDataNode extNode = _dataNode.getNode(name);
        extNode.remove();
        extNode.save();

        return true;
    }

    @Nullable
    private ArenaExtension loadExtension(Class<? extends ArenaExtension> clazz, boolean isInitialAttach) {

        ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotation(clazz);

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

        _extensions.add(extension);
        _loadedMap.put(info.name().toLowerCase(), extension);

        extension.register(REGISTRATION);
        REGISTRATION.attach(extension, info, getArena(), isInitialAttach);

        if (_arena.getSettings().isEnabled())
            REGISTRATION.enable(extension);

        return extension;
    }

    /**
     * Enable all extensions.
     */
    public void enable() {
        for (ArenaExtension extension :_extensions) {
            REGISTRATION.enable(extension);
            _loadedMap.put(extension.getName().toLowerCase(), extension);
        }
    }

    /**
     * Disable all extensions.
     */
    public void disable() {
        for (ArenaExtension extension : _loadedMap.values()) {
            REGISTRATION.disable(extension);
        }
        _loadedMap.clear();
    }

    private void load() {

        for (IDataNode node : _dataNode) {

            Class<? extends ArenaExtension> clazz =
                    PVStarAPI.getExtensionManager().getExtensionClass(node.getName());
            if (clazz == null)
                continue;

            loadExtension(clazz, false);
        }
    }
}
