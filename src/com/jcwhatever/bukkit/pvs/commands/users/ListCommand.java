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


package com.jcwhatever.bukkit.pvs.commands.users;

import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.language.Localizable;
import com.jcwhatever.nucleus.messaging.ChatPaginator;
import com.jcwhatever.nucleus.utils.text.TextColor;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.settings.ArenaSettings;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

@CommandInfo(
        command="list",
        staticParams={"page=1"},
        description="Lists all available arenas.",
        permissionDefault= PermissionDefault.TRUE,

        paramDescriptions = {
                "page= {PAGE}"})

public class ListCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Arenas";
    @Localizable static final String _LABEL_DISABLED = "(disabled)";
    @Localizable static final String _LABEL_HIDDEN = "(hidden)";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {


        int page = args.getInteger("page");

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        List<Arena> arenas = PVStarAPI.getArenaManager().getArenas();


        String disabledLabel = Lang.get(_LABEL_DISABLED);
        String hiddenLabel = Lang.get(_LABEL_HIDDEN);

        for (Arena arena : arenas) {

            ArenaSettings settings = arena.getSettings();

            if (settings.isEnabled() && settings.isVisible())
                pagin.add(arena.getName(), settings.getTypeDisplayName());
            else if (!settings.isEnabled() && sender.isOp())
                pagin.add(TextColor.RED + arena.getName() + ' ' + disabledLabel, settings.getTypeDisplayName());
            else if (sender.isOp())
                pagin.add(TextColor.GRAY + arena.getName() + ' ' + hiddenLabel, settings.getTypeDisplayName());
        }

        pagin.show(sender, page, FormatTemplate.LIST_ITEM_DESCRIPTION);
    }
}
