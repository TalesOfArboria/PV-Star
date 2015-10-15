/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.pvs.commands.admin.spectator;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.mixins.ITabCompletable;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.managed.teleport.TeleportMode;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

import java.util.Collection;

@CommandInfo(
        parent="spectator",
        command="teleportmode",
        staticParams={"mode|info=info"},
        description="Set or view the teleport mode used to add or within the spectator context.",

        paramDescriptions = {
                "mode= Specify the teleport mode using one of the following: "
                        + "TARGET_ONLY, MOUNTS, LEASHED, MOUNTS_AND_LEASHED"})

public class TeleportModeSubCommand extends AbstractPVCommand
        implements IExecutableCommand, ITabCompletable {

    @Localizable static final String _CURRENT_MODE =
            "Arena '{0: arena name}' spectator teleport mode is {1: teleport mode}.";

    @Localizable static final String _SUCCESS =
            "Arena '{0: arena name}' spectator teleport mode set to {1: teleport mode}.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        IArena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "mode"));
        if (arena == null)
            return; // finish

        if (args.getString("mode").equals("info")) {

            TeleportMode mode = arena.getSpectators().getSettings().getTeleportMode();

            tell(sender, Lang.get(_CURRENT_MODE, arena.getName(), mode.name()));
        }
        else {

            TeleportMode mode = args.getEnum("mode", TeleportMode.class);

            arena.getSpectators().getSettings().setTeleportMode(mode);

            tellSuccess(sender, Lang.get(_SUCCESS, arena.getName(), mode.name()));
        }
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] arguments, Collection<String> completions) {
        tabCompleteEnum(arguments, TeleportMode.class, completions);
    }
}

