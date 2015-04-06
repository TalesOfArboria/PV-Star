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


package com.jcwhatever.pvs.commands.admin.game;

import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="game",
        command="points",
        staticParams={"amount=info"},
        description="Set or view player points in the currently selected arena.",

        paramDescriptions = {
                "amount= The number of points the player starts with. " +
                        "Leave blank to see current setting."})

public class PointsSubCommand extends AbstractPVCommand {

    @Localizable static final String _POINTS_INFO =
            "Player start points in arena '{0: arena name}' is set to {1: number of points}.";

    @Localizable static final String _POINTS_SET =
            "Player start points in arena '{0: arena name}' changed to {1: number of points}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        IArena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "amount"));
        if (arena == null)
            return; // finish

        if (args.getString("amount").equals("info")) {

            int points = arena.getGame().getSettings().getStartPoints();
            tell(sender, Lang.get(_POINTS_INFO, arena.getName(), points));
        }
        else {

            int points = args.getInteger("amount");

            arena.getGame().getSettings().setStartPoints(points);

            tellSuccess(sender, Lang.get(_POINTS_SET, arena.getName(), points));
        }
    }
}

