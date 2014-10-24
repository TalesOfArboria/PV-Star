package com.jcwhatever.bukkit.pvs.commands.admin.points;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.points.PointsHandler;
import com.jcwhatever.bukkit.pvs.api.points.PointsType;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;

import java.util.List;

@ICommandInfo(
        parent="points",
        command="current",
        staticParams={ "page=1" },
        usage="/{plugin-command} {command} current [page]",
        description="List points types enabled in the selected arena.")

public class CurrentSubCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Points Types in Arena '{0}'";
    @Localizable static final String _POINTS = "{0} points";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        if (arena == null)
            return; // finished

        int page = args.getInt("page");

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE, arena.getName()));

        List<PointsType> types = PVStarAPI.getPointsManager().getTypes(arena);

        for (PointsType type : types) {

            PointsHandler handler = type.getHandler(arena);
            if (handler == null)
                continue;

            pagin.add(type.getName(), Lang.get(_POINTS, handler.getPoints()));
        }

        pagin.show(sender, page, FormatTemplate.DEFINITION);
    }
}