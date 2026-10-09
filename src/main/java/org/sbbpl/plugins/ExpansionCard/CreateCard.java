package org.sbbpl.plugins.ExpansionCard;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.sbbpl.plugins.MendCount;

import java.util.ArrayList;
import java.util.Objects;

public final class CreateCard {
    private CreateCard() {}

    public static ItemStack createCard(boolean isSet, int num) {
        if (isSet) MendCount.validate(num);
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) Objects.requireNonNull(item.getItemMeta());
        meta.setTitle("经验修补拓展卡");
        meta.setAuthor("Slow Mending Re");
        meta.setPages("主手持卡，副手持有带经验修补的装备，右键使用。");
        meta.setDisplayName(ExpansionCard.name);
        var lore = new ArrayList<>(ExpansionCard.usage);
        lore.add(isSet ? ExpansionCard.setModeText : ExpansionCard.addModeText);
        lore.add(ExpansionCard.frequencyText + (isSet ? MendCount.format(num) : MendCount.formatDelta(num)));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(ExpansionCard.MODE, PersistentDataType.BYTE, (byte) (isSet ? 1 : 0));
        meta.getPersistentDataContainer().set(ExpansionCard.FREQUENCY, PersistentDataType.INTEGER, num);
        item.setItemMeta(meta);
        return item;
    }
}
