package com.jcwhatever.bukkit.pvs.commands.admin.scripts;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

@ICommandInfo(
        parent="scripts",
        command="listall",
        staticParams={ "page=1" },
        usage="/{plugin-command} {command} listall [page]",
        description="List all available scripts.")

public class ListAllSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "All Scripts";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        int page = args.getInt("page");
        List<String> scriptNames = PVStarAPI.getScriptManager().getScriptNames();
        Collections.sort(scriptNames);

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        for (String scriptName : scriptNames) {

            pagin.add(scriptName);
        }

        pagin.show(sender, page, FormatTemplate.ITEM);
    }
}
