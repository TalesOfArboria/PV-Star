package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="arena",
        command={"create"},
        staticParams={"arenaName", "type=Arena"},
        usage="/{plugin-command} {command} create <arenaName> [type]",
        description="Create a new arena. Type is for display purposes and is optional.")

public class CreateSubCommand extends AbstractPVCommand {

    @Localizable static final String _ARENA_ALREADY_EXISTS = "An arena with the name '{0}' already exists.";
    @Localizable static final String _FAILED = "Failed to create arena.";
    @Localizable static final String _SUCCESS = "A new arena with the name '{0}' of type '{1}' has been created.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException, InvalidCommandSenderException {

        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER, "Console cannot select regions.");

        String name = args.getName("arenaName");
        String type = args.getString("type");

        Arena arena = getArena(name);

        if (arena != null) {
            tellError(sender, Lang.get(_ARENA_ALREADY_EXISTS, name));
            return; // finish
        }

        arena = PVStarAPI.getArenaManager().addArena(name, type);
        if (arena == null) {
            tellError(sender, Lang.get(_FAILED));
        }
        else {
            PVStarAPI.getArenaManager().setSelectedArena(sender, arena);
            tellSuccess(sender, Lang.get(_SUCCESS, name, type));
        }

    }


}



