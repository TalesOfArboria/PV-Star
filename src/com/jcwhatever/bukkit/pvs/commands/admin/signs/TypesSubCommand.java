package com.jcwhatever.bukkit.pvs.commands.admin.signs;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.signs.SignHandler;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;

import java.util.List;

@ICommandInfo(
        parent="signs",
        command="types",
        staticParams={"page=1"},
        usage="/{plugin-command} {command} types [page]",
        description="Show a list of sign types.")

public class TypesSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Sign Types";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        List<SignHandler> signHandlers = PVStarAPI.getSignManager().getSignHandlers();
        for (SignHandler handler : signHandlers) {
            pagin.add(handler.getName(), handler.getDescription());
        }

        pagin.show(sender, args.getInt("page"), FormatTemplate.ITEM_DESCRIPTION);
    }
}
