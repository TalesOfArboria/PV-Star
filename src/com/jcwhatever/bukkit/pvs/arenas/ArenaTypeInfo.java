package com.jcwhatever.bukkit.pvs.arenas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ArenaTypeInfo {

    /**
     * The name of the arena type
     * @return
     */
    public String typeName();

    /**
     * Description of the arena type
     * @return
     */
    public String description();


}
