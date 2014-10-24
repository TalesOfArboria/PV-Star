package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="arena",
        command="arenadamage",
        staticParams={"on|off|info=info"},
        usage="/{plugin-command} {command} arenadamage [on|off]",
        description="Set or view the settings for allowing players to damage the selected arena.")

public class ArenaDamageSubCommand extends AbstractPVCommand {

    @Localizable static final String _DAMAGE_ENABLED = "{WHITE}Players are {RED}allowed{WHITE} from damaging arena '{0}'.";
    @Localizable static final String _DAMAGE_DISABLED = "{WHITE}Players are {GREEN}prevented{WHITE} to damage arena '{0}'.";
    @Localizable static final String _DAMAGE_CHANGED_ENABLED = "Arena damage in arena '{0}' changed to Enabled.";
    @Localizable static final String _DAMAGE_CHANGED_DISABLED = "Arena damage in arena '{0}' changed to {RED}Disabled.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "on|off|info"));
        if (arena == null)
            return; // finish

        if (args.getString("on|off|info").equals("info")) {

            boolean isEnabled = arena.getSettings().isArenaDamageEnabled();

            if (isEnabled) {
                tell(sender, Lang.get(_DAMAGE_ENABLED, arena.getName()));
            }
            else {
                tell(sender, Lang.get(_DAMAGE_DISABLED, arena.getName()));
            }
        }
        else {
            boolean isEnabled = args.getBoolean("on|off|info");

            arena.getSettings().setArenaDamageEnabled(isEnabled);

            if (isEnabled) {
                tellSuccess(sender, Lang.get(_DAMAGE_CHANGED_ENABLED, arena.getName()));
            }
            else {
                tellSuccess(sender, Lang.get(_DAMAGE_CHANGED_DISABLED, arena.getName()));
            }

        }
    }
}
