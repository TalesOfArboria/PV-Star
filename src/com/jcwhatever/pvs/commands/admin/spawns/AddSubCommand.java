/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.jcwhatever.pvs.commands.admin.spawns;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.ArenaRegion;
import com.jcwhatever.pvs.api.arena.ArenaTeam;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.pvs.api.spawns.SpawnType;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        parent="spawns",
        command="add",
        staticParams={"name", "type", "team=none"},
        description="Adds your location as a spawn point in the selected arena.",

        paramDescriptions = {
                "name= The name of the spawn. {NAME16}",
                "type= The name of the spawn type.",
                "team= Optional. The name of the team the spawn is for. Default is 'none'."})

public class AddSubCommand extends AbstractPVCommand implements IExecutableCommand {

    @Localizable static final String _INVALID_SPAWN_TYPE =
            "'{0: spawn type name}' is not a valid spawn type.";

    @Localizable static final String _REGION_UNDEFINED =
            "Cannot add spawns until the arena region is defined.";

    @Localizable static final String _DIFFERENT_WORLD =
            "The spawn location must be in the same world as the arena.";

    @Localizable static final String _OUTSIDE_REGION =
            "You must be inside the arena region.";

    @Localizable static final String _ALREADY_EXISTS =
            "A spawnpoint named '{0: spawn name}' already exists.";

    @Localizable static final String _SUCCESS =
            "Spawnpoint '{0: spawn name}' added to arena '{1: arena name}'.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        CommandException.checkNotConsole(getPlugin(), this, sender);

        String name = args.getName("name");
        String type = args.getName("type", 32);
        ArenaTeam team = args.getEnum("team", ArenaTeam.class);

        SpawnType spawnType = PVStarAPI.getSpawnTypeManager().getType(type);

        if (spawnType == null)
            throw new CommandException(Lang.get(_INVALID_SPAWN_TYPE), type);

        Player p = (Player)sender;

        IArena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNING);
        if (arena == null)
            return; // finish

        ArenaRegion region = arena.getRegion();

        if (!region.isDefined())
            throw new CommandException(Lang.get(_REGION_UNDEFINED));

        if (!region.getWorld().getName().equals(p.getLocation().getWorld().getName()))
            throw new CommandException(Lang.get(_DIFFERENT_WORLD));

        if (region.contains(p.getLocation())) {

            Location loc = p.getLocation();

            Spawnpoint current = arena.getSpawns().get(name);
            if (current != null)
                throw new CommandException(Lang.get(_ALREADY_EXISTS, name));

            arena.getSpawns().add(new Spawnpoint(name, spawnType, team, loc));

            tellSuccess(sender, Lang.get(_SUCCESS, name, arena.getName()));
            tell(sender, TextUtils.formatLocation(loc, true));
        } else {
            throw new CommandException(Lang.get(_OUTSIDE_REGION));
        }
    }
}

