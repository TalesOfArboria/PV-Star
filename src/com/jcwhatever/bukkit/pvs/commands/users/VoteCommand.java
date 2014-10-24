package com.jcwhatever.bukkit.pvs.commands.users;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.options.ArenaPlayerRelation;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

@ICommandInfo(
        command={"vote", "v"},
        usage="/{plugin-command} {command}",
        description="Vote to start the arena you're in.",
        permissionDefault= PermissionDefault.TRUE)

public class VoteCommand extends AbstractPVCommand {

    @Localizable static final String _VOTE_NOT_IN_GAME = "You're not in a game.";
    @Localizable static final String _VOTE_GAME_ALREADY_STARTED = "The game has already started.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidCommandSenderException {

        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER);

        Player p = (Player)sender;

        ArenaPlayer player = PVArenaPlayer.get(p);
        Arena arena = player.getArena();

        if (arena == null || player.getArenaRelation() == ArenaPlayerRelation.SPECTATOR) {
            tellError(p, Lang.get(_VOTE_NOT_IN_GAME));
            return; // finish
        }

        if (player.getArenaRelation() == ArenaPlayerRelation.GAME) {
            tellError(p, Lang.get(_VOTE_GAME_ALREADY_STARTED));
            return; // finish
        }

        player.setReady(true);
    }
}
