package com.jcwhatever.bukkit.pvs.commands.admin.modules;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.modules.PVStarModule;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@ICommandInfo(
        parent="modules",
        command="list",
        staticParams={"page=1"},
        usage="/{plugin-command} {command} list [page]",
        description="Shows list of loaded modules.")

public class ListSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "PV-Star Modules";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        int page = args.getInt("page");

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        List<PVStarModule> modules = PVStarAPI.getPlugin().getModules();

        for (PVStarModule module : modules) {
            pagin.add(module.getName() + ' ' + module.getVersion(), module.getDescription());
        }

        pagin.show(sender, page, FormatTemplate.ITEM_DESCRIPTION);
    }

}

