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


package com.jcwhatever.pvs.commands.admin.ext;

import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="ext",
        command="add",
        staticParams = { "extName" },
        description="Add an extension to the currently selected arena.",

        paramDescriptions = {
                "extName= The name of the arena extension to add."})

public class AddSubCommand extends AbstractPVCommand {

    @Localizable static final String _EXT_NOT_FOUND =
            "An arena extension named '{0: extension name}' was not found.";

    @Localizable static final String _FAILED =
            "Failed to add extension '{0: extension name}' to arena '{1: arena name}'.";

    @Localizable static final String _SUCCESS =
            "Added extension '{0: extension name}' to arena '{1: arena name}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

        IArena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNING);
        if (arena == null)
            return; // finish

        String extName = args.getName("extName");

        if (PVStarAPI.getExtensionManager().getExtensionClass(extName) == null) {
            tellError(sender, Lang.get(_EXT_NOT_FOUND, extName));
            return; // finish
        }

        ArenaExtension extension = arena.getExtensions().add(extName);
        if (extension == null) {
            tellError(sender, Lang.get(_FAILED, extName, arena.getName()));
            return; // finish
        }

        tellSuccess(sender, Lang.get(_SUCCESS, extension.getName(), arena.getName()));
    }
}
