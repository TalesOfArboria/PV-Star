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


package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.CommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidArgumentException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.options.PointsBehavior;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="game",
        command="pointsbehavior",
        staticParams={"static|reset|additive|info=info"},
        description="Set or view points behavior when forwarded to the currently selected arena.",

        paramDescriptions = {
                "static|reset|additive|info= 'static' to keep points unchanged, " +
                        "'reset' to lose points from prev arena, " +
                        "'additive' to add points from prev arena to next arena, " +
                        "'info' or leave blank to see the current setting."})

public class PointsBehaviorSubCommand extends AbstractPVCommand {

    @Localizable static final String _POINTS_INFO = "Player points behavior in arena '{0}' is set to {1}.";
    @Localizable static final String _POINTS_SET = "Player points behavior in arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidArgumentException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "static|reset|additive|info"));
        if (arena == null)
            return; // finish

        if (args.getString("static|reset|additive|info").equals("info")) {

            PointsBehavior behavior = arena.getGameManager().getSettings().getPointsBehavior();
            tell(sender, Lang.get(_POINTS_INFO, arena.getName(), behavior));
        }
        else {

            PointsBehavior behavior = args.getEnum("static|reset|additive|info", PointsBehavior.class);

            arena.getGameManager().getSettings().setPointsBehavior(behavior);

            tellSuccess(sender, Lang.get(_POINTS_SET, arena.getName(), behavior));
        }
    }
}
