package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.commands.response.CommandRequests;
import com.jcwhatever.bukkit.generic.commands.response.ResponseHandler;
import com.jcwhatever.bukkit.generic.commands.response.ResponseType;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="arena",
        command="del",
        staticParams={"arenaName"},
        usage="/{plugin-command} {command} del <arenaName>",
        description="Delete the specified arena.")

public class DelSubCommand extends AbstractPVCommand {

    @Localizable static final String _DISABLE_FIRST = "You must disable the arena before it can be deleted.";
    @Localizable static final String _CONFIRM = "{WHITE}Please type '{YELLOW}/confirm{WHITE}' to delete arena '{0}'.";
    @Localizable static final String _SUCCESS = "Arena '{0}' deleted.";

    @Override
    public void execute(final CommandSender sender, CommandArguments args) throws InvalidValueException {

        String arenaName = args.getString("arenaName");

        final Arena arena = getArena(sender, arenaName);
        if (arena == null) {
            return; // finish
        }

        if (arena.getSettings().isEnabled()) {
            tellError(sender, Lang.get(_DISABLE_FIRST));
            return; // finish
        }

        tell(sender, Lang.get(_CONFIRM, arena.getName()));

        // get confirmation
        CommandRequests.request(PVStarAPI.getPlugin(), "delete_" + arena.getName(), sender, new ResponseHandler() {

            @Override
            public void onResponse(ResponseType response) {

                PVStarAPI.getArenaManager().removeArena(arena.getId());

                Arena selectedArena = PVStarAPI.getArenaManager().getSelectedArena(sender);

                if (selectedArena != null && selectedArena.equals(arena)) {
                    PVStarAPI.getArenaManager().setSelectedArena(sender, null);
                }

                tellSuccess(sender, Lang.get(_SUCCESS, arena.getName()));

            }
        }, ResponseType.CONFIRM);
    }

}


