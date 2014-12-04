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

import com.jcwhatever.bukkit.generic.signs.SignContainer;
import com.jcwhatever.bukkit.generic.signs.SignHandler;
import com.jcwhatever.bukkit.generic.utils.Scheduler;
import com.jcwhatever.bukkit.generic.utils.text.TextUtils;
import com.jcwhatever.bukkit.generic.utils.text.TextColor;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.options.NameMatchMode;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.regex.Matcher;

public class PveSignHandler extends SignHandler {

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    public String getName() {
        return "PVE";
    }

    @Override
    public String getDescription() {
        return "An arena join sign. Player joins an arena by clicking on the sign.";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "PVE",
                "<arenaName>",
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
        String rawName = sign.getRawLine(1);

        Matcher matcher = TextUtils.PATTERN_SPACE.matcher(rawName);

        String arenaName = matcher.replaceAll("_");

        List<Arena> arenas = PVStarAPI.getArenaManager().getArena(arenaName, NameMatchMode.CASE_INSENSITIVE);
        return arenas.size() == 1;
    }

    @Override
    protected boolean onSignClick(final Player p, SignContainer sign) {
        String rawName = sign.getRawLine(1);
        Matcher matcher = TextUtils.PATTERN_SPACE.matcher(rawName);

        final String arenaName = matcher.replaceAll("_");

        List<Arena> arenas = PVStarAPI.getArenaManager().getArena(arenaName, NameMatchMode.CASE_INSENSITIVE);
        if (arenas.size() != 1)
            return false;

        final Arena arena =  arenas.get(0);

        Scheduler.runTaskLater(PVStarAPI.getPlugin(), new Runnable() {

            @Override
            public void run() {

                ArenaPlayer player = PVArenaPlayer.get(p);

                // Add player to arena
                arena.join(player);
            }
        });

        return true;
    }

    @Override
    protected boolean onSignBreak(Player p, SignContainer sign) {
        // allow
        return true;
    }
}
