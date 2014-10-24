package com.jcwhatever.bukkit.pvs.commands.admin.lobby;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="lobby",
        command="fall",
        staticParams={"on|off|info=info"},
        usage="/{plugin-command} {command} fall [on|off]",
        description="Allow or prevent player fall damage in the selected arena lobby.")

public class FallSubCommand extends AbstractPVCommand {

    @Localizable static final String _HUNGER_ENABLED = "Arena '{0}' game Fall Damage is enabled.";
    @Localizable static final String _HUNGER_DISABLED = "Arena '{0}' game Fall Damage is {RED}disabled.";
    @Localizable static final String _HUNGER_CHANGE_ENABLED = "Arena '{0}' game Fall Damage changed to enabled.";
    @Localizable static final String _HUNGER_CHANGE_DISABLED = "Arena '{0}' game Fall Damage changed to {RED}disabled.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "on|off|info"));
        if (arena == null)
            return; // finish

        if (args.getString("on|off|info").equals("info")) {

            boolean isEnabled = arena.getLobbyManager().getSettings().hasFallDamage();

            if (isEnabled) {
                tell(sender, Lang.get(_HUNGER_ENABLED, arena.getName()));
            }
            else {
                tell(sender, Lang.get(_HUNGER_DISABLED, arena.getName()));
            }
        }
        else {

            boolean isEnabled = args.getBoolean("on|off|info");

            arena.getLobbyManager().getSettings().setFallDamage(isEnabled);

            if (isEnabled) {
                tellSuccess(sender, Lang.get(_HUNGER_CHANGE_ENABLED, arena.getName()));
            }
            else {
                tellSuccess(sender, Lang.get(_HUNGER_CHANGE_DISABLED, arena.getName()));
            }
        }
    }
}

