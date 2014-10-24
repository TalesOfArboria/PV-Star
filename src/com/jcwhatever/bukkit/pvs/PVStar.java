package com.jcwhatever.bukkit.pvs;

import com.jcwhatever.bukkit.generic.GenericsPlugin;
import com.jcwhatever.bukkit.generic.commands.AbstractCommandHandler;
import com.jcwhatever.bukkit.generic.events.GenericsEventManager;
import com.jcwhatever.bukkit.generic.inventory.KitManager;
import com.jcwhatever.bukkit.generic.language.LanguageManager;
import com.jcwhatever.bukkit.generic.permissions.Permissions;
import com.jcwhatever.bukkit.generic.player.PlayerHelper;
import com.jcwhatever.bukkit.generic.scripting.ScriptHelper;
import com.jcwhatever.bukkit.generic.signs.SignManager;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.pvs.api.IPVStar;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.ArenaPlayer;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ExtensionTypeManager;
import com.jcwhatever.bukkit.pvs.api.arena.managers.ArenaManager;
import com.jcwhatever.bukkit.pvs.api.modules.ModuleInfo;
import com.jcwhatever.bukkit.pvs.api.modules.PVStarModule;
import com.jcwhatever.bukkit.pvs.api.points.PointsManager;
import com.jcwhatever.bukkit.pvs.api.scripting.ScriptManager;
import com.jcwhatever.bukkit.pvs.api.spawns.SpawnTypeManager;
import com.jcwhatever.bukkit.pvs.api.stats.StatsManager;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import com.jcwhatever.bukkit.pvs.arenas.PVArena;
import com.jcwhatever.bukkit.pvs.commands.CommandHandler;
import com.jcwhatever.bukkit.pvs.events.ArenaProtectListener;
import com.jcwhatever.bukkit.pvs.events.MobEventListener;
import com.jcwhatever.bukkit.pvs.events.PlayerEventListener;
import com.jcwhatever.bukkit.pvs.events.PvpListener;
import com.jcwhatever.bukkit.pvs.events.SharingListener;
import com.jcwhatever.bukkit.pvs.modules.ModuleLoader;
import com.jcwhatever.bukkit.pvs.points.PVPointsManager;
import com.jcwhatever.bukkit.pvs.scripting.ApiEventRegistrationHelper;
import com.jcwhatever.bukkit.pvs.scripting.PVScriptManager;
import com.jcwhatever.bukkit.pvs.signs.PVSignManager;
import com.jcwhatever.bukkit.pvs.stats.PVStatsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * PV-Star plugin implementation.
 */
public class PVStar extends GenericsPlugin implements IPVStar {

    private ModuleLoader _moduleLoader;
    private PVArenaManager _arenaManager;
    private CommandHandler _commandHandler;
    private GenericsEventManager _eventManager;
    private StatsManager _statsManager;
    private PointsManager _pointsManager;
    private SignManager _signManager;
    private KitManager _kitManager;
    private LanguageManager _languageManager;
    private PVExtensionTypeManager _extensionManager;
    private PVScriptManager _scriptManager;
    private PVSpawnTypeManager _spawnTypeManager;

    @Override
    public String getChatPrefix() {
        return ChatColor.AQUA + "[PV*] " + ChatColor.WHITE;
    }

    @Override
    public String getConsolePrefix() {
        return "[PV*] ";
    }

    @Override
    public ArenaPlayer getArenaPlayer(Object player) {
        PreCon.notNull(player);

        if (player instanceof ArenaPlayer)
            return (ArenaPlayer)player;

        Player p = PlayerHelper.getPlayer(player);
        PreCon.notNull(p);

        return PVArenaPlayer.get(p);
    }

    @Override
    public ArenaManager getArenaManager() {
        return _arenaManager;
    }

    @Override
    public SpawnTypeManager getSpawnTypeManager() {
        return _spawnTypeManager;
    }

    @Override
    public StatsManager getStatsManager() {
        return _statsManager;
    }

    @Override
    public ExtensionTypeManager getExtensionManager() {
        return _extensionManager;
    }

    @Override
    public PointsManager getPointsManager() {
        return _pointsManager;
    }

    @Override
    public KitManager getKitManager() {
        return _kitManager;
    }

    @Override
    public SignManager getSignManager() {
        return _signManager;
    }

    @Override
    public LanguageManager getLanguageManager() {
        return _languageManager;
    }

    @Override
    public AbstractCommandHandler getCommandHandler() {
        return _commandHandler;
    }

    @Override
    public GenericsEventManager getEventManager() {
        return _eventManager;
    }

    @Override
    public ScriptManager getScriptManager() {
        return _scriptManager;
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
    public ModuleInfo getModuleInfo(PVStarModule module) {
        return _moduleLoader.getModuleInfo(module);
    }


    @Override
    protected void onEnablePlugin() {
        PVStarAPI.setImplementation(this);

        _languageManager = new LanguageManager();
        _signManager = new PVSignManager(this, getSettings().getNode("signs"));
        _pointsManager = new PVPointsManager();
        _statsManager = new PVStatsManager();
        _eventManager = new GenericsEventManager();
        _kitManager = new KitManager(this, getSettings().getNode("kits"));
        _extensionManager = new PVExtensionTypeManager();
        _spawnTypeManager = new PVSpawnTypeManager();

        // enable command
        _commandHandler = new CommandHandler(this);
        registerCommands(_commandHandler);

        loadScripts();

        Msg.info("Loading modules...");

        // load modules
        _moduleLoader = new ModuleLoader(this);
        _moduleLoader.loadModules();
        _moduleLoader.enable(new Runnable() {

            // called after modules are finished loading
            @Override
            public void run() {

                // load arenas
                _arenaManager = new PVArenaManager(getSettings().getNode("arenas"));

                // register built in arenas
                _arenaManager.registerType(PVArena.class);

                // load arenas, permissions batch for performance
                Permissions.runBatchOperation(true, new Runnable() {

                    @Override
                    public void run() {
                        _arenaManager.loadArenas();
                    }
                });

                // register event listeners
                registerEventListeners(
                        new ArenaProtectListener(),
                        new MobEventListener(),
                        new PlayerEventListener(),
                        new PvpListener(),
                        new SharingListener());

                Msg.info("Modules loaded.");
            }
        });
    }

    @Override
    protected void onDisablePlugin() {

        Collection<PVStarModule> modules = _moduleLoader.getModules();

        for (PVStarModule module : modules) {
            module.dispose();
        }
    }

    private void loadScripts() {

        File scriptFolder = new File(getDataFolder(), "scripts");
        if (!scriptFolder.exists() && !scriptFolder.mkdirs()) {
            return;
        }

        _scriptManager = new PVScriptManager(this, scriptFolder, ScriptHelper.getGlobalEngineManager());

        // register script events
        new ApiEventRegistrationHelper(_scriptManager).register();
    }


}
