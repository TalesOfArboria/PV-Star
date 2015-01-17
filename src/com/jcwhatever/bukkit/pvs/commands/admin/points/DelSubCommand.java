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


package com.jcwhatever.bukkit.pvs.commands.admin.points;

import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.points.PointsType;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="points",
        command="del",
        staticParams={ "typeName" },
        description="Remove points type from the currently selected arena.",

        paramDescriptions = {
                "typeName= The name of the points type to remove."})

public class DelSubCommand extends AbstractPVCommand {

    @Localizable static final String _TYPE_NOT_FOUND = "A points type named '{0}' was not found.";
    @Localizable static final String _SUCCESS = "Points type '{0}' removed from arena '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return;

        String typeName = args.getName("typeName", 32);

        PointsType type = PVStarAPI.getPointsManager().getType(typeName);
        if (type == null) {
            tellError(sender, Lang.get(_TYPE_NOT_FOUND, typeName));
            return; // finished
        }

        type.remove(arena);

        tellSuccess(sender, Lang.get(_SUCCESS, type.getName(), arena.getName()));
    }
}
