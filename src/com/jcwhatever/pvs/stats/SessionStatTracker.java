/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.pvs.stats;

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.stats.ISessionStatTracker;
import com.jcwhatever.pvs.api.stats.StatType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of {@link ISessionStatTracker}.
 */
public class SessionStatTracker implements ISessionStatTracker {

    private final IArenaPlayer _player;
    private final Map<StatType, StatScore> _scores = new HashMap<>(15);

    /**
     * Constructor.
     *
     * @param player  The player the tracker is for.
     */
    public SessionStatTracker(IArenaPlayer player) {
        PreCon.notNull(player);

        _player = player;
    }

    @Nullable
    @Override
    public IArena getArena() {
        return _player.getArena();
    }

    @Override
    public UUID getPlayerId() {
        return _player.getUniqueId();
    }

    @Override
    public IArenaPlayer getArenaPlayer() {
        return _player;
    }

    @Override
    public double getScore(StatType type) {
        PreCon.notNull(type);

        StatScore score = _scores.get(type);
        return score == null ? 0.0D : score.score;
    }

    @Override
    public void increment(StatType type, double amount) {
        PreCon.notNull(type);

        if (Double.compare(amount, 0.0D) == 0)
            return;

        StatScore score = _scores.get(type);
        if (score == null) {
            score = new StatScore(type);
            _scores.put(type, score);
        }

        score.score += amount;
    }

    @Override
    public Collection<StatType> getStatTypes() {
        return getStatTypes(new ArrayList<StatType>(_scores.size()));
    }

    @Override
    public <T extends Collection<StatType>> T getStatTypes(T output) {
        PreCon.notNull(output);

        if (output instanceof ArrayList)
            ((ArrayList) output).ensureCapacity(_scores.size());

        output.addAll(_scores.keySet());
        return output;
    }

    public Collection<StatScore> getScores() {
        return _scores.values();
    }

    public void reset() {
        _scores.clear();
    }

    public static class StatScore {
        public final StatType statType;
        public double score = 0.0D;

        StatScore(StatType type) {
            statType = type;
        }
    }
}
