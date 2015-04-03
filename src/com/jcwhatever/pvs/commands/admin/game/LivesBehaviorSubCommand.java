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


package com.jcwhatever.pvs.commands.admin.game;

import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.arena.options.LivesBehavior;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="game",
        command="livesbehavior",
        staticParams={"static|reset|additive|info=info"},
        description="Set or view lives behavior when forwarded to the currently selected arena.",

        paramDescriptions = {
                "static|reset|additive|info= 'static' to keep current lives unchanged, " +
                        "'reset' to use next arena lives, 'additive' to add lives from previous and next arena," +
                        "'info' or leave blank to see the current setting."})

public class LivesBehaviorSubCommand extends AbstractPVCommand {

    @Localizable static final String _BEHAVIOR_INFO =
            "Player lives behavior in arena '{0: arena name}' is set to {1: behaviour}.";

    @Localizable static final String _BEHAVIOR_SET =
            "Player lives behavior in arena '{0: arena name}' changed to {1: behaviour}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws CommandException {

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