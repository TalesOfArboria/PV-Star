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


package com.jcwhatever.bukkit.pvs;

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtensionInfo;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ExtensionTypeManager;
import com.jcwhatever.bukkit.pvs.api.exceptions.InvalidNameException;
import com.jcwhatever.bukkit.pvs.api.exceptions.MissingExtensionAnnotationException;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Repository of arena extension types.
 */
public class PVExtensionTypeManager implements ExtensionTypeManager {

    private static final Map<String, Class<? extends ArenaExtension>> _extensionMap = new HashMap<>(25);

    @Override
    public Set<String> getExtensionNames() {
        return _extensionMap.keySet();
    }

    @Override
    public List<Class<? extends ArenaExtension>> getExtensionClasses() {
        return new ArrayList<>( _extensionMap.values());
    }

    @Nullable
    @Override
    public Class<? extends ArenaExtension> getExtensionClass(String name) {
        return _extensionMap.get(name.toLowerCase());
    }

    @Override
    public void registerType(Class<? extends ArenaExtension> extension) {
        PreCon.notNull(extension);

        ArenaExtensionInfo info = extension.getAnnotation(ArenaExtensionInfo.class);
        if (info == null)
            throw new MissingExtensionAnnotationException(extension);

        if (!TextUtils.isValidName(info.name(), 32)) {
            throw new InvalidNameException("Arena Extension with an invalid name was detected: " + info.name());
        }

        String key = info.name().toLowerCase();

        if (_extensionMap.containsKey(key)) {
            Msg.warning("An Arena Extension was registered that overwrites another " +
                    "extension. Extension name: {0}.", info.name());
        }

        _extensionMap.put(key, extension);
    }
}
