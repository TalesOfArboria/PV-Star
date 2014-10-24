package com.jcwhatever.bukkit.pvs.arenas.settings;

import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.settings.SpectatorManagerSettings;

/**
 * Spectator manager settings implementation.
 */
public class PVSpectatorSettings extends AbstractPlayerSettings implements SpectatorManagerSettings {

    /*
     * Constructor.
     */
    public PVSpectatorSettings(Arena arena) {
        super(arena, "spectators");
    }
}
