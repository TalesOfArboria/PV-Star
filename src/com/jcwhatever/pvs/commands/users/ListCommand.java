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


package com.jcwhatever.pvs.commands.users;

import com.jcwhatever.nucleus.internal.NucLang;
import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.managed.messaging.ChatPaginator;
import com.jcwhatever.nucleus.utils.text.TextColor;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.nucleus.utils.text.components.IChatClickable.ClickAction;
import com.jcwhatever.nucleus.utils.text.components.IChatHoverable.HoverAction;
import com.jcwhatever.nucleus.utils.text.components.IChatMessage;
import com.jcwhatever.nucleus.utils.text.format.args.ClickableArgModifier;
import com.jcwhatever.nucleus.utils.text.format.args.HoverableArgModifier;
import com.jcwhatever.nucleus.utils.text.format.args.TextArg;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.settings.IArenaSettings;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

@CommandInfo(
        command="list",
        staticParams={"page=1"},
        floatingParams = {"search="},
        description="Lists all available arenas.",
        permissionDefault= PermissionDefault.TRUE,

        paramDescriptions = {
                "page= {PAGE}",
                "search= Optional. Specify a search filter."
        })

public class ListCommand extends AbstractPVCommand implements IExecutableCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Arenas";
    @Localizable static final String _LABEL_DISABLED = "(disabled)";
    @Localizable static final String _LABEL_HIDDEN = "(hidden)";
    @Localizable static final String _CLICK_MESSAGE = "{YELLOW}Click to join arena.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {


        int page = args.getInteger("page");

        ChatPaginator pagin = createPagin(args, 7, Lang.get(_PAGINATOR_TITLE));

        List<IArena> arenas = PVStarAPI.getArenaManager().getArenas();

        String disabledLabel = Lang.get(_LABEL_DISABLED).toString();
        String hiddenLabel = Lang.get(_LABEL_HIDDEN).toString();
        IChatMessage clickMessage = NucLang.get(_CLICK_MESSAGE);

        for (IArena arena : arenas) {

            IArenaSettings settings = arena.getSettings();

            if (!settings.isVisible() && !sender.isOp())
                continue;

            String prefix = "";
            String suffix = "";

            if (!settings.isEnabled()) {
                prefix = TextColor.RED.getFormatCode();
                suffix = ' ' + disabledLabel;
            } else if (!settings.isVisible()) {
                prefix = TextColor.GRAY.getFormatCode();
                suffix = ' ' + hiddenLabel;
            }

            TextArg name = new TextArg(prefix + arena.getName() + suffix,
                    new ClickableArgModifier(ClickAction.RUN_COMMAND, "/pv j " + arena.getName()),
                    new HoverableArgModifier(HoverAction.SHOW_TEXT, clickMessage));

            pagin.add(name, settings.getTypeDisplayName());
        }

        if (!args.isDefaultValue("search"))
            pagin.setSearchTerm(args.getString("search"));

        pagin.show(sender, page, FormatTemplate.LIST_ITEM_DESCRIPTION);
    }
}
