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


package com.jcwhatever.pvs.commands.admin.game.autostart;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="autostart",
        command="minplayers",
        staticParams={"amount=info"},
        description="Set or view autostart minimum players in the currently selected arena.",

        paramDescriptions = {
                "amount= The minimum number of players. Leave blank to see current setting."})

public class MinPlayersSubCommand extends AbstractPVCommand implements IExecutableCommand {

    @Localizable static final String _MIN_PLAYERS_INFO =
            "Minimum autostart players in arena '{0: arena name}' is set to {1: number of players}.";

    @Localizable static final String _MIN_PLAYERS_SET =
            "Minimum autostart players in arena '{0: arena name}' changed to {1: number of players}.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        IArena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNING);
        if (arena == null)
            return; // finish

        if (args.getString("amount").equals("info")) {

            int min = arena.getLobby().getSettings().getMinAutoStartPlayers();
            tell(sender, Lang.get(_MIN_PLAYERS_INFO, arena.getName(), min));
        }
        else {

            int min = args.getInteger("amount");

            arena.getLobby().getSettings().setMinAutoStartPlayers(min);

            tellSuccess(sender, Lang.get(_MIN_PLAYERS_SET, arena.getName(), min));
        }
    }
}