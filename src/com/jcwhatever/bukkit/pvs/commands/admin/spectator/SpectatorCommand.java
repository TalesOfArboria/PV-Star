package com.jcwhatever.bukkit.pvs.commands.admin.spectator;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

@ICommandInfo(
        command="spectator",
        description="Manage spectator settings.")

public class SpectatorCommand extends AbstractPVCommand {

    public SpectatorCommand() {
        super();

        registerSubCommand(ArmorDamageSubCommand.class);
        registerSubCommand(FallSubCommand.class);
        registerSubCommand(HungerSubCommand.class);
        registerSubCommand(PvpSubCommand.class);
        registerSubCommand(SharingSubCommand.class);
        registerSubCommand(ToolDamageSubCommand.class);
        registerSubCommand(WeaponDamageSubCommand.class);
    }
}