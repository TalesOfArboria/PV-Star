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
