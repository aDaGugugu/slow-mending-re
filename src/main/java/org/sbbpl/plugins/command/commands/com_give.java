package org.sbbpl.plugins.command.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.sbbpl.plugins.ExpansionCard.CreateCard;

public final class com_give {
    private com_give() {}

    public static void give(Player player, int frequency, boolean isSet, int quantity) {
        if (quantity < 1 || quantity > 2304) throw new IllegalArgumentException("数量必须在 1 到 2304 之间。");
        ItemStack template = CreateCard.createCard(isSet, frequency);
        int stackSize = Math.min(template.getMaxStackSize(), player.getInventory().getMaxStackSize());
        int capacity = 0;
        for (ItemStack slot : player.getInventory().getStorageContents()) {
            if (slot == null || slot.getType().isAir()) capacity += stackSize;
            else if (slot.isSimilar(template)) capacity += Math.max(0, stackSize - slot.getAmount());
        }
        // Validate the whole delivery before changing inventory; never overwrite a held item.
        if (capacity < quantity) throw new IllegalArgumentException("目标玩家背包空间不足，未发放拓展卡。");
        int remaining = quantity;
        while (remaining > 0) {
            int amount = Math.min(stackSize, remaining);
            ItemStack stack = template.clone();
            stack.setAmount(amount);
            player.getInventory().addItem(stack);
            remaining -= amount;
        }
    }
}
