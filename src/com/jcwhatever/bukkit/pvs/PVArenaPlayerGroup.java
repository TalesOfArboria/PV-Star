package com.jcwhatever.bukkit.pvs;

import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayerGroup;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PVArenaPlayerGroup implements ArenaPlayerGroup {

    private Set<ArenaPlayer> _players = new HashSet<>(20);

    /*
     * Determine if the group has the specified player.
     */
    @Override
    public boolean hasPlayer(ArenaPlayer player) {
        PreCon.notNull(player);

        return _players.contains(player);
    }

    /*
     * Get the number of players in the group.
     */
    @Override
    public int size() {
        return _players.size();
    }

    /*
     * Get the number of players in the group who are in the specified arena.
     */
    @Override
    public int size(Arena arena) {
        return getPlayers(arena).size();
    }

    /*
     * Determine if players in the provided collection are ready.
     *
     * Players that are not in the group are ignored.
     *
     * If any player that is in the group is not ready, false is returned.
     */
    @Override
    public boolean isReady(Collection<ArenaPlayer> players) {
        PreCon.notNull(players);

        for (ArenaPlayer player : players) {
            if (!_players.contains(player))
                continue;

            if (!player.isReady())
                return false;
        }
        return true;
    }

    /*
     * Filters the collection of players to players that are in the group
     */
    @Override
    public List<ArenaPlayer> filterPlayers(Collection<ArenaPlayer> players) {

        List<ArenaPlayer> result = new ArrayList<>(players.size());

        for (ArenaPlayer player : players) {
            if (!_players.contains(player))
                continue;

            result.add(player);
        }

        return result;
    }

    /*
     * Tell a message to all players in the group.
     */
    @Override
    public void tell(String message, Object... params) {
        PreCon.notNullOrEmpty(message);
        PreCon.notNull(params);

        for (ArenaPlayer player : _players) {
            Msg.tell(player.getHandle(), message, params);
        }
    }

    /*
     * Add a player to the group.
     */
    @Override
    public void addPlayer(ArenaPlayer player) {
        PreCon.isValid(player instanceof PVArenaPlayer);

        _players.add(player);

        // remove the player from their current group, if any
        ArenaPlayerGroup currentGroup = player.getPlayerGroup();
        if (currentGroup != null)
            currentGroup.removePlayer(player);

        player.setPlayerGroup(this);
    }

    /*
     * Remove a player from the group.
     */
    @Override
    public void removePlayer(ArenaPlayer player) {
        PreCon.isValid(player instanceof PVArenaPlayer);

        _players.remove(player);

        // make sure the player is in the group before
        // setting player group to null on player
        ArenaPlayerGroup currentGroup = player.getPlayerGroup();
        if (currentGroup != null && !currentGroup.equals(this))
            return;

        player.setPlayerGroup(null);
    }

    /*
     * Get all players in the group
     */
    @Override
    public List<ArenaPlayer> getPlayers() {
        return new ArrayList<>(_players);
    }

    /*
     * Get players in the group who are in the specified arena.
     */
    @Override
    public List<ArenaPlayer> getPlayers(Arena arena) {
        PreCon.notNull(arena);

        List<ArenaPlayer> result = new ArrayList<ArenaPlayer>(_players.size());

        for (ArenaPlayer player : _players) {
            if (arena.equals(player.getArena())) {
                result.add(player);
            }
        }
        return result;
    }
}
