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


package com.jcwhatever.pvs.points;

import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.points.IPointsManager;
import com.jcwhatever.pvs.api.points.PointsType;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Points manager implementation
 */
public class PVPointsManager implements IPointsManager {

    private final Map<String, PointsType> _typeMap = new HashMap<>(25);

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

    @Override
    public List<PointsType> getTypes() {
        return new ArrayList<>(_typeMap.values());
    }

    @Override
    public List<PointsType> getTypes(IArena arena) {

        // get the arenas points type node
        IDataNode dataNode = arena.getDataNode("points");

        List<PointsType> results = new ArrayList<PointsType>(dataNode.size());

        for (IDataNode node : dataNode) {

            PointsType type = getType(node.getName());
            if (type == null)
                continue;

            results.add(type);
        }

        return results;
    }

    @Override
    @Nullable
    public PointsType getType(String typeName) {
        return _typeMap.get(typeName.toLowerCase());
    }

    @Override
    public void loadTypes(IArena arena) {

        IDataNode dataNode = arena.getDataNode("points");

        for (IDataNode node : dataNode) {

            PointsType type = getType(node.getName());
            if (type == null)
                continue;

            boolean isEnabled = node.getBoolean("enabled");

            if (isEnabled)
                type.add(arena);
        }
    }
}
