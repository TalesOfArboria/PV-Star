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


package com.jcwhatever.bukkit.pvs.commands.admin.kits.items;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.inventory.Kit;
import com.jcwhatever.bukkit.generic.items.ItemStackHelper;
import com.jcwhatever.bukkit.generic.items.ItemStackSerializer.SerializerOutputType;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.utils.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

@ICommandInfo(
        parent="items",
        command="list",
        staticParams={ "kitName", "page=1" },
        usage="/{plugin-command} {command} items list <kitName> [page]",
        description="List items in a player kit.")

public class ListSubCommand extends AbstractCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Kit Items";
    @Localizable static final String _KIT_NOT_FOUND = "A kit named '{0}' was not found.";
    @Localizable static final String _LABEL_ARMOR = "ARMOR";
    @Localizable static final String _LABEL_ITEMS = "ITEMS";
    @Localizable static final String _LABEL_NONE = "<none>";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        String kitName = args.getName("kitName");
        int	page = args.getInt("page");

        Kit kit = PVStarAPI.getKitManager().getKitByName(kitName);
        if (kit == null) {
            tellError(sender, Lang.get(_KIT_NOT_FOUND, kitName));
            return; // finish
        }

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        // Armor
        pagin.addFormatted(FormatTemplate.SUB_HEADER, Lang.get(_LABEL_ARMOR));
        ItemStack[] armor = kit.getArmor();

        if (armor.length == 0) {
            pagin.addFormatted(FormatTemplate.ITEM, Lang.get(_LABEL_NONE));
        }
        else {

            for (ItemStack item : armor) {
                pagin.add(ItemStackHelper.serializeToString(item, SerializerOutputType.COLOR));
            }
        }

        // Items
        pagin.addFormatted(FormatTemplate.SUB_HEADER, Lang.get(_LABEL_ITEMS));
        ItemStack[] items = kit.getItems();

        if (items.length == 0) {
            pagin.addFormatted(FormatTemplate.ITEM, Lang.get(_LABEL_NONE));
        }
        else {

            for (ItemStack item : items) {
                pagin.add(ItemStackHelper.serializeToString(item, SerializerOutputType.COLOR));
            }
        }

        pagin.show(sender, page, FormatTemplate.RAW);
    }
}
