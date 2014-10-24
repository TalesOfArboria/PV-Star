package com.jcwhatever.bukkit.pvs.commands.admin.kits;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.inventory.Kit;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="kits",
        command="add",
        staticParams={ "kitName" },
        usage="/{plugin-command} {command} add <kitName>",
        description="Add a new player kit.")

public class AddSubCommand extends AbstractCommand {

    @Localizable static final String _KIT_ALREADY_EXISTS = "A kit named '{0}' already exists.";
    @Localizable static final String _FAILED = "Failed to create kit.";
    @Localizable static final String _SUCCESS = "Kit '{0}' created.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        String kitName = args.getName("kitName");

        Kit kit = PVStarAPI.getKitManager().getKitByName(kitName);
        if (kit != null) {
            tellError(sender, Lang.get(_KIT_ALREADY_EXISTS, kitName));
            return; // finish
        }

        kit = PVStarAPI.getKitManager().createKit(kitName);
        if (kit == null) {
            tellError(sender, Lang.get(_FAILED));
            return; // finish
        }

        tellSuccess(sender, Lang.get(_SUCCESS, kit.getName()));
    }
}
