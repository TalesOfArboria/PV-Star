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
import com.jcwhatever.bukkit.generic.performance.queued.QueueResult.CancelHandler;
import com.jcwhatever.bukkit.generic.performance.queued.QueueResult.FailHandler;
import com.jcwhatever.bukkit.generic.performance.queued.QueueResult.Future;
import com.jcwhatever.bukkit.generic.regions.BuildMethod;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

import org.bukkit.command.CommandSender;

import java.io.IOException;

@ICommandInfo(
        parent="arena",
        command="restoreregion",
        staticParams = { "fast|balanced|performance=performance" },
        usage="/{plugin-command} {command} restoreregion [fast|balanced|performance]",
        description="Restore the selected arenas region from disk.")

public class RestoreRegionSubCommand extends AbstractPVCommand {

    @Localizable static final String _CANNOT_RESTORE = "The region for arena '{0}' is not saved.";
    @Localizable static final String _RESTORING = "The region for arena '{0}' is being restored...";
    @Localizable static final String _CANCELLED = "Restore cancelled.";
    @Localizable static final String _FAILED = "There was an error that prevented the region from being restored.";
    @Localizable static final String _SUCCESS = "The region for arena '{0}' has been restored.";

    @Override
    public void execute(final CommandSender sender, CommandArguments args) throws InvalidValueException {

        final Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finished

        if (!arena.getRegion().canRestore()) {
            tellError(sender, Lang.get(_CANNOT_RESTORE, arena.getName()));
            return; // finished
        }

        BuildMethod method = args.getEnum("fast|balanced|performance", BuildMethod.class);

        tell(sender, Lang.get(_RESTORING, arena.getName()));

        Future future;

        try {
            future = arena.getRegion().restoreData(method);
        } catch (IOException e) {
            e.printStackTrace();
            tellError(sender, Lang.get(_FAILED));
            return; // finished
        }

        future.onFail(
                new FailHandler() {
                    @Override
                    public void run(String reason) {
                        tellError(sender, Lang.get(_FAILED, reason));
                    }
                })
                .onCancel(new CancelHandler() {
                    @Override
                    public void run(String reason) {
                        tell(sender, Lang.get(_CANCELLED, reason));
                    }
                })
                .onComplete(new Runnable() {

                    @Override
                    public void run() {

                        tellSuccess(sender, Lang.get(_SUCCESS, arena.getName()));
                    }
                });
    }
}

