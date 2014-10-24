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
        command="mobspawn",
        staticParams={"on|off|info=info"},
        usage="/{plugin-command} {command} mobspawn [on|off]",
        description="Set or view the settings for allowing natural mob spawns in the selected arena.")

public class MobSpawnSubCommand extends AbstractPVCommand {

    @Localizable static final String _SPAWNING_ENABLED = "{WHITE}Natural mob spawning in arena '{0}' is {GREEN}Enabled{WHITE}.";
    @Localizable static final String _SPAWNING_DISABLED = "{WHITE}Natural mob spawning in arena '{0}' is {RED}Disabled{WHITE}.";
    @Localizable static final String _SPAWNING_CHANGED_ENABLED = "Natural mob spawning in arena '{0}' changed to Enabled.";
    @Localizable static final String _SPAWNING_CHANGED_DISABLED = "Natural mob spawning in arena '{0}' changed to {RED}Disabled.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "on|off|info"));
        if (arena == null)
            return; // finish

        if (args.getString("on|off|info").equals("info")) {

            boolean isEnabled = arena.getSettings().isMobSpawnEnabled();

            if (isEnabled) {
                tell(sender, Lang.get(_SPAWNING_ENABLED, arena.getName()));
            }
            else {
                tell(sender, Lang.get(_SPAWNING_DISABLED, arena.getName()));
            }
        }
        else {
            boolean isEnabled = args.getBoolean("on|off|info");

            arena.getSettings().setMobSpawnEnabled(isEnabled);

            if (isEnabled) {
                tellSuccess(sender, Lang.get(_SPAWNING_CHANGED_ENABLED, arena.getName()));
            }
            else {
                tellSuccess(sender, Lang.get(_SPAWNING_CHANGED_DISABLED, arena.getName()));
            }

        }
    }
}

