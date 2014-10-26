/* This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.pvs;

import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.managers.GameManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.LobbyManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.SpawnManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.SpectatorManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.TeamManager;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtensionManager;
import com.jcwhatever.bukkit.pvs.api.arena.settings.ArenaSettings;
import com.jcwhatever.bukkit.pvs.api.scripting.ArenaScriptManager;
import com.jcwhatever.bukkit.pvs.arenas.managers.PVGameManager;
import com.jcwhatever.bukkit.pvs.arenas.managers.PVLobbyManager;
import com.jcwhatever.bukkit.pvs.arenas.managers.PVSpawnManager;
import com.jcwhatever.bukkit.pvs.arenas.managers.PVSpectatorManager;
import com.jcwhatever.bukkit.pvs.arenas.managers.PVTeamManager;
import com.jcwhatever.bukkit.pvs.scripting.PVArenaScriptManager;
import com.jcwhatever.bukkit.pvs.arenas.settings.PVArenaSettings;

/**
 * Provides implementations to an arena.
 *
 * <p>Lazy loaded managers to allow arena to setup before constructing managers.</p>
 */
public class ArenaProvider {

    private final Arena _arena;
    private GameManager _gameManager;
    private LobbyManager _lobbyManager;
    private SpectatorManager _spectatorManager;
    private SpawnManager _spawnManager;
    private TeamManager _teamManager;
    private ArenaExtensionManager _extensionManager;
    private ArenaScriptManager _scriptManager;
    private ArenaSettings _arenaSettings;

    public ArenaProvider(Arena arena) {
        _arena = arena;
    }

    
    public Arena getArena() {
        return _arena;
    }

    
    public GameManager getGameManager() {
        if (_gameManager == null)
            _gameManager = new PVGameManager(_arena);

        return _gameManager;
    }

    
    public LobbyManager getLobbyManager() {
        if (_lobbyManager == null)
            _lobbyManager = new PVLobbyManager(_arena);

        return _lobbyManager;
    }

    
    public SpectatorManager getSpectatorManager() {
        if (_spectatorManager == null)
            _spectatorManager = new PVSpectatorManager(_arena);

        return _spectatorManager;
    }

    
    public SpawnManager getSpawnManager() {
        if (_spawnManager == null)
            _spawnManager = new PVSpawnManager(_arena);

        return _spawnManager;
    }

    
    public TeamManager getTeamManager() {
        if (_teamManager == null)
            _teamManager = new PVTeamManager(_arena);

        return _teamManager;
    }

    
    public ArenaExtensionManager getExtensionManager() {
        if (_extensionManager == null)
            _extensionManager = new PVArenaExtensionManager(_arena);

        return _extensionManager;
    }

    
    public ArenaScriptManager getScriptManager() {
        if (_scriptManager == null)
            _scriptManager = new PVArenaScriptManager(_arena);

        return _scriptManager;
    }

    
    public ArenaSettings getSettings() {
        if (_arenaSettings == null)
            _arenaSettings = new PVArenaSettings(_arena);

        return _arenaSettings;
    }
}
