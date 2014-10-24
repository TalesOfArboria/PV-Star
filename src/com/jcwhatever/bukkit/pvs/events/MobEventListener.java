package com.jcwhatever.bukkit.pvs.events;

import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobEventListener implements Listener {

    /*
     Handle natural mob spawning
     */
    @EventHandler(priority= EventPriority.HIGHEST)
    private void onNaturalSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM)
            return;

        Arena arena = PVStarAPI.getArenaManager().getArena(event.getLocation());
        if (arena == null)
            return;

        if (!arena.getSettings().isMobSpawnEnabled())
            event.setCancelled(true);
    }

}
