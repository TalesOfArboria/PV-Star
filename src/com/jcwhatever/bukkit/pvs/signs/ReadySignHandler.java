package com.jcwhatever.bukkit.pvs.signs;

import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.signs.SignContainer;
import com.jcwhatever.bukkit.generic.signs.SignHandler;
import com.jcwhatever.bukkit.generic.utils.TextUtils.TextColor;
import com.jcwhatever.bukkit.pvs.PVArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.options.ArenaPlayerRelation;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ReadySignHandler extends SignHandler {

    @Localizable static final String _VOTE_NOT_IN_GAME = "You're not in a game.";
    @Localizable static final String _VOTE_GAME_ALREADY_STARTED = "The game has already started.";

    @Override
    public Plugin getPlugin() {
        return PVStarAPI.getPlugin();
    }

    @Override
    public String getName() {
        return "Ready";
    }

    @Override
    public String getDescription() {
        return "A sign that votes to start when a player clicks on it.";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "Ready",
                "--anything--",
                "--anything--",
                "--anything--"
        };
    }

    @Override
    public String getHeaderPrefix() {
        return TextColor.BOLD.toString() + TextColor.DARK_GREEN;
    }

    @Override
    protected void onSignLoad(SignContainer sign) {
        // do nothing
    }

    @Override
    protected boolean onSignChange(Player p, SignContainer sign) {

        Arena arena = PVStarAPI.getArenaManager().getArena(sign.getLocation());
        return arena != null;
    }

    @Override
    protected boolean onSignClick(Player p, SignContainer sign) {

        ArenaPlayer player = PVArenaPlayer.get(p);
        Arena arena = player.getArena();

        if (arena == null || player.getArenaRelation() == ArenaPlayerRelation.SPECTATOR) {
            Msg.tellError(p, Lang.get(_VOTE_NOT_IN_GAME));
            return false; // finish
        }

        if (player.getArenaRelation() == ArenaPlayerRelation.GAME) {
            Msg.tellError(p, Lang.get(_VOTE_GAME_ALREADY_STARTED));
            return false; // finish
        }

        player.setReady(true);

        return true;
    }

    @Override
    protected boolean onSignBreak(Player p, SignContainer sign) {
        // allow
        return true;
    }
}
