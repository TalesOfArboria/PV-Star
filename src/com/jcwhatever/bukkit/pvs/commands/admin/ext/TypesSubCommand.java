package com.jcwhatever.bukkit.pvs.commands.admin.ext;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtensionInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;

import java.util.List;

@ICommandInfo(
        parent="ext",
        command="types",
        staticParams = { "page=1" },
        usage="/{plugin-command} {command} types [page]",
        description="List available arena extensions.")

public class TypesSubCommand extends AbstractPVCommand {

    @Localizable
    static final String _PAGINATOR_TITLE = "Available Arena Extensions";

    @Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidCommandSenderException, InvalidValueException {

        int page = args.getInt("page");

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        List<Class<? extends ArenaExtension>> classes = PVStarAPI.getExtensionManager().getExtensionClasses();
        for (Class<? extends ArenaExtension> extClass : classes) {
            ArenaExtensionInfo info = extClass.getAnnotation(ArenaExtensionInfo.class);
            if (info == null)
                continue;

            pagin.add(info.name(), info.description());
        }

        pagin.show(sender, page, FormatTemplate.ITEM_DESCRIPTION);
    }
}
