/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.jcwhatever.pvs.commands;

import com.jcwhatever.nucleus.commands.CommandDispatcher;
import com.jcwhatever.pvs.commands.admin.arena.ArenaCommand;
import com.jcwhatever.pvs.commands.admin.ext.ExtCommand;
import com.jcwhatever.pvs.commands.admin.game.GameCommand;
import com.jcwhatever.pvs.commands.admin.lobby.LobbyCommand;
import com.jcwhatever.pvs.commands.admin.modules.ModulesCommand;
import com.jcwhatever.pvs.commands.admin.points.PointsCommand;
import com.jcwhatever.pvs.commands.admin.signs.SignsCommand;
import com.jcwhatever.pvs.commands.admin.spawns.SpawnsCommand;
import com.jcwhatever.pvs.commands.admin.spectator.SpectatorCommand;
import com.jcwhatever.pvs.commands.users.JoinCommand;
import com.jcwhatever.pvs.commands.users.LeaveCommand;
import com.jcwhatever.pvs.commands.users.ListCommand;
import com.jcwhatever.pvs.commands.users.VoteCommand;

import org.bukkit.plugin.Plugin;

public class PVCommandDispatcher extends CommandDispatcher {

    /**
     * Constructor
     *
     * @param plugin The plugin the command handler is for
     */
    public PVCommandDispatcher(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected void registerCommands() {

        // admin commands
        registerCommand(ArenaCommand.class);
        registerCommand(ExtCommand.class);
        registerCommand(GameCommand.class);
        registerCommand(LobbyCommand.class);
        registerCommand(ModulesCommand.class);
        registerCommand(PointsCommand.class);
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
