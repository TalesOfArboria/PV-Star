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


package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.options.LivesBehavior;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="game",
        command="livesbehavior",
        staticParams={"static|reset|additive|info=info"},
        usage="/{plugin-command} {command} livesbehavior [static|reset|additive]",
        description="Set or view lives behavior when forwarded to the currently selected arena.")

public class LivesBehaviorSubCommand extends AbstractPVCommand {

    @Localizable static final String _BEHAVIOR_INFO = "Player lives behavior in arena '{0}' is set to {1}.";
    @Localizable static final String _BEHAVIOR_SET = "Player lives behavior in arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "static|reset|additive|info"));
        if (arena == null)
            return; // finish

        if (args.getString("static|reset|additive|info").equals("info")) {

            LivesBehavior behavior = arena.getGameManager().getSettings().getLivesBehavior();
            tell(sender, Lang.get(_BEHAVIOR_INFO, arena.getName(), behavior));
        }
        else {

            LivesBehavior behavior = args.getEnum("static|reset|additive|info", LivesBehavior.class);

            arena.getGameManager().getSettings().setLivesBehavior(behavior);

            tellSuccess(sender, Lang.get(_BEHAVIOR_SET, arena.getName(), behavior));
        }
    }
}