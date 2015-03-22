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


package com.jcwhatever.pvs.commands.admin.points;

import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.pvs.api.points.PointsHandler;
import com.jcwhatever.pvs.api.points.PointsType;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="points",
        command="set",
        staticParams={ "typeName", "amount=info" },
        description="Set points received or deducted for a points type in the selected arena.",

        paramDescriptions = {
                "typeName= The name of the points type.",
                "amount= The amount received or deducted. Use a negative number to deduct. " +
                        "Leave blank to see current setting."})

public class SetSubCommand extends AbstractPVCommand {

    @Localizable static final String _TYPE_NOT_FOUND =
            "A points type named '{0: points type name}' was not found.";

    @Localizable static final String _TYPE_NOT_ADDED =
            "Points type '{0: points type name}' is not added to arena '{1: arena name}'";

    @Localizable static final String _INFO =
            "Points type in arena '{0: arena name}' is {1: points type name}.";

    @Localizable static final String _CHANGED =
            "Points type in arena '{0: arena name}' changed to {1: points type name}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "amount"));
        if (arena == null)
            return;

        String typeName = args.getName("typeName", 32);

        PointsType type = PVStarAPI.getPointsManager().getType(typeName);
        if (type == null) {
            tellError(sender, Lang.get(_TYPE_NOT_FOUND, typeName));
            return; // finished
        }

        PointsHandler handler = type.getHandler(arena);
        if (handler == null) {
            tellError(sender, Lang.get(_TYPE_NOT_ADDED, type.getName(), arena.getName()));
            return; // finished
        }

        if (args.getString("amount").equals("info")) {

            int points = handler.getPoints();

            tell(sender, Lang.get(_INFO, arena.getName(), points));
        }
        else {

            int points = args.getInteger("amount");

            handler.setPoints(points);

            tellSuccess(sender, Lang.get(_CHANGED, arena.getName(), points));

        }
    }
}

