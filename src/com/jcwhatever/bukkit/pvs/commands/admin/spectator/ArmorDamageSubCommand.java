package com.jcwhatever.bukkit.pvs.commands.admin.spectator;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="spectator",
        command="armordamage",
        staticParams={"on|off|info=info"},
        usage="/{plugin-command} {command} armordamage [on|off]",
        description="Allow or prevent player armor damage/durability in the selected arena spectator area.")

public class ArmorDamageSubCommand extends AbstractPVCommand {

    @Localizable static final String _ARMOR_DAMAGE_ENABLED = "Arena '{0}' spectator Armor Damage is enabled.";
    @Localizable static final String _ARMOR_DAMAGE_DISABLED = "Arena '{0}' spectator Armor Damage is {RED}disabled.";
    @Localizable static final String _ARMOR_DAMAGE_CHANGE_ENABLED = "Arena '{0}' spectator Armor Damage changed to enabled.";
    @Localizable static final String _ARMOR_DAMAGE_CHANGE_DISABLED = "Arena '{0}' spectator Armor Damage changed to {RED}disabled.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "on|off|info"));
        if (arena == null)
            return; // finish

        if (args.getString("on|off|info").equals("info")) {

            boolean isEnabled = arena.getSpectatorManager().getSettings().isArmorDamageable();

            if (isEnabled) {
                tell(sender, Lang.get(_ARMOR_DAMAGE_ENABLED, arena.getName()));
            }
            else {
                tell(sender, Lang.get(_ARMOR_DAMAGE_DISABLED, arena.getName()));
            }
        }
        else {

            boolean isEnabled = args.getBoolean("on|off|info");

            arena.getSpectatorManager().getSettings().setArmorDamageable(isEnabled);

            if (isEnabled) {
                tellSuccess(sender, Lang.get(_ARMOR_DAMAGE_CHANGE_ENABLED, arena.getName()));
            }
            else {
                tellSuccess(sender, Lang.get(_ARMOR_DAMAGE_CHANGE_DISABLED, arena.getName()));
            }
        }
    }
}

