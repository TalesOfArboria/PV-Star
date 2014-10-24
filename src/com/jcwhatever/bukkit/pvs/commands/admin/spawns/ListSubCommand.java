package com.jcwhatever.bukkit.pvs.commands.admin.spawns;


import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.spawns.SpawnType;
import com.jcwhatever.bukkit.pvs.api.spawns.Spawnpoint;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@ICommandInfo(
        parent="spawns",
        command="list",
        staticParams={"page=1"},
        floatingParams={"type=$all"},
        usage="/{plugin-command} {command} list [page] [--type]",
        description="Shows a list of spawns for the selected arena.")

public class ListSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "{0} Spawnpoints";
    @Localizable static final String _LABEL_TYPE = "Type";
    @Localizable static final String _LABEL_TEAM = "Team";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        if (arena == null)
            return; // finish

        int page = args.getInt("page");

        String typeName = args.getString("type");

        SpawnType type = PVStarAPI.getSpawnTypeManager().getType(typeName);

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE, arena.getName()));

        List<SpawnType> spawnTypes = PVStarAPI.getSpawnTypeManager().getSpawnTypes();
        for (SpawnType spawnType : spawnTypes) {

            if (type == null || type == spawnType) {

                List<Spawnpoint> spawns = arena.getSpawnManager().getSpawns(spawnType);
                if (spawns == null)
                    continue;

                showSpawns(spawnType.getName(), pagin, spawns);
            }

        }

        pagin.show(sender, page, FormatTemplate.RAW);
    }

    private void showSpawns(String subHeader, ChatPaginator pagin, List<Spawnpoint> spawns) {
        pagin.addFormatted(FormatTemplate.SUB_HEADER, subHeader);

        String typeLabel = Lang.get(_LABEL_TYPE);
        String teamLabel = Lang.get(_LABEL_TEAM);

        for (Spawnpoint spawn : spawns) {

            String formatted = TextUtils.format("{YELLOW}{0}{GRAY} - {1}:{2}, {3}:{4}", spawn.getName(), typeLabel, spawn.getSpawnType().getName(), teamLabel, spawn.getTeam().name());

            pagin.addFormatted(FormatTemplate.RAW, formatted);
        }
    }

}
