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


package com.jcwhatever.bukkit.pvs.points;

import com.jcwhatever.generic.storage.IDataNode;
import com.jcwhatever.generic.utils.PreCon;
import com.jcwhatever.generic.utils.text.TextUtils;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.points.PointsManager;
import com.jcwhatever.bukkit.pvs.api.points.PointsType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Points manager implementation
 */
public class PVPointsManager implements PointsManager {

    private Map<String, PointsType> _typeMap = new HashMap<>(25);

    /*
     * Register a points type
     */
    @Override
    public boolean registerType(PointsType type) {
        PreCon.notNull(type);
        PreCon.isValid(TextUtils.isValidName(type.getName(), 32));

        // make sure a type with the same name isn't already registered.
        String key = type.getName().toLowerCase();
        if (_typeMap.containsKey(key))
            return false;

        _typeMap.put(key, type);

        return true;
    }

    /*
     * Get the registered points types.
     */
    @Override
    public List<PointsType> getTypes() {
        return new ArrayList<>(_typeMap.values());
    }

    /*
     * Get a list of registered points types
     * installed on the specified arena.
     */
    @Override
    public List<PointsType> getTypes(Arena arena) {

        // get the arenas points type node
        IDataNode dataNode = arena.getDataNode("points");

        Set<String> typeNames = dataNode.getSubNodeNames();
        List<PointsType> results = new ArrayList<PointsType>(typeNames.size());

        for (String typeName : typeNames) {

            PointsType type = getType(typeName);
            if (type == null)
                continue;

            results.add(type);
        }

        return results;
    }

    /*
     * Get a points type by name.
     */
    @Override
    @Nullable
    public PointsType getType(String typeName) {
        return _typeMap.get(typeName.toLowerCase());
    }

    /*
     * Enable points types on the specified arena.
     * Intended to be called once after an arena is loaded.
     */
    @Override
    public void loadTypes(Arena arena) {

        IDataNode dataNode = arena.getDataNode("points");

        Set<String> typeNames = dataNode.getSubNodeNames();

        for (String typeName : typeNames) {

            PointsType type = getType(typeName);
            if (type == null)
                continue;

            boolean isEnabled = dataNode.getBoolean(typeName + ".enabled");

            if (isEnabled)
                type.add(arena);
        }
    }
}
