package com.jcwhatever.bukkit.pvs.commands.admin.kits;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.inventory.Kit;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;

import java.util.List;

@ICommandInfo(
        parent="kits",
        command="list",
        staticParams={ "page=1" },
        usage="/{plugin-command} {command} list [page]",
        description="List player kits.")

public class ListSubCommand extends AbstractCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Kits";
    @Localizable static final String _FORMAT = "{GOLD}{0} {GRAY}({1} Items, {2} Armor)";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        int	page = args.getInt("page");

        List<Kit> kits = PVStarAPI.getKitManager().getKits();

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        for (Kit kit : kits) {
            pagin.add(kit.getName(), kit.getItems().length, kit.getArmor().length);
        }

        pagin.show(sender, page, Lang.get(_FORMAT));
    }
}
