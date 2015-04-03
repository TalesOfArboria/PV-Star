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
import com.jcwhatever.nucleus.managed.messaging.ChatPaginator;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.pvs.api.points.PointsHandler;
import com.jcwhatever.pvs.api.points.PointsType;
import com.jcwhatever.pvs.api.utils.Msg;

import org.bukkit.command.CommandSender;

import java.util.List;

@CommandInfo(
        parent="points",
        command="current",
        staticParams={ "page=1" },
        description="List points types enabled in the selected arena.",

        paramDescriptions = {
                "page= {PAGE}"})

public class CurrentSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE =
            "Points Types in Arena '{0: arena name}'";

    @Localizable static final String _POINTS =
            "{0: number of points} points";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        Arena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        if (arena == null)
            return; // finished

        int page = args.getInteger("page");

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE, arena.getName()));

        List<PointsType> types = PVStarAPI.getPointsManager().getTypes(arena);

        for (PointsType type : types) {

            PointsHandler handler = type.getHandler(arena);
            if (handler == null)
                continue;

            pagin.add(type.getName(), Lang.get(_POINTS, handler.getPoints()));
        }

        pagin.show(sender, page, FormatTemplate.CONSTANT_DEFINITION);
    }
}