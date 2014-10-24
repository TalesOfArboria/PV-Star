package com.jcwhatever.bukkit.pvs.commands.admin.scripts;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

@ICommandInfo(
        parent="scripts",
        command="list",
        staticParams={ "page=1" },
        usage="/{plugin-command} {command} list [page]",
        description="List scripts installed in the selected arena.")

public class ListSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Scripts in Arena '{0}'";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        if (arena == null)
            return; // finished

        int page = args.getInt("page");
        List<String> scriptNames = arena.getScriptManager().getScriptNames();
        Collections.sort(scriptNames);

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE, arena.getName()));

        for (String scriptName : scriptNames) {

            pagin.add(scriptName);
        }

        pagin.show(sender, page, FormatTemplate.ITEM);
    }
}
