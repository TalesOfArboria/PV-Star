/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
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

package com.jcwhatever.pvs.commands.admin.arena;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.options.DropsCleanup;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="arena",
        command="drops-cleanup",
        staticParams={"off|before|after="},
        description="Set or view the item drops cleanup behaviour in your selected arena.",

        paramDescriptions = {
                "behaviour= The item drop behaviour."})

public class DropsCleanupSubCommand extends AbstractPVCommand implements IExecutableCommand {

    @Localizable
    static final String _SET_BEHAVIOUR =
            "Item drops cleanup behaviour in arena '{0: arena name}' has been set to {1: behaviour}.";

    @Localizable static final String _VIEW_BEHAVIOUR =
            "Item drops cleanup behaviour in arena '{0: arena name}' is {1: behaviour}.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        IArena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "off|before|after"));
        if (arena == null)
            return; // finish

        if (args.isDefaultValue("off|before|after")) {
            tell(sender, Lang.get(_VIEW_BEHAVIOUR,
                    arena.getName(), arena.getSettings().getDropsCleanup().name()));
        }
        else {
            DropsCleanup behaviour = args.getEnum("off|before|after", DropsCleanup.class);

            arena.getSettings().setDropsCleanup(behaviour);

            tellSuccess(sender, Lang.get(_SET_BEHAVIOUR, arena.getName(), behaviour));
        }
    }
}

