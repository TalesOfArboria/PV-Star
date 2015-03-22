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

import com.jcwhatever.nucleus.utils.modules.YamlModuleInfo;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.modules.ModuleInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/*
 * Loads and stores module info from module.yml file
 * in the module jar file.
 */
public class PVModuleInfo extends YamlModuleInfo implements ModuleInfo {

    private String _version;
    private String _moduleClassName;
    private String _description;
    private long _logicalVersion;
    private List<String> _authors;
    private Set<String> _bukkitDepends;
    private Set<String> _bukkitSoftDepends;
    private Set<String> _moduleDepends;
    private Set<String> _moduleSoftDepends;

    /*
     * Constructor. Pass in module.yml text string.
     */
    public PVModuleInfo(JarFile jarFile) {
        super(PVStarAPI.getPlugin(), "module.yml", jarFile);
    }

    /**
     * Get the module class name.
     */
    public String getModuleClassName() {
        return _moduleClassName;
    }

    @Override
    public String getVersion() {
        return _version;
    }

    @Override
    public String getDescription() {
        return _description;
    }

    @Override
    public List<String> getAuthors() {
        return _authors;
    }

    @Override
    public long getLogicalVersion() {
        return _logicalVersion;
    }

    @Override
    public Set<String> getBukkitDepends() {
        return _bukkitDepends;
    }

    @Override
    public Set<String> getBukkitSoftDepends() {
        return _bukkitSoftDepends;
    }

    @Override
    public Set<String> getModuleDepends() {
        return _moduleDepends;
    }

    @Override
    public Set<String> getModuleSoftDepends() {
        return _moduleSoftDepends;
    }

    @Override
    protected boolean onLoad(IDataNode dataNode) {
        // get the required logical version
        _logicalVersion = dataNode.getLong("logical-version", -1);
        if (_logicalVersion < 0)
            return false;

        // get the optional display version
        _version = dataNode.getString("display-version", "v" + _logicalVersion);

        // get the optional description
        _description = dataNode.getString("description", "");

        _moduleClassName = dataNode.getString("module");
        if (_moduleClassName == null)
            return false;

        // get module authors
        String rawAuthors = dataNode.getString("authors");

        if (rawAuthors == null) {
            _authors = Collections.unmodifiableList(new ArrayList<String>(0));
        }
        else {
            String[] authorArray = TextUtils.PATTERN_COMMA.split(rawAuthors);
            ArrayList<String> authors = new ArrayList<>(authorArray.length);

            for (String author : authorArray) {
                authors.add(author.trim());
            }

            _authors = Collections.unmodifiableList(authors);
        }

        // get Bukkit dependencies
        _bukkitDepends = getDepends(dataNode, "bukkit-depends");

        // get Bukkit soft dependencies
        _bukkitSoftDepends = getDepends(dataNode, "bukkit-soft-depends");

        // get PV-Star module dependencies
        _moduleDepends = getDepends(dataNode, "depends");

        // get PV-Star module soft dependencies
        _moduleSoftDepends = getDepends(dataNode, "soft-depends");

        return true;
    }

    /*
     * Get dependencies from the specified module data node.
     */
    private Set<String> getDepends(IDataNode moduleNode, String nodeName) {

        List<String> depends = moduleNode.getStringList(nodeName,
                null /* null to prevent unnecessary object creation*/);

        if (depends == null)
            depends = new ArrayList<>(0);

        return Collections.unmodifiableSet(new HashSet<>(depends));
    }
}
