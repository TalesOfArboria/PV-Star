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
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="arena",
        command="enable",
        usage="/{plugin-command} {command} enable",
        description="Enable the currently selected arena.")

public class EnableSubCommand extends AbstractPVCommand {

    @Localizable static final String _ALREADY_ENABLED = "Arena '{0}' is already enabled.";
    @Localizable static final String _FAILED = "Failed to enable Arena '{0}'.";
    @Localizable static final String _SUCCESS = "Arena '{0}' enabled.";

    @Override
    public void execute(final CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        if (arena == null) {
            return; // finished
        }

        if (arena.getSettings().isEnabled()) {
            tellError(sender, Lang.get(_ALREADY_ENABLED, arena.getName()));
            return; // finished
        }

        arena.getSettings().setEnabled(true);

        if (arena.getSettings().isEnabled()) {
            tellSuccess(sender, Lang.get(_SUCCESS, arena.getName()));
        }
        else {
            tellError(sender, Lang.get(_FAILED, arena.getName()));
        }
    }
}
