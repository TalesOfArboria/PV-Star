package com.jcwhatever.bukkit.pvs.arenas.managers;

import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.managers.SpectatorManager;
import com.jcwhatever.bukkit.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.bukkit.pvs.api.arena.settings.SpectatorManagerSettings;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.arenas.settings.PVSpectatorSettings;
import org.bukkit.Location;

import javax.annotation.Nullable;

/**
 * Spectator manager implementation
 */
public class PVSpectatorManager extends AbstractPlayerManager implements SpectatorManager {

    private SpectatorManagerSettings _settings;

    /*
     * Constructor.
     */
    public PVSpectatorManager(Arena arena) {
        super(arena);

        _settings = new PVSpectatorSettings(arena);
    }

    /*
     * Get the spectator manager settings.
     */
    @Override
    public SpectatorManagerSettings getSettings() {
        return _settings;
    }

    /*
     * Called when a respawn location is needed for a player.
     */
    @Nullable
    @Override
    protected Location onRespawnPlayer(ArenaPlayer player) {
        return getSpawnLocation(player);
    }

    /**
     * Called when a spawn location is needed for a player.
     */
    @Override
    protected Location onAddPlayer(ArenaPlayer player, AddPlayerReason reason) {
        return getSpawnLocation(player);
    }

    /*
     * Called before a player is removed from spectators.
     */
    @Override
    protected void onPreRemovePlayer(ArenaPlayer player, RemovePlayerReason reason) {
        // do nothing
    }

    /*
     * Called after a player is removed from spectators.
     */
    @Override
    protected Location onRemovePlayer(ArenaPlayer player, RemovePlayerReason reason) {
        return getArena().getSettings().getRemoveLocation();
    }

    /*
     * Get a spectator spawn
     */
    private Location getSpawnLocation(ArenaPlayer player) {
        Spawnpoint spawnpoint = getArena().getSpawnManager().getRandomSpectatorSpawn(player.getTeam());
        if (spawnpoint == null) {
            Msg.warning("Failed to find a spectator spawn for a player in arena '{0}'.", getArena().getName());
            return null;
        }

        return spawnpoint;
    }
}
