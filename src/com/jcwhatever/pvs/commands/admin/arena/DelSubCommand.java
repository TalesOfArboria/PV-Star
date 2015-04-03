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


package com.jcwhatever.pvs.commands.admin.arena;

import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.commands.response.IRequestContext;
import com.jcwhatever.nucleus.commands.response.ResponseRequestor;
import com.jcwhatever.nucleus.commands.response.ResponseType;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.utils.observer.update.UpdateSubscriber;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent="arena",
        command="del",
        staticParams={"arenaName"},
        description="Delete the specified arena.",

        paramDescriptions = {
                "arenaName= The name of the arena to delete."})

public class DelSubCommand extends AbstractPVCommand {

    @Localizable static final String _DISABLE_FIRST =
            "You must disable the arena before it can be deleted.";

    @Localizable static final String _CONFIRM =
            "{WHITE}Please type '{YELLOW}/confirm{WHITE}' to delete arena '{0: arena name}'.";

    @Localizable static final String _SUCCESS =
            "Arena '{0: arena name}' deleted.";

    @Override
    public void execute(final CommandSender sender, CommandArguments args) throws CommandException {

        String arenaName = args.getString("arenaName");

        final Arena arena = getArena(sender, arenaName);
        if (arena == null)
            return; // finish

        if (arena.getSettings().isEnabled()) {
            tellError(sender, Lang.get(_DISABLE_FIRST));
            return; // finish
        }

        tell(sender, Lang.get(_CONFIRM, arena.getName()));

        ResponseRequestor.contextBuilder(PVStarAPI.getPlugin())
            .name("delete_" + arena.getName())
            .timeout(15)
            .response(ResponseType.CONFIRM)
            .build(sender)
            .onRespond(new UpdateSubscriber<IRequestContext>() {
                @Override
                public void on(IRequestContext context) {

                    if (!ResponseType.CONFIRM.equals(context.getResponse()))
                        return;

                    PVStarAPI.getArenaManager().removeArena(arena.getId());

                    Arena selectedArena = PVStarAPI.getArenaManager().getSelectedArena(sender);

                    if (selectedArena != null && selectedArena.equals(arena)) {
                        PVStarAPI.getArenaManager().setSelectedArena(sender, null);
                    }

                    tellSuccess(sender, Lang.get(_SUCCESS, arena.getName()));
                }
            })
            .sendRequest();
    }
}


