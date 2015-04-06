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

import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.settings.ILobbyManagerSettings;

/**
 *Lobby manager settings implementation.
 */
public class PVLobbySettings extends AbstractPlayerSettings implements ILobbyManagerSettings {

    private boolean _isImmobilized = false;
    private int _minAutoStartPlayers = 4;
    private boolean _hasAutoStart;

    /*
     * Constructor.
     */
    public PVLobbySettings(IArena arena) {
        super(arena, "lobby");

        loadSettings();
    }

    @Override
    public boolean isImmobilized() {
        return _isImmobilized;
    }

    @Override
    public void setImmobilized(boolean isEnabled) {
        _isImmobilized = isEnabled;

        save("immobilized", isEnabled);
    }

    @Override
    public boolean hasAutoStart() {
        return _hasAutoStart;
    }

    @Override
    public void setAutoStart(boolean isEnabled) {
        _hasAutoStart = isEnabled;

        save("auto-start", isEnabled);
    }

    @Override
    public int getMinAutoStartPlayers() {
        return _minAutoStartPlayers;
    }

    @Override
    public void setMinAutoStartPlayers(int minPlayers) {
        _minAutoStartPlayers = minPlayers;

        save("min-auto-start-players", minPlayers);
    }

    /*
     * initial load of settings from manager data node
     */
    private void loadSettings() {
        _isImmobilized = getDataNode().getBoolean("immobilized", _isImmobilized);
        _minAutoStartPlayers = getDataNode().getInteger("min-auto-start-players", _minAutoStartPlayers);
        _hasAutoStart = getDataNode().getBoolean("auto-start", _hasAutoStart);
    }
}
