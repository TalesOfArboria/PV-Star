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

import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.options.LivesBehavior;
import com.jcwhatever.pvs.api.arena.options.PointsBehavior;
import com.jcwhatever.pvs.api.arena.settings.IGameSettings;

/**
 * Game manager settings implementation.
 */
public class PVGameSettings extends AbstractPlayerSettings implements IGameSettings {

    private int _lives = 1;
    private int _points = 0;
    private int _endDelayTicks = 0;
    private boolean _postGameCleanup = true;
    private LivesBehavior _livesBehavior = LivesBehavior.ADDITIVE;
    private PointsBehavior _pointsBehavior = PointsBehavior.STATIC;

    /*
     * Constructor.
     */
    public PVGameSettings(IArena arena) {
        super(arena, "game");

        _lives = getDataNode().getInteger("lives", _lives);
        _points = getDataNode().getInteger("points", _points);
        _endDelayTicks = getDataNode().getInteger("end-delay-ticks", _endDelayTicks);
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
        save("lives", _lives = lives);
    }

    @Override
    public int getStartPoints() {
        return _points;
    }

    @Override
    public void setStartPoints(int points) {
        save("points", _points = points);
    }

    @Override
    public LivesBehavior getLivesBehavior() {
        return _livesBehavior;
    }

    @Override
    public void setLivesBehavior(LivesBehavior behavior) {
        save("lives-behavior", _livesBehavior = behavior);
    }

    @Override
    public PointsBehavior getPointsBehavior() {
        return _pointsBehavior;
    }

    @Override
    public void setPointsBehavior(PointsBehavior behavior) {
        save("points-behavior", _pointsBehavior = behavior);
    }

    @Override
    public void setPostGameEntityCleanup(boolean isEnabled) {
        save("post-game-cleanup", _postGameCleanup = isEnabled);
    }

    @Override
    public int getEndDelayTicks() {
        return _endDelayTicks;
    }

    @Override
    public void setEndDelayTicks(int delayTicks) {
        save("end-delay-ticks", _endDelayTicks = delayTicks);
    }
}
