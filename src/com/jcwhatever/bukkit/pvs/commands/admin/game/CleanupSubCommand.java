package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="game",
        command="cleanup",
        staticParams={"on|off|info=info"},
        usage="/{plugin-command} {command} cleanup [on|off]",
        description="Set or view the post game entity cleanup setting in the selected arena.")

public class CleanupSubCommand extends AbstractPVCommand {

    @Localizable static final String _CLEANUP_ENABLED = "Arena '{0}' post game cleanup is enabled.";
    @Localizable static final String _CLEANUP_DISABLED = "Arena '{0}' post game cleanup is {RED}disabled.";
    @Localizable static final String _CLEANUP_CHANGE_ENABLED = "Arena '{0}' post game cleanup changed to enabled.";
    @Localizable static final String _CLEANUP_CHANGE_DISABLED = "Arena '{0}' post game cleanup changed to {RED}disabled.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "on|off|info"));
        if (arena == null)
            return; // finish

        if (args.getString("on|off|info").equals("info")) {

            boolean isEnabled = arena.getGameManager().getSettings().hasPostGameEntityCleanup();

            if (isEnabled) {
                tell(sender, Lang.get(_CLEANUP_ENABLED, arena.getName()));
            }
            else {
                tell(sender, Lang.get(_CLEANUP_DISABLED, arena.getName()));
            }
        }
        else {

            boolean isEnabled = args.getBoolean("on|off|info");

            arena.getGameManager().getSettings().setPostGameEntityCleanup(isEnabled);

            if (isEnabled) {
                tellSuccess(sender, Lang.get(_CLEANUP_CHANGE_ENABLED, arena.getName()));
            }
            else {
                tellSuccess(sender, Lang.get(_CLEANUP_CHANGE_DISABLED, arena.getName()));
            }
        }
    }
}
