package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.regions.RegionSelection;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;

import com.jcwhatever.bukkit.pvs.api.arena.ArenaRegion;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ICommandInfo(
        parent="arena",
        command="setregion",
        usage="/{plugin-command} {command} setregion",
        description="Sets the region of the selected arena using the current World Edit selection.")

public class SetRegionSubCommand extends AbstractPVCommand {

    @Localizable static final String _MUST_BE_SAME_WORLD = "The new region selection must be in the same world as the current region.";
    @Localizable static final String _SUCCESS =  "Region for arena '{0}' has been set.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidCommandSenderException {

        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER, "Console cannot select regions.");

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        Player p = (Player)sender;

        RegionSelection sel = getWorldEditSelection(p);
        if (sel == null)
            return; // finish

        Location locationA = sel.getP1();
        Location locationB = sel.getP2();

        ArenaRegion region = arena.getRegion();

        if (region.isDefined() && !locationA.getWorld().equals(region.getP1().getWorld())) {
            tellError(sender, Lang.get(_MUST_BE_SAME_WORLD));
            return; // finish
        }

        region.setCoords(locationA, locationB);

        tellSuccess(sender, Lang.get(_SUCCESS, arena.getName()));
    }
}

