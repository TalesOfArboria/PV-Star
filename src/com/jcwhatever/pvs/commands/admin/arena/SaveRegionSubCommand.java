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

import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.nucleus.utils.observer.result.FutureResultAgent.Future;
import com.jcwhatever.nucleus.utils.observer.result.FutureSubscriber;
import com.jcwhatever.nucleus.utils.observer.result.Result;
import com.jcwhatever.nucleus.utils.performance.queued.QueueTask;

import org.bukkit.command.CommandSender;

import java.io.IOException;

@CommandInfo(
        parent="arena",
        command="saveregion",
        description="Save the selected arenas region to disk.")

public class SaveRegionSubCommand extends AbstractPVCommand {

    @Localizable static final String _SAVING =
            "The region for arena '{0: arena name}' is being saved...";

    @Localizable static final String _CANCELLED =
            "Save cancelled.";

    @Localizable static final String _FAILED =
            "There was an error that prevented the region from being saved.";

    @Localizable static final String _SUCCESS =
            "The region for arena '{0: arena name}' has been saved.";

    @Override
    public void execute(final CommandSender sender, CommandArguments args) throws CommandException {

        final Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        tell(sender, Lang.get(_SAVING, arena.getName()));

        Future<QueueTask> future;

        try {
            future = arena.getRegion().saveData();
        } catch (IOException e) {
            e.printStackTrace();
            tellError(sender, Lang.get(_FAILED));
            return;
        }

        future.onError(new FutureSubscriber<QueueTask>() {
            @Override
            public void on(Result<QueueTask> result) {
                tellError(sender, Lang.get(_FAILED, result.getMessage()));
            }
        })
        .onCancel(new FutureSubscriber<QueueTask>() {
            @Override
            public void on(Result<QueueTask> result) {
                tell(sender, Lang.get(_CANCELLED, result.getMessage()));
            }
        })
        .onSuccess(new FutureSubscriber<QueueTask>() {
            @Override
            public void on(Result<QueueTask> result) {
                tellSuccess(sender, Lang.get(_SUCCESS, arena.getName()));
            }
        });
    }
}
