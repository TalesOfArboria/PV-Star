package com.jcwhatever.bukkit.pvs.commands.admin.kits;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.inventory.Kit;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.player.PlayerHelper;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ICommandInfo(
        parent="kits",
        command="give",
        staticParams={ "kitName", "playerName=$self" },
        usage="/{plugin-command} {command} give <kitName> [playerName]",
        description="Give a player a kit.")

public class GiveSubCommand extends AbstractCommand {

    @Localizable static final String _KIT_NOT_FOUND = "A kit named '{0}' was not found.";
    @Localizable static final String _PLAYER_NOT_FOUND = "A player named '{0}' was not found.";
    @Localizable static final String _SUCCESS = "Kit '{0}' given to player '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {

        String kitName = args.getName("kitName");

        Player kitPlayer;

        if (args.getString("playerName").equals("$self")) {

            InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER, "Cannot give items to console.");

            kitPlayer = (Player)sender;
        }
        else {

            String playerName = args.getName("playerName");

            kitPlayer = PlayerHelper.getPlayer(playerName);

            if (kitPlayer == null) {
                tellError(sender, Lang.get(_PLAYER_NOT_FOUND, playerName));
                return; // finished
            }
        }

        Kit kit = PVStarAPI.getKitManager().getKitByName(kitName);
        if (kit == null) {
            tellError(sender, Lang.get(_KIT_NOT_FOUND, kitName));
            return; // finish
        }

        kit.give(kitPlayer);

        tellSuccess(sender, Lang.get(_SUCCESS, kit.getName(), kitPlayer.getName()));
    }
}
