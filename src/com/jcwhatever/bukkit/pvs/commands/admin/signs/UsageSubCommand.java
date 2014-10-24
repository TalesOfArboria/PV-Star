package com.jcwhatever.bukkit.pvs.commands.admin.signs;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.signs.SignHandler;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="signs",
        command="usage",
        staticParams={ "typeName" },
        usage="/{plugin-command} {command} usage <typeName>",
        description="Get usage information about a sign type.")

public class UsageSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Usage for '{0}'";
    @Localizable static final String _HANDLER_NOT_FOUND = "A sign handler named '{0}' was not found.";
    @Localizable static final String _FORMAT = "{GRAY}{0} {WHITE}{1}";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        String typeName = args.getName("typeName");

        SignHandler handler = PVStarAPI.getSignManager().getSignHandler(typeName);
        if (handler == null) {
            tellError(sender, Lang.get(_HANDLER_NOT_FOUND, typeName));
            return; // finished
        }

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE, handler.getName()));

        String[] usage = handler.getUsage();

        for (int i=0; i < 4; i++) {

            String line = usage[i];
            if (line == null)
                line = "";

            int centerPadding = (int)Math.round(Math.max(0.0D, (32.0D - line.length()) / 2.0D));

            pagin.add((i + 1) + ".", TextUtils.padLeft(line, centerPadding));
        }

        pagin.show(sender, 1, Lang.get(_FORMAT));
    }
}