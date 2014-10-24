package com.jcwhatever.bukkit.pvs.events;

import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.settings.PlayerManagerSettings;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class SharingListener implements Listener {

    /*
     Handle inventory sharing
    */
    @EventHandler(priority= EventPriority.HIGHEST)
    private void onInventorySharing(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity == null)
            return;

        if (!(humanEntity instanceof Player))
            return;

        final Player p = (Player)humanEntity;
        ArenaPlayer player = PVArenaPlayer.get(p);
        Arena arena = player.getArena();

        if (arena == null)
            return;

        PlayerManagerSettings settings = player.getRelatedSettings();
        if (settings == null)
            return;

        if (settings.isSharingEnabled())
            return;

        // prevent sharing
        if ((event.getAction() == InventoryAction.PLACE_ALL ||
                event.getAction() == InventoryAction.PLACE_ONE ||
                event.getAction() == InventoryAction.PLACE_SOME)) {

            if (event.getInventory().getHolder() instanceof Chest ||
                    event.getInventory().getHolder() instanceof DoubleChest) {

                if (event.getRawSlot() >= event.getInventory().getContents().length)
                    return;

                event.setCancelled(true);
                event.setResult(Result.DENY);

                // close inventory to prevent sharing bug
                // i.e. If you try enough, eventually the event wont fire but the item
                // will make it into inventory.
                Bukkit.getScheduler().runTask(PVStarAPI.getPlugin(), new Runnable() {

                    @Override
                    public void run() {
                        p.closeInventory();
                    }

                });
            }
        }
    }

    /*
     Handle drop item sharing
    */
    @EventHandler(priority=EventPriority.HIGHEST)
    private void onPlayerDropItem(PlayerDropItemEvent event) {

        ArenaPlayer player =PVArenaPlayer.get(event.getPlayer());

        Arena arena = player.getArena();
        if (arena == null)
            return;

        PlayerManagerSettings settings = player.getRelatedSettings();
        if (settings == null)
            return;

        if (settings.isSharingEnabled())
            return;

        event.setCancelled(true);
    }

}
