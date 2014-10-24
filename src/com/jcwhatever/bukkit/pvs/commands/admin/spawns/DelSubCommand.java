package com.jcwhatever.bukkit.pvs.commands.admin.spawns;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="spawns",
        command="del",
        staticParams={"name"},
        usage="/{plugin-command} {command} del <name>",
        description="Deletes specified spawn point from the selected arena.")

public class DelSubCommand extends AbstractPVCommand {

    @Localizable static final String _FAILED = "Spawnpoint '{0}' for the arena '{1}' was not found.";
    @Localizable static final String _SUCCESS = "Spawnpoint {0} removed from arena '{1}'.";


    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        String name = args.getString("name");

        Spawnpoint spawnpoint = arena.getSpawnManager().getSpawn(name);
        if (spawnpoint == null) {
            tellError(sender, Lang.get(_FAILED, name, arena.getName()));
            return; // finish
        }

        arena.getSpawnManager().removeSpawn(spawnpoint);
        tellSuccess(sender, Lang.get(_SUCCESS, name, arena.getName()));
    }
}
