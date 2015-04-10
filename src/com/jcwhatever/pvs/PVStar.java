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


package com.jcwhatever.pvs;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.NucleusPlugin;
import com.jcwhatever.nucleus.events.manager.BukkitEventForwarder;
import com.jcwhatever.nucleus.events.manager.EventManager;
import com.jcwhatever.nucleus.managed.scripting.IEvaluatedScript;
import com.jcwhatever.nucleus.managed.scripting.IScriptApi;
import com.jcwhatever.nucleus.managed.scripting.SimpleScriptApi;
import com.jcwhatever.nucleus.managed.scripting.SimpleScriptApi.IApiObjectCreator;
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.providers.permissions.Permissions;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import com.jcwhatever.pvs.api.IPVStar;
import com.jcwhatever.pvs.api.PVStarAPI;
import com.jcwhatever.pvs.api.arena.IArena;
import com.jcwhatever.pvs.api.arena.IArenaPlayer;
import com.jcwhatever.pvs.api.arena.extensions.IExtensionTypeManager;
import com.jcwhatever.pvs.api.arena.managers.IArenaManager;
import com.jcwhatever.pvs.api.commands.ICommandHelper;
import com.jcwhatever.pvs.api.events.PVStarLoadedEvent;
import com.jcwhatever.pvs.api.modules.IModuleInfo;
import com.jcwhatever.pvs.api.modules.PVStarModule;
import com.jcwhatever.pvs.api.points.IPointsManager;
import com.jcwhatever.pvs.api.spawns.ISpawnTypeManager;
import com.jcwhatever.pvs.api.stats.IStatsManager;
import com.jcwhatever.pvs.api.utils.Msg;
import com.jcwhatever.pvs.arenas.Arena;
import com.jcwhatever.pvs.commands.PVCommandHelper;
import com.jcwhatever.pvs.commands.admin.arena.ArenaCommand;
import com.jcwhatever.pvs.commands.admin.ext.ExtCommand;
import com.jcwhatever.pvs.commands.admin.game.GameCommand;
import com.jcwhatever.pvs.commands.admin.lobby.LobbyCommand;
import com.jcwhatever.pvs.commands.admin.modules.ModulesCommand;
import com.jcwhatever.pvs.commands.admin.points.PointsCommand;
import com.jcwhatever.pvs.commands.admin.spawns.SpawnsCommand;
import com.jcwhatever.pvs.commands.admin.spectator.SpectatorCommand;
import com.jcwhatever.pvs.commands.users.JoinCommand;
import com.jcwhatever.pvs.commands.users.LeaveCommand;
import com.jcwhatever.pvs.commands.users.ListCommand;
import com.jcwhatever.pvs.commands.users.VoteCommand;
import com.jcwhatever.pvs.listeners.ArenaEventForwarder;
import com.jcwhatever.pvs.listeners.MobEventListener;
import com.jcwhatever.pvs.listeners.PlayerEventListener;
import com.jcwhatever.pvs.listeners.PvpListener;
import com.jcwhatever.pvs.listeners.SharingListener;
import com.jcwhatever.pvs.listeners.WorldEventListener;
import com.jcwhatever.pvs.modules.ModuleLoader;
import com.jcwhatever.pvs.points.PointsManager;
import com.jcwhatever.pvs.scripting.PVStarScriptApi;
import com.jcwhatever.pvs.signs.ClassSignHandler;
import com.jcwhatever.pvs.signs.PveSignHandler;
import com.jcwhatever.pvs.signs.PvpSignHandler;
import com.jcwhatever.pvs.signs.ReadySignHandler;
import com.jcwhatever.pvs.stats.StatsManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/**
 * PV-Star plugin implementation.
 */
public class PVStar extends NucleusPlugin implements IPVStar {

    private ModuleLoader _moduleLoader;
    private ArenaManager _arenaManager;
    private EventManager _eventManager;
    private IStatsManager _statsManager;
    private IPointsManager _pointsManager;
    private ExtensionTypeManager _extensionManager;
    private SpawnTypeManager _spawnTypeManager;
    private PVCommandHelper _commandHelper;
    private ArenaEventForwarder _eventForwarder;
    private IScriptApi _scriptApi;
    private boolean _isLoaded;

    @Override
    public boolean isLoaded() {
        return _isLoaded;
    }

    @Override
    public String getChatPrefix() {
        return ChatColor.AQUA + "[PV*] " + ChatColor.WHITE;
    }

    @Override
    public String getConsolePrefix() {
        return "[PV*] ";
    }

    @Override
    public IArenaPlayer getArenaPlayer(Object player) {
        PreCon.notNull(player);

        if (player instanceof IArenaPlayer)
            return (IArenaPlayer) player;

        Player p = PlayerUtils.getPlayer(player);
        PreCon.notNull(p);

        return ArenaPlayer.get(p);
    }

    @Override
    public IArenaManager getArenaManager() {
        return _arenaManager;
    }

    @Override
    public ISpawnTypeManager getSpawnTypeManager() {
        return _spawnTypeManager;
    }

    @Override
    public IStatsManager getStatsManager() {
        return _statsManager;
    }

    @Override
    public IExtensionTypeManager getExtensionManager() {
        return _extensionManager;
    }

    @Override
    public IPointsManager getPointsManager() {
        return _pointsManager;
    }

    @Override
    public ICommandHelper getCommandHelper() {
        return _commandHelper;
    }

    @Override
    public EventManager getEventManager() {
        return _eventManager;
    }

    @Override
    @Nullable
    public PVStarModule getModule(String name) {
        PreCon.notNullOrEmpty(name);

        return _moduleLoader.getModule(name);
    }

    @Override
    public List<PVStarModule> getModules() {
        return _moduleLoader.getModules();
    }

    @Override
    @Nullable
    public IModuleInfo getModuleInfo(PVStarModule module) {
        return _moduleLoader.getModuleInfo(module);
    }

    @Override
    protected void onEnablePlugin() {
        PVStarAPI.setImplementation(this);

        _pointsManager = new PointsManager();
        _statsManager = new StatsManager();
        _eventManager = new EventManager(this);
        _extensionManager = new ExtensionTypeManager();
        _spawnTypeManager = new SpawnTypeManager();
        _commandHelper = new PVCommandHelper();

        Nucleus.getSignManager().registerHandler(new ClassSignHandler());
        Nucleus.getSignManager().registerHandler(new PveSignHandler());
        Nucleus.getSignManager().registerHandler(new PvpSignHandler());
        Nucleus.getSignManager().registerHandler(new ReadySignHandler());

        // admin commands
        registerCommand(ArenaCommand.class);
        registerCommand(ExtCommand.class);
        registerCommand(GameCommand.class);
        registerCommand(LobbyCommand.class);
        registerCommand(ModulesCommand.class);
        registerCommand(PointsCommand.class);
        registerCommand(SpawnsCommand.class);
        registerCommand(SpectatorCommand.class);

        // user commands
        registerCommand(JoinCommand.class);
        registerCommand(LeaveCommand.class);
        registerCommand(ListCommand.class);
        registerCommand(VoteCommand.class);

        Msg.info("Loading modules...");

        // load modules
        _moduleLoader = new ModuleLoader(this);
        _moduleLoader.loadModules();

        // enable loaded modules
        _moduleLoader.enable(new Runnable() {

            // called after modules are finished loading
            @Override
            public void run() {

                // load arenas
                _arenaManager = new ArenaManager(getDataNode().getNode("arenas"));

                // register built in arenas
                _arenaManager.registerType(Arena.class);

                // load arenas, permissions batch for performance
                Permissions.runBatchOperation(new Runnable() {

                    @Override
                    public void run() {
                        _arenaManager.loadArenas();
                    }
                });

                // register event listeners
                registerEventListeners(
                        new MobEventListener(),
                        new PlayerEventListener(),
                        new PvpListener(),
                        new SharingListener(),
                        new WorldEventListener());

                // forward global Bukkit events to the appropriate
                // arena event manager.
                _eventForwarder = new ArenaEventForwarder();
                BukkitEventForwarder.registerBukkitEvents(_eventForwarder);

                _scriptApi = new SimpleScriptApi(PVStar.this, "pvstar", new IApiObjectCreator() {
                    @Override
                    public IDisposable create(Plugin plugin, IEvaluatedScript script) {
                        return new PVStarScriptApi();
                    }
                });

                // register script api
                Nucleus.getScriptApiRepo().registerApi(_scriptApi);

                Msg.info("Modules loaded.");
                _isLoaded = true;

                _eventManager.call(this, new PVStarLoadedEvent());
            }
        });
    }

    @Override
    protected void onDisablePlugin() {

        _isLoaded = false;

        // end arenas
        List<IArena> arenas = _arenaManager.getArenas();
        for (IArena arena : arenas) {
            arena.getGame().end();
        }

        Collection<PVStarModule> modules = _moduleLoader.getModules();

        for (PVStarModule module : modules) {
            ModuleLoader.REGISTRATION.dispose(module);
        }

        if (_eventForwarder != null) {
            _eventForwarder.dispose();
            _eventForwarder = null;
        }

        Nucleus.getScriptApiRepo().unregisterApi(_scriptApi);
    }
}
