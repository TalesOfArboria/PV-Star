/*
 * This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.jcwhatever.pvs.arenas.settings;

import com.jcwhatever.pvs.api.arena.Arena;
import com.jcwhatever.pvs.api.arena.options.LivesBehavior;
import com.jcwhatever.pvs.api.arena.options.PointsBehavior;
import com.jcwhatever.pvs.api.arena.settings.GameManagerSettings;

/**
 * Game manager settings implementation.
 */
public class PVGameSettings extends AbstractPlayerSettings implements GameManagerSettings {

    private int _lives = 1;
    private int _points = 0;
    private boolean _postGameCleanup = true;
    private LivesBehavior _livesBehavior = LivesBehavior.ADDITIVE;
    private PointsBehavior _pointsBehavior = PointsBehavior.STATIC;

    /*
     * Constructor.
     */
    public PVGameSettings(Arena arena) {
        super(arena, "game");

        _lives = getDataNode().getInteger("lives", _lives);
        _points = getDataNode().getInteger("points", _points);
        _postGameCleanup = getDataNode().getBoolean("post-game-cleanup", _postGameCleanup);
        _livesBehavior = getDataNode().getEnum("lives-behavior", _livesBehavior, LivesBehavior.class);
        _pointsBehavior = getDataNode().getEnum("points-behavior", _pointsBehavior, PointsBehavior.class);
    }

    @Override
    public int getStartLives() {
        return _lives;
    }

    @Override
    public void setStartLives(int lives) {
        _lives = lives;

        save("lives", lives);
    }

    @Override
    public int getStartPoints() {
        return _points;
    }

    @Override
    public void setStartPoints(int points) {
        _points = points;

        save("points", points);
    }

    @Override
    public LivesBehavior getLivesBehavior() {
        return _livesBehavior;
    }

    @Override
    public void setLivesBehavior(LivesBehavior behavior) {
        _livesBehavior = behavior;

        save("lives-behavior", behavior);
    }

    @Override
    public PointsBehavior getPointsBehavior() {
        return _pointsBehavior;
    }

    @Override
    public void setPointsBehavior(PointsBehavior behavior) {
        _pointsBehavior = behavior;

        save("points-behavior", behavior);
    }

    @Override
    public void setPostGameEntityCleanup(boolean isEnabled) {
        _postGameCleanup = isEnabled;

        save("post-game-cleanup", isEnabled);
    }
}
