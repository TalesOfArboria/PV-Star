/* This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


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
import com.jcwhatever.bukkit.pvs.Lang;
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



