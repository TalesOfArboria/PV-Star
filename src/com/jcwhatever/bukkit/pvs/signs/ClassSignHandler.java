package com.jcwhatever.bukkit.pvs.signs;

import com.jcwhatever.bukkit.generic.inventory.InventoryHelper;
import com.jcwhatever.bukkit.generic.inventory.Kit;
import com.jcwhatever.bukkit.generic.signs.SignContainer;
import com.jcwhatever.bukkit.generic.signs.SignHandler;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.generic.utils.TextUtils.TextColor;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.options.ArenaPlayerRelation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.regex.Matcher;

public class ClassSignHandler extends SignHandler {

    private static final String PLAYER_CLASS_META = "com.jcwhatever.bukkit.pvs.signs.ClassSignHandler.PLAYER_CLASS_META";

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    public String getName() {
        return "Class";
    }

    @Override
    public String getDescription() {
        return "Gives a kit to the player that clicks on it after clearing the players inventory.";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "Class",
                "<kitName>",
                "--anything--",
                "--anything--"
        };
    }

    @Override
    public String getHeaderPrefix() {
        return TextColor.DARK_BLUE.toString();
    }

    @Override
    protected void onSignLoad(SignContainer sign) {
        // do nothing
    }

    @Override
    protected boolean onSignChange(Player p, SignContainer sign) {
        Arena arena = PVStarAPI.getArenaManager().getArena(sign.getLocation());
        return arena != null;
    }

    @Override
    protected boolean onSignClick(Player p, SignContainer sign) {

        ArenaPlayer player = PVArenaPlayer.get(p);
        Arena arena = player.getArena();
        if (arena == null)
            return false;

        if (player.getArenaRelation() != ArenaPlayerRelation.LOBBY)
            return false;

        String className = sign.getRawLine(1);
        String currentClassName = player.getSessionMeta().get(PLAYER_CLASS_META);

        Matcher matcher = TextUtils.PATTERN_SPACE.matcher(className.toLowerCase());
        String searchName = matcher.replaceAll("_");

        if (currentClassName == null || !searchName.equals(currentClassName.toLowerCase())) {

            Kit kit = PVStarAPI.getKitManager().getKitByName(searchName);

            if (kit != null) {
                InventoryHelper.clearAll(p.getInventory());

                kit.give(p);

                player.getRelatedManager().tell(ChatColor.GREEN + p.getName() + " is a " + className + '.');
            }
        }

        return false;
    }

    @Override
    protected boolean onSignBreak(Player p, SignContainer sign) {
        // allow
        return true;
    }
}
