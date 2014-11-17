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


package com.jcwhatever.bukkit.pvs.arenas.settings;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.arena.settings.ArenaSettings;
import com.jcwhatever.bukkit.pvs.api.events.ArenaDisabledEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaEnabledEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaPreEnableEvent;

import org.bukkit.Location;

import java.util.Set;
import javax.annotation.Nullable;

/**
 * Arena settings implementation.
 */
public class PVArenaSettings implements ArenaSettings {

    private final Arena _arena;
    private final IDataNode _dataNode;

    private int _minPlayers = 2;
    private int _maxPlayers = 10;
    private boolean _isVisible = true;
    private boolean _isEnabled = true;
    private boolean _isMobSpawnEnabled = false;
    private Location _removeLocation;
    private String _typeDisplayName;

    /*
     * Constructor.
     */
    public PVArenaSettings(Arena arena) {
        PreCon.notNull(arena);

        _arena = arena;
        _dataNode = arena.getDataNode("settings.arena");

        _isEnabled = _dataNode.getBoolean("enabled", _isEnabled);
        _minPlayers = _dataNode.getInteger("min-players", _minPlayers);
        _maxPlayers = _dataNode.getInteger("max-players", _maxPlayers);
        _isVisible = _dataNode.getBoolean("visible", _isVisible);
        _isMobSpawnEnabled = _dataNode.getBoolean("mob-spawn", _isMobSpawnEnabled);
        _removeLocation = _dataNode.getLocation("remove-location", _removeLocation);
        _typeDisplayName = _dataNode.getString("type-display");
    }

    /*
     * Determine if the arena is enabled.
     */
    @Override
    public boolean isEnabled() {
        return _isEnabled && _arena.getRegion().isDefined();
    }

    /*
     * Set the arena enabled state.
     */
    @Override
    public void setEnabled(boolean isEnabled) {

        if (_isEnabled == isEnabled)
            return;

        if (isEnabled) {

            if (_arena.getEventManager().call(new ArenaPreEnableEvent(_arena)).isCancelled())
                return;

            _isEnabled = true;

            // Enable all extensions when arena is enabled.
            Set<ArenaExtension> extensions = _arena.getExtensionManager().getAll();
            for (ArenaExtension extension : extensions) {
                extension.enable();
            }

            _arena.getEventManager().call(new ArenaEnabledEvent(_arena));
        }
        else {
            _isEnabled = false;

            _arena.getGameManager().end();

            // Disable all extensions when arena is disabled.
            Set<ArenaExtension> extensions = _arena.getExtensionManager().getAll();
            for (ArenaExtension extension : extensions) {
                extension.disable();
            }

            _arena.getEventManager().call(new ArenaDisabledEvent(_arena));
        }

        _dataNode.set("enabled", _isEnabled);
        _dataNode.saveAsync(null);
    }

    /*
     * Determine if the arena is visible to players
     * in arena lists and joinable via commands.
     */
    @Override
    public final boolean isVisible() {
        return _isVisible;
    }

    /*
     * Set the arenas visibility to players.
     */
    @Override
    public final void setIsVisible(boolean isVisible) {
        _isVisible = isVisible;

        _dataNode.set("visible", isVisible);
        _dataNode.saveAsync(null);
    }

    /*
     * Get a custom type name for display.
     */
    @Override
    public final String getTypeDisplayName() {
        return _typeDisplayName != null
                ? _typeDisplayName
                : "Arena";
    }

    /*
     * Set a custom type display name for the arena
     */
    @Override
    public final void setTypeDisplayName(@Nullable String typeDisplayName) {

        _typeDisplayName = typeDisplayName;

        _dataNode.set("type-display", typeDisplayName);
        _dataNode.saveAsync(null);
    }

    /*
     * Get the minimum players needed to play the arena.
     */
    @Override
    public int getMinPlayers() {
        return _minPlayers;
    }

    /*
     * Set the minimum players needed to play the arena.
     */
    @Override
    public void setMinPlayers(int minPlayers) {
        _minPlayers = minPlayers;

        save("min-players", minPlayers);
    }

    /*
     * Get the maximum players allowed in the arena.
     */
    @Override
    public int getMaxPlayers() {
        return _maxPlayers;
    }

    /*
     * Set the maximum players allowed in the arena.
     */
    @Override
    public void setMaxPlayers(int maxPlayers) {
        _maxPlayers = maxPlayers;

        save("max-players", maxPlayers);
    }

    /*
     * Determine if natural mob spawns are enabled
     * in the arena.
     */
    @Override
    public boolean isMobSpawnEnabled() {
        return _isMobSpawnEnabled;
    }

    /*
     * Set flag for natural mob spawns in the arena.
     */
    @Override
    public void setMobSpawnEnabled(boolean isEnabled) {
        _isMobSpawnEnabled = isEnabled;

        save("mob-spawn", isEnabled);
    }

    /*
     * Get the location a player is teleported to when
     * they are removed from the arena.
     */
    @Override
    @Nullable
    public Location getRemoveLocation() {

        if (_removeLocation != null)
            return _removeLocation;

        if (_arena.getRegion().getWorld() != null)
            return _arena.getRegion().getWorld().getSpawnLocation();

        return null;
    }

    /*
     * Set the location a player is teleported to when
     * they are removed from the arena.
     */
    @Override
    public void setRemoveLocation(@Nullable Location location) {
        _removeLocation = location;

        save("remove-location", location);
    }

    /*
     * Save a setting.
     */
    protected void save(String nodeName, @Nullable Object value) {
        _dataNode.set(nodeName, value);
        _dataNode.saveAsync(null);
    }

}
