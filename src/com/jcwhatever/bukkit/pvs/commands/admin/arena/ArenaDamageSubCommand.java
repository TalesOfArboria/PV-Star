/* This file is part of PV-Star for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="arena",
        command="arenadamage",
        staticParams={"on|off|info=info"},
        usage="/{plugin-command} {command} arenadamage [on|off]",
        description="Set or view the settings for allowing players to damage the selected arena.")

public class ArenaDamageSubCommand extends AbstractPVCommand {

    @Localizable static final String _DAMAGE_ENABLED = "{WHITE}Players are {RED}allowed{WHITE} from damaging arena '{0}'.";
    @Localizable static final String _DAMAGE_DISABLED = "{WHITE}Players are {GREEN}prevented{WHITE} to damage arena '{0}'.";
    @Localizable static final String _DAMAGE_CHANGED_ENABLED = "Arena damage in arena '{0}' changed to Enabled.";
    @Localizable static final String _DAMAGE_CHANGED_DISABLED = "Arena damage in arena '{0}' changed to {RED}Disabled.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "on|off|info"));
        if (arena == null)
            return; // finish

        if (args.getString("on|off|info").equals("info")) {

            boolean isEnabled = arena.getSettings().isArenaDamageEnabled();

            if (isEnabled) {
                tell(sender, Lang.get(_DAMAGE_ENABLED, arena.getName()));
            }
            else {
                tell(sender, Lang.get(_DAMAGE_DISABLED, arena.getName()));
            }
        }
        else {
            boolean isEnabled = args.getBoolean("on|off|info");

            arena.getSettings().setArenaDamageEnabled(isEnabled);

            if (isEnabled) {
                tellSuccess(sender, Lang.get(_DAMAGE_CHANGED_ENABLED, arena.getName()));
            }
            else {
                tellSuccess(sender, Lang.get(_DAMAGE_CHANGED_DISABLED, arena.getName()));
            }

        }
    }
}
