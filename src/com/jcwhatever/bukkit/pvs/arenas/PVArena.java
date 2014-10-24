package com.jcwhatever.bukkit.pvs.arenas;

@ArenaTypeInfo(
        typeName="arena",
        description="A basic arena.")
public class PVArena extends AbstractArena {

    @Override
    protected boolean onCanJoin() {
        return true;
    }
}
