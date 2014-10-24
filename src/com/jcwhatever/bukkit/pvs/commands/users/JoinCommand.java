package com.jcwhatever.bukkit.pvs.commands.users;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.options.AddPlayerReason;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;


@ICommandInfo(
        command={"join", "j"},
        staticParams={"arenaName"},
        usage="/{plugin-command} {command} <arenaName>",
        description="Join an arena.",
        permissionDefault= PermissionDefault.TRUE)

public class JoinCommand extends AbstractPVCommand {

    @Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {

        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);

        Player p = (Player)sender;

        String arenaName = args.getName("arenaName");

        Arena arena = getArena(sender, arenaName);
        if (arena == null) {
            return; // finish
        }

        ArenaPlayer player = PVArenaPlayer.get(p);

        // Add player to arena
        arena.join(player, AddPlayerReason.PLAYER_JOIN);

    }
}


