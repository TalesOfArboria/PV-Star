package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="game",
        command="lives",
        staticParams={"amount=info"},
        usage="/{plugin-command} {command} lives [amount]",
        description="Set or view player lives in the currently selected arena.")

public class LivesSubCommand extends AbstractPVCommand {

    @Localizable static final String _LIVES_INFO = "Player lives in arena '{0}' is set to {1}.";
    @Localizable static final String _LIVES_SET = "Player lives in arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "amount"));
        if (arena == null)
            return; // finish

        if (args.getString("amount").equals("info")) {

            int lives = arena.getGameManager().getSettings().getStartLives();
            tell(sender, Lang.get(_LIVES_INFO, arena.getName(), lives));
        }
        else {

            int lives = args.getInt("amount");

            arena.getGameManager().getSettings().setStartLives(lives);

            tellSuccess(sender, Lang.get(_LIVES_SET, arena.getName(), lives));
        }
    }
}

