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


package com.jcwhatever.bukkit.pvs.commands.admin.ext;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="ext",
        command="del",
        staticParams = { "extName" },
        usage="/{plugin-command} {command} del <extName>",
        description="Remove an extension from the currently selected arena.")

public class DelSubCommand extends AbstractPVCommand {

    @Localizable static final String _EXT_NOT_FOUND = "An arena extension named '{0}' was not found.";
    @Localizable static final String _EXT_NOT_IN_ARENA = "Arena extension '{0}' is not installed in arena '{1}'.";
    @Localizable static final String _FAILED =  "Failed to remove extension '{0}' from arena '{1}'.";
    @Localizable static final String _SUCCESS =  "Removed extension '{0}' from arena '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidCommandSenderException, InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        String extName = args.getName("extName");

        if (PVStarAPI.getExtensionManager().getExtensionClass(extName) == null) {
            tellError(sender, Lang.get(_EXT_NOT_FOUND, extName));
            return; // finish
        }

        ArenaExtension extension = arena.getExtensionManager().get(extName);
        if (extension == null) {
            tellError(sender, Lang.get(_EXT_NOT_IN_ARENA, extName, arena.getName()));
            return; // finish
        }

        if (!arena.getExtensionManager().remove(extName)) {
            tellError(sender, Lang.get(_FAILED, extName, arena.getName()));
            return; // finish
        }

        tellSuccess(sender, Lang.get(_SUCCESS, extName, arena.getName()));
    }
}

