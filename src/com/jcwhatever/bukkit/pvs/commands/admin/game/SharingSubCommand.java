package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="game",
        command="sharing",
        staticParams={"on|off|info=info"},
        usage="/{plugin-command} {command} sharing [on|off]",
        description="Allow or deny players to drop inventory items in the selected arena game.")

public class SharingSubCommand extends AbstractPVCommand {

    @Localizable static final String _SHARING_ENABLED = "Arena '{0}' game inventory sharing is enabled.";
    @Localizable static final String _SHARING_DISABLED = "Arena '{0}' game inventory sharing is {RED}disabled.";
    @Localizable static final String _SHARING_CHANGE_ENABLED = "Arena '{0}' game inventory sharing changed to enabled.";
    @Localizable static final String _SHARING_CHANGE_DISABLED = "Arena '{0}' game inventory sharing changed to {RED}disabled.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "on|off|info"));
        if (arena == null)
            return; // finish

        if (args.getString("on|off|info").equals("info")) {

            boolean isEnabled = arena.getGameManager().getSettings().isSharingEnabled();

            if (isEnabled) {
                tell(sender, Lang.get(_SHARING_ENABLED, arena.getName()));
            }
            else {
                tell(sender, Lang.get(_SHARING_DISABLED, arena.getName()));
            }
        }
        else {

            boolean isEnabled = args.getBoolean("on|off|info");

            arena.getGameManager().getSettings().setSharingEnabled(isEnabled);

            if (isEnabled) {
                tellSuccess(sender, Lang.get(_SHARING_CHANGE_ENABLED, arena.getName()));
            }
            else {
                tellSuccess(sender, Lang.get(_SHARING_CHANGE_DISABLED, arena.getName()));
            }
        }
    }
}
