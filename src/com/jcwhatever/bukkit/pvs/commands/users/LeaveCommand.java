package com.jcwhatever.bukkit.pvs.commands.users;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.options.RemovePlayerReason;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

@ICommandInfo(
        command={"leave", "l"},
        usage="/{plugin-command} {command}",
        description="Leave the arena.",
        permissionDefault= PermissionDefault.TRUE)

public class LeaveCommand extends AbstractPVCommand {

    @Localizable static final String _NOT_IN_ARENA = "You're not in an arena.";
    @Localizable static final String _SUCCESS = "Thanks for playing!";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidCommandSenderException {

        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);

        Player p = (Player)sender;
        ArenaPlayer player = PVArenaPlayer.get(p);

        Arena arena = player.getArena();

        if (arena == null) {

            tellError(p, Lang.get(_NOT_IN_ARENA));
            return; // finish
        }

        if (arena.remove(player, RemovePlayerReason.PLAYER_LEAVE))
            tellSuccess(p, Lang.get(_SUCCESS));
    }
}
