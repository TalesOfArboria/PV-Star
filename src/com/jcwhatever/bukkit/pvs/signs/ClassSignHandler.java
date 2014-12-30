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


package com.jcwhatever.bukkit.pvs.signs;

import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.options.ArenaPlayerRelation;
import com.jcwhatever.nucleus.kits.IKit;
import com.jcwhatever.nucleus.signs.SignContainer;
import com.jcwhatever.nucleus.signs.SignHandler;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.text.TextColor;
import com.jcwhatever.nucleus.utils.text.TextUtils;

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
        return "Gives a kit to the player that clicks on it after clearing the players chest.";
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

            IKit kit = PVStarAPI.getKitManager().get(searchName);

            if (kit != null) {
                InventoryUtils.clearAll(p.getInventory());

                kit.give(p);

                //noinspection ConstantConditions
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
