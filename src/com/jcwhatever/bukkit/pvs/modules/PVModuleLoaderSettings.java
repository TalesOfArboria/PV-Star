/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.bukkit.pvs.modules;

import com.jcwhatever.bukkit.generic.modules.ClassLoadMethod;
import com.jcwhatever.bukkit.generic.modules.IModuleFactory;
import com.jcwhatever.bukkit.generic.modules.IModuleInfo;
import com.jcwhatever.bukkit.generic.modules.IModuleInfoFactory;
import com.jcwhatever.bukkit.generic.modules.JarModuleLoader;
import com.jcwhatever.bukkit.generic.modules.JarModuleLoaderSettings;
import com.jcwhatever.bukkit.generic.utils.FileUtils;
import com.jcwhatever.bukkit.generic.utils.FileUtils.DirectoryTraversal;
import com.jcwhatever.bukkit.generic.utils.IEntryValidator;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.modules.PVStarModule;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.annotation.Nullable;

/*
 * 
 */
public final class PVModuleLoaderSettings extends JarModuleLoaderSettings<PVStarModule> {

    private static final String MODULE_MANIFEST = "module.yml";

    private final File _moduleFolder;
    private final Map<String, PVModuleInfo> _moduleInfo = new HashMap<>(50);


    PVModuleLoaderSettings() {

        // get module folder
        _moduleFolder = new File(PVStarAPI.getPlugin().getDataFolder(), "modules");
        if (!_moduleFolder.exists() && !_moduleFolder.mkdirs()) {
            throw new RuntimeException("Failed to create PV-Star modules folder.");
        }
    }


    @Override
    @Nullable
    public File getModuleFolder() {
        return _moduleFolder;
    }

    @Override
    public DirectoryTraversal getDirectoryTraversal() {
        return DirectoryTraversal.NONE;
    }

    @Override
    public ClassLoadMethod getClassLoadMethod() {
        return ClassLoadMethod.DIRECT;
    }

    /**
     * Get the factory used to provide the class name to
     * load from a jar file when the {@code ClassLoadMethod}
     * is set to {@code DIRECT}.
     */
    @Override
    @Nullable
    public IClassNameFactory<PVStarModule> getClassNameFactory() {
        return new IClassNameFactory<PVStarModule>() {
            @Nullable
            @Override
            public String getClassName(JarFile jarFile, JarModuleLoader<PVStarModule> loader) {

                JarEntry entry = jarFile.getJarEntry(MODULE_MANIFEST);

                InputStream stream = null;
                String moduleInfoString = null;
                try {
                     stream = jarFile.getInputStream(entry);

                     moduleInfoString = FileUtils.scanTextFile(stream, StandardCharsets.UTF_8, 50);

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (moduleInfoString == null) {
                    return null;
                }

                PVModuleInfo moduleInfo = new PVModuleInfo(moduleInfoString);
                if (!moduleInfo.isLoaded()) {
                    Msg.warning("Failed to load module '{0}' because its {1} file is missing " +
                                    "required information.",
                            jarFile.getName(), MODULE_MANIFEST);
                    return null;
                }

                PVStarModule current = loader.getModule(moduleInfo.getName());
                PVModuleInfo currentInfo = _moduleInfo.get(moduleInfo.getModuleClassName());
                // see if module is already loaded and only replace if current is a lesser version.
                if (current != null && currentInfo != null) {

                    if (currentInfo.getLogicalVersion() >= moduleInfo.getLogicalVersion()) {
                        return null;
                    }
                }

                _moduleInfo.put(moduleInfo.getModuleClassName(), moduleInfo);

                return moduleInfo.getModuleClassName();
            }
        };
    }

    @Override
    public IModuleInfoFactory<PVStarModule> getModuleInfoFactory() {
        return new IModuleInfoFactory<PVStarModule>() {

            @Override
            public IModuleInfo create(PVStarModule module, JarModuleLoader<PVStarModule> loader) {

                return _moduleInfo.get(module.getClass().getCanonicalName());
            }
        };
    }

    @Override
    public IModuleFactory<PVStarModule> getModuleFactory() {
        return new IModuleFactory<PVStarModule>() {
            @Override
            public PVStarModule create(Class<PVStarModule> clazz, JarModuleLoader<PVStarModule> loader)
                    throws InstantiationException, IllegalAccessException,
                    NoSuchMethodException, InvocationTargetException {

                Constructor<PVStarModule> constructor = clazz.getConstructor();
                return constructor.newInstance();
            }
        };
    }

    @Override
    public IEntryValidator<JarFile> getJarValidator() {
        return new IEntryValidator<JarFile>() {
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
}
