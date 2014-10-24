package com.jcwhatever.bukkit.pvs.arenas.settings;

import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.settings.LobbyManagerSettings;

/**
 *Lobby manager settings implementation.
 */
public class PVLobbySettings extends AbstractPlayerSettings implements LobbyManagerSettings {

    private boolean _isImmobilized = false;
    private int _startCountdown = 10;
    private int _minAutoStartPlayers = 4;
    private boolean _hasAutoStart;

    /*
     * Constructor.
     */
    public PVLobbySettings(Arena arena) {
        super(arena, "lobby");

        loadSettings();
    }

    /*
     * Determine if players in the lobby are prevented
     * from moving from their spawn in location.
     */
    @Override
    public boolean isImmobilized() {
        return _isImmobilized;
    }

    /*
     * Set players immobilized in lobby.
     */
    @Override
    public void setImmobilized(boolean isEnabled) {
        _isImmobilized = isEnabled;

        save("immobilized", isEnabled);
    }

    /*
     * Determine if auto start is enabled.
     */
    @Override
    public boolean hasAutoStart() {
        return _hasAutoStart;
    }

    /*
     * Set auto start enabled.
     */
    @Override
    public void setAutoStart(boolean isEnabled) {
        _hasAutoStart = isEnabled;

        save("auto-start", isEnabled);
    }

    /*
     * Get the number of seconds to countdown from
     * before the game starts.
     */
    @Override
    public int getStartCountdownSeconds() {
        return _startCountdown;
    }

    /*
     * Set the number of seconds to countdown from
     * before the game starts.
     */
    @Override
    public void setStartCountdownSeconds(int seconds) {
        _startCountdown = seconds;

        save("start-countdown", seconds);
    }

    /*
     * Get the minimum number of players required
     * to auto start.
     */
    @Override
    public int getMinAutoStartPlayers() {
        return _minAutoStartPlayers;
    }

    /*
     * Set the minimum number of players required
     * to auto start.
     */
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
        _startCountdown = getDataNode().getInteger("start-countdown", _startCountdown);
        _minAutoStartPlayers = getDataNode().getInteger("min-auto-start-players", _minAutoStartPlayers);
        _hasAutoStart = getDataNode().getBoolean("auto-start", _hasAutoStart);
    }
}
