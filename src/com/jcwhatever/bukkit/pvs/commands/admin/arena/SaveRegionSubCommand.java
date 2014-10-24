package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.performance.queued.QueueResult.CancelHandler;
import com.jcwhatever.bukkit.generic.performance.queued.QueueResult.FailHandler;
import com.jcwhatever.bukkit.generic.performance.queued.QueueResult.Future;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import org.bukkit.command.CommandSender;

import java.io.IOException;

@ICommandInfo(
        parent="arena",
        command="saveregion",
        usage="/{plugin-command} {command} saveregion",
        description="Save the selected arenas region to disk.")

public class SaveRegionSubCommand extends AbstractPVCommand {

    @Localizable static final String _SAVING = "The region for arena '{0}' is being saved...";
    @Localizable static final String _CANCELLED = "Save cancelled.";
    @Localizable static final String _FAILED = "There was an error that prevented the region from being saved.";
    @Localizable static final String _SUCCESS = "The region for arena '{0}' has been saved.";

    @Override
    public void execute(final CommandSender sender, CommandArguments args) throws InvalidValueException {

        final Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        tell(sender, Lang.get(_SAVING, arena.getName()));

        Future future;

        try {
            future = arena.getRegion().saveData();
        } catch (IOException e) {
            e.printStackTrace();
            tellError(sender, Lang.get(_FAILED));
            return;
        }

        future.onFail(new FailHandler() {
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
