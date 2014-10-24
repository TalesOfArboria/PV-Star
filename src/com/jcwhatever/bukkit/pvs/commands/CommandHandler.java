package com.jcwhatever.bukkit.pvs.commands;

import com.jcwhatever.bukkit.generic.commands.AbstractCommandHandler;
import com.jcwhatever.bukkit.pvs.commands.admin.arena.ArenaCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.ext.ExtCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.game.GameCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.kits.KitsCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.lobby.LobbyCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.modules.ModulesCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.points.PointsCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.scripts.ScriptsCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.signs.SignsCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.spawns.SpawnsCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.spectator.SpectatorCommand;
import com.jcwhatever.bukkit.pvs.commands.users.JoinCommand;
import com.jcwhatever.bukkit.pvs.commands.users.LeaveCommand;
import com.jcwhatever.bukkit.pvs.commands.users.ListCommand;
import com.jcwhatever.bukkit.pvs.commands.users.VoteCommand;
import org.bukkit.plugin.Plugin;

public class CommandHandler extends AbstractCommandHandler {

    /**
     * Constructor
     *
     * @param plugin The plugin the command handler is for
     */
    public CommandHandler(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected void registerCommands() {

        // admin commands
        registerCommand(ArenaCommand.class);
        registerCommand(ExtCommand.class);
        registerCommand(GameCommand.class);
        registerCommand(KitsCommand.class);
        registerCommand(LobbyCommand.class);
        registerCommand(ModulesCommand.class);
        registerCommand(PointsCommand.class);
        registerCommand(ScriptsCommand.class);
        registerCommand(SignsCommand.class);
        registerCommand(SpawnsCommand.class);
        registerCommand(SpectatorCommand.class);

        // user commands
        registerCommand(JoinCommand.class);
        registerCommand(LeaveCommand.class);
        registerCommand(ListCommand.class);
        registerCommand(VoteCommand.class);

    }
}
