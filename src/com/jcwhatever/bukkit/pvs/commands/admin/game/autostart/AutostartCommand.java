package com.jcwhatever.bukkit.pvs.commands.admin.game.autostart;

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
        command="autostart",
        staticParams={"on|off|info=info"},
        usage="/{plugin-command} {command} autostart [on|off]",
        description="Turn autostart on or off for the selected arena.")

public class AutostartCommand extends AbstractPVCommand {

    @Localizable static final String _AUTOSTART_ENABLED = "Arena '{0}' autostart is enabled.";
    @Localizable static final String _AUTOSTART_DISABLED = "Arena '{0}' autostart is {RED}disabled.";
    @Localizable static final String _AUTOSTART_CHANGE_ENABLED = "Arena '{0}' autostart changed to enabled.";
    @Localizable static final String _AUTOSTART_CHANGE_DISABLED = "Arena '{0}' autostart changed to {RED}disabled.";

    public AutostartCommand() {
        super();

        registerSubCommand(MinPlayersSubCommand.class);
    }

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        if (args.getString("on|off|info").equals("info")) {

            boolean isEnabled = arena.getLobbyManager().getSettings().hasAutoStart();

            if (isEnabled) {
                tell(sender, Lang.get(_AUTOSTART_ENABLED, arena.getName()));
            }
            else {
                tell(sender, Lang.get(_AUTOSTART_DISABLED, arena.getName()));
            }
        }
        else {

            boolean isEnabled = args.getBoolean("on|off|info");

            arena.getLobbyManager().getSettings().setAutoStart(isEnabled);

            if (isEnabled) {
                tellSuccess(sender, Lang.get(_AUTOSTART_CHANGE_ENABLED, arena.getName()));
            }
            else {
                tellSuccess(sender, Lang.get(_AUTOSTART_CHANGE_DISABLED, arena.getName()));
            }
        }
    }



}

