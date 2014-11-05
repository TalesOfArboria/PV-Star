/* This file is part of ${MODULE_NAME}, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.pvs.commands;

import com.jcwhatever.bukkit.generic.commands.AbstractCommandUtils;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtensionInfo;
import com.jcwhatever.bukkit.pvs.api.arena.options.NameMatchMode;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand.ArenaReturned;
import com.jcwhatever.bukkit.pvs.api.commands.CommandHelper;
import com.jcwhatever.bukkit.pvs.api.exceptions.MissingExtensionAnnotationException;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public class PVCommandHelper extends AbstractCommandUtils implements CommandHelper {

    @Localizable static final String _ARENA_NOT_SELECTED = "No arena selected. Use '/{plugin-command} arena select <arenaName>' first.";
    @Localizable static final String _WAIT_TILL_FINISHED = "Please wait until the arena is finished.";
    @Localizable static final String _ARENA_NOT_EXISTS = "Specified arena '{0}' doesn't exist. Type '/{plugin-command} list' for a list.";
    @Localizable static final String _MULTIPLE_ARENAS = "Multiple arenas found for '{0}'. Please be more specific:";
    @Localizable static final String _EXTENSION_NOT_FOUND = "{0} is not installed in arena '{1}'.";

    /**
     * Constructor.
     *
     */
    public PVCommandHelper() {
        super(PVStarAPI.getPlugin());
    }

    /**
     * Get the command senders currently selected arena.
     *
     * @param sender    The command sender to check and display error messages to.
     * @param returned  Specify return conditions.
     */
    @Nullable
    @Override
    public Arena getSelectedArena(CommandSender sender, ArenaReturned returned) {
        Arena arena;

        if ((arena = PVStarAPI.getArenaManager().getSelectedArena(sender)) == null) {
            tellError(sender, Lang.get(_ARENA_NOT_SELECTED));
            return null;
        }

        if (returned == ArenaReturned.NOT_RUNNNING && arena.getGameManager().isRunning()) {
            tellError(sender, Lang.get(_WAIT_TILL_FINISHED));
            return null;
        }

        return arena;
    }

    /**
     * Get an extension instance from an arena.
     *
     * @param sender  The command sender to display error messages to.
     * @param arena   The arena to get the extension instance from.
     * @param clazz   The extension type.
     * @param <T>     The extension type.
     */
    @Override
    @Nullable
    public <T extends ArenaExtension> T getExtension(CommandSender sender, Arena arena, Class<T> clazz) {

        T extension = arena.getExtensionManager().get(clazz);
        if (extension == null) {

            ArenaExtensionInfo info = clazz.getAnnotation(ArenaExtensionInfo.class);
            if (info == null)
                throw new MissingExtensionAnnotationException(clazz);

            tellError(sender, Lang.get(_EXTENSION_NOT_FOUND, info.name(), arena.getName()));
            return null; // finish
        }

        return extension;
    }

    /**
     * Get an arena by name.
     *
     * @param sender     The command sender to display error messages to.
     * @param arenaName  The name or partial name of the arena to find.
     */
    @Override
    @Nullable
    public Arena getArena(CommandSender sender, String arenaName) {
        PreCon.notNull(sender);
        PreCon.notNullOrEmpty(arenaName);

        List<Arena> results = PVStarAPI.getArenaManager().getArena(arenaName, NameMatchMode.CASE_INSENSITIVE);

        if (results.size() == 0) {
            results = PVStarAPI.getArenaManager().getArena(arenaName, NameMatchMode.BEGINS_WITH);
        }

        if (results.size() == 0) {
            tellError(sender, Lang.get(_ARENA_NOT_EXISTS, arenaName));
        }
        else if (results.size() == 1) {
            return results.get(0);
        }
        else {
            tellError(sender, Lang.get(_MULTIPLE_ARENAS, "{YELLOW}" + arenaName));
            tell(sender, TextUtils.concat(results, ", "));
        }

        return null;
    }

    /**
     * Get an arena by exact name. (non-case sensitive)
     *
     * @param arenaName  The name of the arena.
     */
    @Override
    @Nullable
    public Arena getArena(String arenaName) {
        PreCon.notNullOrEmpty(arenaName);

        List<Arena> results = PVStarAPI.getArenaManager().getArena(arenaName, NameMatchMode.CASE_INSENSITIVE);

        if (results.size() == 1) {
            return results.get(0);
        }

        return null;
    }


    /**
     * Get a list of arena ids from a comma delimited string of arena names.
     *
     * @param sender      The command sender to display error messages to.
     * @param arenaNames  The names of the arenas.
     *
     * @return  Null if any arena in the list could not be found.
     */
    @Override
    @Nullable
    public List<UUID> getArenaIds(CommandSender sender, String arenaNames) {
        PreCon.notNullOrEmpty(arenaNames);

        String[] arenaNamesArray = TextUtils.PATTERN_COMMA.split(arenaNames);
        List<UUID> result = new ArrayList<>(arenaNamesArray.length);

        for (String arenaName : arenaNamesArray) {

            Arena arena = getArena(sender, arenaName);
            if (arena == null)
                return null;

            result.add(arena.getId());
        }
        return result;
    }
}
