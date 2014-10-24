package com.jcwhatever.bukkit.pvs.spawns;

import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.spawns.SpawnType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.util.List;

public class GameSpawnType extends SpawnType {

    @Override
    public String getName() {
        return "Game";
    }

    @Override
    public String getDescription() {
        return "Represents a spawn used to identify game spawn locations.";
    }

    @Override
    public boolean isSpawner() {
        return false;
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public boolean isHostile() {
        return false;
    }

    @Nullable
    @Override
    public EntityType[] getEntityTypes() {
        return new EntityType[0];
    }

    @Nullable
    @Override
    public List<Entity> spawn(Arena arena, Location location, int count) {
        return null;
    }
}
