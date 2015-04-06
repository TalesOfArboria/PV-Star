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
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.regions.BuildMethod;
import com.jcwhatever.nucleus.utils.observer.result.FutureResultAgent.Future;
import com.jcwhatever.nucleus.utils.observer.result.FutureSubscriber;
import com.jcwhatever.nucleus.utils.observer.result.Result;
import com.jcwhatever.nucleus.utils.performance.queued.QueueTask;

import org.bukkit.command.CommandSender;

import java.io.IOException;

@CommandInfo(
        parent="arena",
        command="restoreregion",
        staticParams = { "fast|balanced|performance=performance" },
        description="Restore the selected arenas region from disk.",

        paramDescriptions = {
                "fast|balanced|performance= Optional. Use one of the values to " +
                        "set the build mode. Default is 'performance'."})

public class RestoreRegionSubCommand extends AbstractPVCommand {

    @Localizable static final String _CANNOT_RESTORE =
            "The region for arena '{0: arena name}' is not saved.";

    @Localizable static final String _RESTORING =
            "The region for arena '{0: arena name}' is being restored...";

    @Localizable static final String _CANCELLED =
            "Restore cancelled.";

    @Localizable static final String _FAILED =
            "There was an error that prevented the region from being restored.";

    @Localizable static final String _SUCCESS =
            "The region for arena '{0: arena name}' has been restored.";

    @Override
    public void execute(final CommandSender sender, CommandArguments args) throws CommandException {

        final IArena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNING);
        if (arena == null)
            return; // finished

        if (!arena.getRegion().canRestore()) {
            tellError(sender, Lang.get(_CANNOT_RESTORE, arena.getName()));
            return; // finished
        }

        BuildMethod method = args.getEnum("fast|balanced|performance", BuildMethod.class);

        tell(sender, Lang.get(_RESTORING, arena.getName()));

        Future<QueueTask> future;

        try {
            future = arena.getRegion().restoreData(method);
        } catch (IOException e) {
            e.printStackTrace();
            tellError(sender, Lang.get(_FAILED));
            return; // finished
        }

        future.onError(
                new FutureSubscriber<QueueTask>() {
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

