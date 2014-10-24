package com.jcwhatever.bukkit.pvs.commands.users;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.generic.utils.TextUtils.TextColor;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.settings.ArenaSettings;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

@ICommandInfo(
        command="list",
        staticParams={"page=1"},
        usage="/{plugin-command} {command} [page]",
        description="Lists all available arenas.",
        permissionDefault= PermissionDefault.TRUE)

public class ListCommand extends AbstractPVCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Arenas";
    @Localizable static final String _LABEL_DISABLED = "(disabled)";
    @Localizable static final String _LABEL_HIDDEN = "(hidden)";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {


        int page = args.getInt("page");

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        List<Arena> arenas = PVStarAPI.getArenaManager().getArenas();


        String disabledLabel = Lang.get(_LABEL_DISABLED);
        String hiddenLabel = Lang.get(_LABEL_HIDDEN);

        for (Arena arena : arenas) {

            ArenaSettings settings = arena.getSettings();

            if (settings.isEnabled() && settings.isVisible())
                pagin.add(arena.getName(), settings.getTypeDisplayName());
            else if (!settings.isEnabled() && sender.isOp())
                pagin.add(TextColor.RED + arena.getName() + ' ' + disabledLabel, settings.getTypeDisplayName());
            else if (sender.isOp())
                pagin.add(TextColor.GRAY + arena.getName() + ' ' + hiddenLabel, settings.getTypeDisplayName());
        }

        pagin.show(sender, page, FormatTemplate.ITEM_DESCRIPTION);
    }
}
