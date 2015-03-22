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

import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.PVArenaPlayer;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.pvs.api.arena.options.ArenaPlayerRelation;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.nucleus.utils.signs.SignContainer;
import com.jcwhatever.nucleus.utils.signs.SignHandler;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.nucleus.utils.text.TextColor;

import org.bukkit.entity.Player;

public class ReadySignHandler extends SignHandler {

    @Localizable static final String _VOTE_NOT_IN_GAME = "You're not in a game.";
    @Localizable static final String _VOTE_GAME_ALREADY_STARTED = "The game has already started.";

    /**
     * Constructor.
     */
    public ReadySignHandler() {
        super(PVStarAPI.getPlugin(), "Ready");
    }

    @Override
    public String getDescription() {
        return "A sign that votes to start when a player clicks on it.";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "Ready",
                "--anything--",
                "--anything--",
                "--anything--"
        };
    }

    @Override
    public String getHeaderPrefix() {
        return TextColor.BOLD.toString() + TextColor.DARK_GREEN;
    }

    @Override
    protected void onSignLoad(SignContainer sign) {
        // do nothing
    }

    @Override
    protected SignChangeResult onSignChange(Player p, SignContainer sign) {

        Arena arena = PVStarAPI.getArenaManager().getArena(sign.getLocation());
        return arena != null
                ? SignChangeResult.VALID
                : SignChangeResult.INVALID;
    }

    @Override
    protected SignClickResult onSignClick(Player p, SignContainer sign) {

        ArenaPlayer player = PVArenaPlayer.get(p);
        Arena arena = player.getArena();

        if (arena == null || player.getArenaRelation() == ArenaPlayerRelation.SPECTATOR) {
            Msg.tellError(p, Lang.get(_VOTE_NOT_IN_GAME));
            return SignClickResult.IGNORED;
        }

        if (player.getArenaRelation() == ArenaPlayerRelation.GAME) {
            Msg.tellError(p, Lang.get(_VOTE_GAME_ALREADY_STARTED));
            return SignClickResult.IGNORED;
        }

        player.setReady(true);

        return SignClickResult.HANDLED;
    }

    @Override
    protected SignBreakResult onSignBreak(Player p, SignContainer sign) {
        return SignBreakResult.ALLOW;
    }
}
