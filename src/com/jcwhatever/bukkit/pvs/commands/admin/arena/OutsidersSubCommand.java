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
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.options.OutsidersAction;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.Lang;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="arena",
        command="outsiders",
        staticParams={"none|join|kick|info=info"},
        usage="/{plugin-command} {command} outsiders [none|join|kick]",
        description="Set or view the action taken when a non-arena player enters the selected arenas region.")

public class OutsidersSubCommand extends AbstractPVCommand {

    @Localizable static final String _VIEW_OUTSIDERS = "Action taken when outsiders enter region for arena '{0}' is {1}.";
    @Localizable static final String _SET_OUTSIDERS = "Action taken when outsiders enter region for arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "none|join|kick|info"));
        if (arena == null)
            return; // finish

        if (args.getString("none|join|kick|info").equals("info")) {
            tell(sender, Lang.get(_VIEW_OUTSIDERS, arena.getName(), arena.getSettings().getOutsidersAction().name()));
        }
        else {
            OutsidersAction action = args.getEnum("none|join|kick|info", OutsidersAction.class);
            arena.getSettings().setOutsidersAction(action);

            tellSuccess(sender, Lang.get(_SET_OUTSIDERS, arena.getName(), action.name()));
        }
    }
}
