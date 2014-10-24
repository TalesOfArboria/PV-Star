package com.jcwhatever.bukkit.pvs.signs;

import com.jcwhatever.bukkit.generic.signs.SignManager;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import org.bukkit.plugin.Plugin;

public class PVSignManager extends SignManager {

    public PVSignManager(Plugin plugin, IDataNode dataNode) {
        super(plugin, dataNode);

        registerSignType(new ClassSignHandler());
        registerSignType(new PveSignHandler());
        registerSignType(new PvpSignHandler());
        registerSignType(new ReadySignHandler());
    }
}
