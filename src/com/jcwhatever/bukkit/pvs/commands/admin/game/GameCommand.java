package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.game.autostart.AutostartCommand;

@ICommandInfo(
        command="game",
        description="Manage game settings.")

public class GameCommand extends AbstractPVCommand {

    public GameCommand() {
        super();

        registerSubCommand(ArmorDamageSubCommand.class);
        registerSubCommand(AutostartCommand.class);
        registerSubCommand(CleanupSubCommand.class);
        registerSubCommand(CountdownSubCommand.class);
        registerSubCommand(FallSubCommand.class);
        registerSubCommand(HungerSubCommand.class);
        registerSubCommand(LivesBehaviorSubCommand.class);
        registerSubCommand(LivesSubCommand.class);
        registerSubCommand(PointsSubCommand.class);
        registerSubCommand(PointsBehaviorSubCommand.class);
        registerSubCommand(PvpSubCommand.class);
        registerSubCommand(ReserveSpawnsSubCommand.class);
        registerSubCommand(SharingSubCommand.class);
        registerSubCommand(TeamPvpSubCommand.class);
        registerSubCommand(ToolDamageSubCommand.class);
        registerSubCommand(WeaponDamageSubCommand.class);
    }

}
