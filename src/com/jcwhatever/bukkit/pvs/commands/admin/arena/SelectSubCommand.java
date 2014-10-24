package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="arena",
        command="select",
        staticParams={"arenaName"},
        usage="/{plugin-command} {command} select <arenaName>",
        description="Set an arena as your selected arena.")

public class SelectSubCommand extends AbstractPVCommand {

    @Localizable static final String _SUCCESS = "'{0}' is your selected arena.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        String arenaName = args.getString("arenaName");

        Arena arena = getArena(sender, arenaName);
        if (arena == null)
            return; // finish

        PVStarAPI.getArenaManager().setSelectedArena(sender, arena);

        tellSuccess(sender, Lang.get(_SUCCESS, arena.getName()));
    }
}
