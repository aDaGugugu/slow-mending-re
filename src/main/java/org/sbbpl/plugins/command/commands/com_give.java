package org.sbbpl.plugins.command.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.sbbpl.plugins.ExpansionCard.CreateCard;

import java.util.Objects;

public class com_give {
    public static void give(Player player,int num,boolean isSet,int quantity){
        //获取
        ItemStack itemStack = CreateCard.createCard(isSet,num);
        itemStack.setAmount(quantity);
        Objects.requireNonNull(player.getEquipment()).setItemInMainHand(itemStack);
    }
}
