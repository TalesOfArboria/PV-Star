package com.jcwhatever.bukkit.pvs.points;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.points.PointsManager;
import com.jcwhatever.bukkit.pvs.api.points.PointsType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
