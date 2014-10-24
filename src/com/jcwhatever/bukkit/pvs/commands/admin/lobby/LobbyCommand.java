package com.jcwhatever.bukkit.pvs.commands.admin.lobby;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

@ICommandInfo(
        command="lobby",
        description="Manage lobby settings.")

public class LobbyCommand extends AbstractPVCommand {

    public LobbyCommand() {
        super();

        registerSubCommand(ArmorDamageSubCommand.class);
        registerSubCommand(FallSubCommand.class);
        registerSubCommand(PvpSubCommand.class);
        registerSubCommand(ReserveSpawnsSubCommand.class);
        registerSubCommand(SharingSubCommand.class);
        registerSubCommand(HungerSubCommand.class);
        registerSubCommand(ImmobilizeSubCommand.class);
        registerSubCommand(ToolDamageSubCommand.class);
        registerSubCommand(WeaponDamageSubCommand.class);
    }
}
