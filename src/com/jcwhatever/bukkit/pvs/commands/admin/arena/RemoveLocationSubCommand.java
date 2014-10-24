package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.arguments.LocationResponse;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.LocationUtils;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ICommandInfo(
        parent="arena",
        command="removelocation",
        staticParams={"current|select|info=info"},
        usage="/{plugin-command} {command} removelocation [current|select]",
        description="Set or view the location that players are teleported to when they are removed from the selected arena.")

public class RemoveLocationSubCommand extends AbstractPVCommand {

    @Localizable static final String _VIEW_LOCATION = "Kick location for arena '{0}' is:";
    @Localizable static final String _SET_LOCATION = "Kick location for arena '{0}' set to:";

    @Override
    public void execute(final CommandSender sender, CommandArguments args) throws InvalidValueException {

        final Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "current|select|info"));
        if (arena == null)
            return; // finish

        if (args.getString("current|select|info").equals("info")) {

            tell(sender, Lang.get(_VIEW_LOCATION, arena.getName()));
            tell(sender, LocationUtils.locationToString(arena.getSettings().getRemoveLocation()));
        }
        else {

            args.getLocation(sender, "current|select|info", new LocationResponse() {

                @Override
                public void onLocationRetrieved(Player p, Location result) {

                    arena.getSettings().setRemoveLocation(result);

                    tellSuccess(sender, Lang.get(_SET_LOCATION, arena.getName()));
                    tell(sender, LocationUtils.locationToString(arena.getSettings().getRemoveLocation()));
                }

            });
        }
    }
}
