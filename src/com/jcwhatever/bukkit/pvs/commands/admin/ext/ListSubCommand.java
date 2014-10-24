package com.jcwhatever.bukkit.pvs.commands.admin.ext;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;

import java.util.Set;

@ICommandInfo(
        parent="ext",
        command="list",
        staticParams = { "page=1" },
        usage="/{plugin-command} {command} list [page]",
        description="List extensions installed in the currently selected arena.")

public class ListSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Extensions in arena '{0}'";

    @Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidCommandSenderException, InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        if (arena == null)
            return; // finish

        int page = args.getInt("page");


        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE, arena.getName()));

        Set<ArenaExtension> extensions = arena.getExtensionManager().getAll();

        for (ArenaExtension extension : extensions) {
            pagin.add(extension.getName(), extension.getDescription());
        }

        pagin.show(sender, page, FormatTemplate.ITEM_DESCRIPTION);
    }
}

