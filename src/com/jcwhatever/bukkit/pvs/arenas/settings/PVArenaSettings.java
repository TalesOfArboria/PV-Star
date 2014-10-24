package com.jcwhatever.bukkit.pvs.arenas.settings;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.options.OutsidersAction;
import com.jcwhatever.bukkit.pvs.api.arena.settings.ArenaSettings;
import com.jcwhatever.bukkit.pvs.api.events.ArenaDisabledEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaEnabledEvent;
import com.jcwhatever.bukkit.pvs.api.events.ArenaPreEnableEvent;
import org.bukkit.Location;

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
    private boolean _isArenaDamageEnabled = false;
    private boolean _isAutoRestoreEnabled = false;
    private Location _removeLocation;
    private OutsidersAction _outsidersAction = OutsidersAction.NONE;
    private String _typeDisplayName;

    /*
     * Constructor.
     */
    public PVArenaSettings(Arena arena) {
        PreCon.notNull(arena);

        _arena = arena;
        _dataNode = arena.getDataNode("settings.arena");

        _minPlayers = _dataNode.getInteger("min-players", _minPlayers);
        _maxPlayers = _dataNode.getInteger("max-players", _maxPlayers);
        _isVisible = _dataNode.getBoolean("visible", _isVisible);
        _isArenaDamageEnabled = _dataNode.getBoolean("arena-damage", _isArenaDamageEnabled);
        _isMobSpawnEnabled = _dataNode.getBoolean("mob-spawn", _isMobSpawnEnabled);
        _isAutoRestoreEnabled = _dataNode.getBoolean("auto-restore", _isAutoRestoreEnabled);
        _removeLocation = _dataNode.getLocation("remove-location", _removeLocation);
        _outsidersAction = _dataNode.getEnum("outsiders-action", _outsidersAction, OutsidersAction.class);
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

            _arena.getEventManager().call(new ArenaEnabledEvent(_arena));
        }
        else {
            _isEnabled = false;

            _arena.getGameManager().end();

            _arena.getEventManager().call(new ArenaDisabledEvent(_arena));
        }
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
     * Determine if player can break blocks in the
     * arena.
     */
    @Override
    public boolean isArenaDamageEnabled() {
        return _isArenaDamageEnabled;
    }

    /*
     * Set if player can break blocks in the arena.
     */
    @Override
    public void setArenaDamageEnabled(boolean isEnabled) {
        _isArenaDamageEnabled = isEnabled;

        save("arena-damage", isEnabled);
    }

    /*
     * Determine if arena auto restores when the arena
     * ends.
     */
    @Override
    public boolean isAutoRestoreEnabled() {
        return _isAutoRestoreEnabled;
    }

    /*
     * Set arena auto restore.
     */
    @Override
    public void setAutoRestoreEnabled(boolean isEnabled) {
        _isAutoRestoreEnabled = isEnabled;

        save("auto-restore", isEnabled);
    }

    /*
     * Get the location a player is teleported to when
     * they are removed from the arena.
     */
    @Override
    public Location getRemoveLocation() {
        return _removeLocation != null ? _removeLocation : _arena.getRegion().getWorld().getSpawnLocation();
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
     * Get the action to take when an outsider enters the
     * arena region.
     */
    @Override
    public OutsidersAction getOutsidersAction() {
        return _outsidersAction;
    }

    /*
     * Set the action to take when an outsider enters the
     * arena region.
     */
    @Override
    public void setOutsidersAction(OutsidersAction action) {
        PreCon.notNull(action);

        save("outsiders-action", action);
    }

    /*
     * Save a setting.
     */
    protected void save(String nodeName, Object value) {
        _dataNode.set(nodeName, value);
        _dataNode.saveAsync(null);
    }

}
