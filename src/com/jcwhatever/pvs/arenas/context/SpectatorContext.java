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


package com.jcwhatever.pvs.arenas.context;

import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.context.ISpectatorContext;
import com.jcwhatever.pvs.api.arena.options.AddToContextReason;
import com.jcwhatever.pvs.api.arena.options.ArenaContext;
import com.jcwhatever.pvs.api.arena.options.RemoveFromContextReason;
import com.jcwhatever.pvs.api.arena.settings.ISpectatorSettings;
import com.jcwhatever.pvs.api.events.players.PlayerAddedToContextEvent;
import com.jcwhatever.pvs.api.events.players.PlayerAddedToSpectatorEvent;
import com.jcwhatever.pvs.api.events.players.PlayerRemovedFromSpectatorEvent;
import com.jcwhatever.pvs.arenas.AbstractArena;
import com.jcwhatever.pvs.arenas.managers.SpawnManager;
import com.jcwhatever.pvs.arenas.settings.PVSpectatorSettings;

import org.bukkit.Location;

import javax.annotation.Nullable;

/**
 * Spectator manager implementation
 */
public class SpectatorContext extends AbstractContextManager implements ISpectatorContext {

    private final ISpectatorSettings _settings;

    /*
     * Constructor.
     */
    public SpectatorContext(AbstractArena arena) {
        super(arena);

        _settings = new PVSpectatorSettings(arena);
    }

    @Override
    public ArenaContext getContext() {
        return ArenaContext.SPECTATOR;
    }

    @Override
    public ISpectatorSettings getSettings() {
        return _settings;
    }

    @Override
    protected Location onPrePlayerAdd(IArenaPlayer player, AddToContextReason reason) {

        return SpawnManager.getRespawnLocation(
                this, ArenaContext.SPECTATOR, new Location(null, 0, 0, 0));
    }

    @Nullable
    @Override
    protected PlayerAddedToContextEvent onPlayerAdded(
            IArenaPlayer player, AddToContextReason reason, PlayerAddedToContextEvent contextEvent) {

        PlayerAddedToSpectatorEvent event = new PlayerAddedToSpectatorEvent(contextEvent);

        getArena().getEventManager().call(this, event);

        return event;
    }

    @Override
    protected void onPreRemovePlayer(IArenaPlayer player, RemoveFromContextReason reason) {
        // do nothing
    }

    @Override
    protected Location onRemovePlayer(IArenaPlayer player, RemoveFromContextReason reason) {

        IArena arena = getArena();

        PlayerRemovedFromSpectatorEvent event = new PlayerRemovedFromSpectatorEvent(
                arena, player, this,  getContext(), reason);

        arena.getEventManager().call(this, event);

        return getArena().getSettings().getRemoveLocation();
    }
}
