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


package com.jcwhatever.pvs.signs;

import com.jcwhatever.nucleus.providers.kits.IKit;
import com.jcwhatever.nucleus.providers.kits.Kits;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.managed.signs.ISignContainer;
import com.jcwhatever.nucleus.managed.signs.SignHandler;
import com.jcwhatever.nucleus.utils.text.TextColor;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.pvs.players.ArenaPlayer;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.options.ArenaContext;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;

/**
 * Player "class" sign for distributing {@link IKit}.
 */
public class ClassSignHandler extends SignHandler {

    private static final MetaKey<String> PLAYER_CLASS_META = new MetaKey<>(String.class);

    /**
     * Constructor.
     */
    public ClassSignHandler() {
        super(PVStarAPI.getPlugin(), "Class");
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
    protected void onSignLoad(ISignContainer sign) {
        // do nothing
    }

    @Override
    protected SignChangeResult onSignChange(Player player, ISignContainer sign) {
        IArena arena = PVStarAPI.getArenaManager().getArena(sign.getLocation());
        return arena != null
                ? SignChangeResult.VALID
                : SignChangeResult.INVALID;
    }

    @Override
    protected SignClickResult onSignClick(Player p, ISignContainer sign) {

        IArenaPlayer player = ArenaPlayer.get(p);
        if (player == null)
            return SignClickResult.IGNORED;

        IArena arena = player.getArena();
        if (arena == null)
            return SignClickResult.IGNORED;

        if (player.getContext() != ArenaContext.LOBBY)
            return SignClickResult.IGNORED;

        String className = sign.getRawLine(1);
        String currentClassName = player.getSessionMeta().get(PLAYER_CLASS_META);

        Matcher matcher = TextUtils.PATTERN_SPACE.matcher(className.toLowerCase());
        String searchName = matcher.replaceAll("_");

        if (currentClassName == null || !searchName.equals(currentClassName.toLowerCase())) {

            IKit kit = Kits.get(searchName);

            if (kit != null) {
                InventoryUtils.clearAll(p.getInventory());

                kit.give(p);

                //noinspection ConstantConditions
                player.getContextManager().tell(ChatColor.GREEN + p.getName() + " is a " + className + '.');
            }
        }

        return SignClickResult.HANDLED;
    }

    @Override
    protected SignBreakResult onSignBreak(Player player, ISignContainer sign) {
        return SignBreakResult.ALLOW;
    }
}
