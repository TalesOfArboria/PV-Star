/* This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.pvs.signs;

import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.signs.SignContainer;
import com.jcwhatever.bukkit.generic.signs.SignHandler;
import com.jcwhatever.bukkit.generic.utils.TextUtils.TextColor;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.options.ArenaPlayerRelation;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ReadySignHandler extends SignHandler {

    @Localizable static final String _VOTE_NOT_IN_GAME = "You're not in a game.";
    @Localizable static final String _VOTE_GAME_ALREADY_STARTED = "The game has already started.";

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    public String getName() {
        return "Ready";
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
    protected boolean onSignChange(Player p, SignContainer sign) {

        Arena arena = PVStarAPI.getArenaManager().getArena(sign.getLocation());
        return arena != null;
    }

    @Override
    protected boolean onSignClick(Player p, SignContainer sign) {

        ArenaPlayer player = PVArenaPlayer.get(p);
        Arena arena = player.getArena();

        if (arena == null || player.getArenaRelation() == ArenaPlayerRelation.SPECTATOR) {
            Msg.tellError(p, Lang.get(_VOTE_NOT_IN_GAME));
            return false; // finish
        }

        if (player.getArenaRelation() == ArenaPlayerRelation.GAME) {
            Msg.tellError(p, Lang.get(_VOTE_GAME_ALREADY_STARTED));
            return false; // finish
        }

        player.setReady(true);

        return true;
    }

    @Override
    protected boolean onSignBreak(Player p, SignContainer sign) {
        // allow
        return true;
    }
}
