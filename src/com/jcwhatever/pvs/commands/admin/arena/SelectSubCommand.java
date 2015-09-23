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


package com.jcwhatever.pvs.commands.admin.arena;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.mixins.ITabCompletable;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.utils.CollectionUtils;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

@CommandInfo(
        parent="arena",
        command="select",
        staticParams={"arenaName"},
        description="Set an arena as your selected arena.",

        paramDescriptions = {
                "arenaName= The name of the arena to select."})

public class SelectSubCommand extends AbstractPVCommand
        implements IExecutableCommand, ITabCompletable {

    @Localizable static final String _SUCCESS =
            "'{0: arena name}' is your selected arena.";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        String arenaName = args.getString("arenaName");

        IArena arena = getArena(sender, arenaName);
        if (arena == null)
            return; // finish

        PVStarAPI.getArenaManager().setSelectedArena(sender, arena);

        tellSuccess(sender, Lang.get(_SUCCESS, arena.getName()));
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] arguments, Collection<String> completions) {
        List<IArena> arenas = PVStarAPI.getArenaManager().getArenas();
        tabCompleteSearch(arguments, arenas, completions, new CollectionUtils.ISearchTextGetter<IArena>() {
            @Override
            public String getText(IArena element) {
                return element.getName();
            }
        });
    }
}
