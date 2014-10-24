package com.jcwhatever.bukkit.pvs.commands.admin.spawns;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaRegion;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaTeam;
import com.jcwhatever.bukkit.pvs.api.spawns.SpawnType;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ICommandInfo(
        parent="spawns",
        command="add",
        staticParams={"name", "type", "team=none"},
        usage="/{plugin-command} {command} add <name> <type> [team=none]",
        description="Adds your location as a spawn point in the selected arena.")

public class AddSubCommand extends AbstractPVCommand {

    @Localizable static final String _INVALID_SPAWN_TYPE = "'{0}' is not a valid spawn type.";
    @Localizable static final String _REGION_UNDEFINED = "Cannot add spawns until the arena region is defined.";
    @Localizable static final String _DIFFERENT_WORLD = "The spawn location must be in the same world as the arena.";
    @Localizable static final String _OUTSIDE_REGION = "You must be inside the arena region.";
    @Localizable static final String _ALREADY_EXISTS = "A spawnpoint named '{0}' already exists.";
    @Localizable static final String _SUCCESS = "Spawnpoint '{0}' added to arena '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {

        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER,
                "Console cannot set locations.");

        String name = args.getName("name");
        String type = args.getName("type", 32);
        ArenaTeam team = args.getEnum("team", ArenaTeam.class);

        SpawnType spawnType = PVStarAPI.getSpawnTypeManager().getType(type);

        if (spawnType == null) {
            tellError(sender, Lang.get(_INVALID_SPAWN_TYPE), type);
            return; // finish
        }

        Player p = (Player)sender;

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        ArenaRegion region = arena.getRegion();

        if (!region.isDefined()) {
            tellError(sender, Lang.get(_REGION_UNDEFINED));
            return; // finish
        }

        if (!region.getWorld().getName().equals(p.getLocation().getWorld().getName())) {
            tellError(sender, Lang.get(_DIFFERENT_WORLD));
            return; // finish
        }

        if (!region.contains(p.getLocation())) {
            tellError(sender, Lang.get(_OUTSIDE_REGION));
        } else {

            Location loc = p.getLocation();

            Spawnpoint current = arena.getSpawnManager().getSpawn(name);
            if (current != null) {
                tellError(sender, Lang.get(_ALREADY_EXISTS, name));
                return; // finish
            }

            arena.getSpawnManager().addSpawn(new Spawnpoint(name, spawnType, team, loc));

            tellSuccess(sender, Lang.get(_SUCCESS, name, arena.getName()));
            tell(sender, TextUtils.formatLocation(loc, true));
        }
    }
}

