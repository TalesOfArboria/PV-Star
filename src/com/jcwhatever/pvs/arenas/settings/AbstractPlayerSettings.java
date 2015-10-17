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


package com.jcwhatever.pvs.arenas.settings;

import com.jcwhatever.nucleus.managed.teleport.TeleportMode;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.settings.IContextSettings;

/**
 * Basic settings for player manager settings implementations
 */
public abstract class AbstractPlayerSettings implements IContextSettings {

    private final IDataNode _dataNode;
    private final IArena _arena;

    private boolean _isPvpEnabled = true;
    private boolean _isTeamPvpEnabled = false;
    private boolean _isSharingEnabled = true;
    private boolean _isHungerEnabled = true;
    private boolean _isAutoHealEnabled = true;
    private boolean _isArmorDamageable = true;
    private boolean _isToolsDamageable = true;
    private boolean _isWeaponsDamageable = true;
    private boolean _hasFallDamage = true;
    private boolean _isSpawnsReserved = false;
    private int _maxDeathTicks = 20 * 20;
    private TeleportMode _teleportMode = TeleportMode.TARGET_ONLY;

    /*
     * Constructor.
     * nodeName is the name of the settings node.
     */
    public AbstractPlayerSettings(IArena arena, String nodeName) {
        PreCon.notNull(arena);
        PreCon.notNullOrEmpty(nodeName);

        _arena = arena;
        _dataNode = arena.getDataNode("settings." + nodeName);

        _isPvpEnabled = _dataNode.getBoolean("pvp", _isPvpEnabled);
        _isTeamPvpEnabled = _dataNode.getBoolean("team-pvp", _isTeamPvpEnabled);
        _isSharingEnabled = _dataNode.getBoolean("sharing", _isSharingEnabled);
        _isHungerEnabled = _dataNode.getBoolean("hunger", _isHungerEnabled);
        _isAutoHealEnabled = _dataNode.getBoolean("auto-heal", _isAutoHealEnabled);
        _isArmorDamageable = _dataNode.getBoolean("armor-damage", _isArmorDamageable);
        _isWeaponsDamageable = _dataNode.getBoolean("weapons-damage", _isWeaponsDamageable);
        _isToolsDamageable = _dataNode.getBoolean("tools-damage", _isToolsDamageable);
        _hasFallDamage = _dataNode.getBoolean("fall-damage", _hasFallDamage);
        _isSpawnsReserved = _dataNode.getBoolean("spawns-reserved", _isSpawnsReserved);
        _maxDeathTicks = _dataNode.getInteger("max-death-ticks", _maxDeathTicks);
        _teleportMode = _dataNode.getEnum("teleport-mode", _teleportMode, TeleportMode.class);
    }

    @Override
    public final IArena getArena() {
        return _arena;
    }

    @Override
    public final IDataNode getDataNode() {
        return _dataNode;
    }

    @Override
    public boolean isPvpEnabled() {
        return _isPvpEnabled;
    }

    @Override
    public void setPvpEnabled(boolean isEnabled) {
        save("pvp", _isPvpEnabled = isEnabled);
    }

    @Override
    public boolean isTeamPvpEnabled() {
        return _isTeamPvpEnabled;
    }

    @Override
    public void setTeamPvpEnabled(boolean isEnabled) {
        save("team-pvp", _isTeamPvpEnabled = isEnabled);
    }

    @Override
    public boolean isSharingEnabled() {
        return _isSharingEnabled;
    }

    @Override
    public void setSharingEnabled(boolean isEnabled) {
        save("sharing", _isSharingEnabled = isEnabled);
    }

    @Override
    public boolean isHungerEnabled() {
        return _isHungerEnabled;
    }

    @Override
    public void setHungerEnabled(boolean isEnabled) {
        save("hunger", _isHungerEnabled = isEnabled);
    }

    @Override
    public boolean isAutoHealEnabled() {
        return _isAutoHealEnabled;
    }

    @Override
    public void setAutoHealEnabled(boolean isEnabled) {
        save("auto-heal", _isAutoHealEnabled = isEnabled);
    }

    @Override
    public boolean isArmorDamageable() {
        return _isArmorDamageable;
    }

    @Override
    public void setArmorDamageable(boolean isDamageable) {
        save("armor-damage", _isArmorDamageable = isDamageable);
    }

    @Override
    public boolean isWeaponsDamageable() {
        return _isWeaponsDamageable;
    }

    @Override
    public void setWeaponsDamageable(boolean isDamageable) {
        save("weapons-damage", _isWeaponsDamageable = isDamageable);
    }

    @Override
    public boolean isToolsDamageable() {
        return _isToolsDamageable;
    }

    @Override
    public void setToolsDamageable(boolean isDamageable) {
        save("tools-damage", _isToolsDamageable = isDamageable);
    }

    @Override
    public boolean hasFallDamage() {
        return _hasFallDamage;
    }

    @Override
    public void setFallDamage(boolean hasFallDamage) {
        save("fall-damage", _hasFallDamage = hasFallDamage);
    }

    @Override
    public boolean isPlayerSpawnsReserved() {
        return _isSpawnsReserved;
    }

    @Override
    public void setPlayerSpawnsReserved(boolean isEnabled) {
        save("spawns-reserved", _isSpawnsReserved = isEnabled);
    }

    @Override
    public TeleportMode getTeleportMode() {
        return _teleportMode;
    }

    @Override
    public void setTeleportMode(TeleportMode mode) {
        PreCon.notNull(mode);

        save("teleport-mode", _teleportMode = mode);
    }

    @Override
    public int getMaxDeathTicks() {
        return _maxDeathTicks;
    }

    @Override
    public void setMaxDeathTicks(int ticks) {
        save("max-death-ticks", _maxDeathTicks = ticks);
    }

    /**
     * Save setting.
     */
    protected void save(String nodeName, Object value) {
        _dataNode.set(nodeName, value);
        _dataNode.save();
    }
}
