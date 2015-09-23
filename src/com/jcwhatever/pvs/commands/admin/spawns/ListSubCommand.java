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


package com.jcwhatever.pvs.commands.admin.spawns;


import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.managed.messaging.ChatPaginator;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.nucleus.utils.text.components.IChatMessage;
import com.jcwhatever.pvs.Lang;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.pvs.api.spawns.SpawnType;
import com.jcwhatever.pvs.api.spawns.Spawnpoint;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandInfo(
        parent="spawns",
        command="list",
        staticParams={"page=1"},
        floatingParams={"type=$all", "search="},
        description="Shows a list of spawns for the selected arena.",

        paramDescriptions = {
                "page= {PAGE}",
                "type= Optional. Specify the name of the spawn type to filter the results by.",
                "search= Optional. Specify a search filter."})

public class ListSubCommand extends AbstractPVCommand implements IExecutableCommand {

    @Localizable static final String _PAGINATOR_TITLE =
            "{0: arena name} Spawnpoints";

    @Localizable static final String _LABEL_TYPE = "Type";
    @Localizable static final String _LABEL_TEAM = "Team";

    @Override
    public void execute(CommandSender sender, ICommandArguments args) throws CommandException {

        IArena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        if (arena == null)
            return; // finish

        int page = args.getInteger("page");

        String typeName = args.getString("type");

        SpawnType type = PVStarAPI.getSpawnTypeManager().getType(typeName);

        ChatPaginator pagin = createPagin(args, 7, Lang.get(_PAGINATOR_TITLE, arena.getName()));

        List<SpawnType> spawnTypes = PVStarAPI.getSpawnTypeManager().getSpawnTypes();
        for (SpawnType spawnType : spawnTypes) {

            if (type == null || type == spawnType) {

                List<Spawnpoint> spawns = arena.getSpawns().getAll(spawnType);
                if (spawns == null)
                    continue;

                showSpawns(spawnType.getName(), pagin, spawns);
            }

        }

        if (!args.isDefaultValue("search"))
            pagin.setSearchTerm(args.getString("search"));

        pagin.show(sender, page, FormatTemplate.RAW);
    }

    private void showSpawns(String subHeader, ChatPaginator pagin, List<Spawnpoint> spawns) {

        if (spawns.isEmpty())
            return;

        pagin.addFormatted(FormatTemplate.SUB_HEADER, subHeader);

        String typeLabel = Lang.get(_LABEL_TYPE).toString();
        String teamLabel = Lang.get(_LABEL_TEAM).toString();

        for (Spawnpoint spawn : spawns) {

            IChatMessage formatted = TextUtils.format("{YELLOW}{0}{GRAY} - {1}:{2}, {3}:{4}",
                    spawn.getName(), typeLabel, spawn.getSpawnType().getName(), teamLabel, spawn.getTeam().name());

            pagin.addFormatted(FormatTemplate.RAW, formatted);
        }
    }

}
