package org.sbbpl.plugins.ExpansionCard;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.sbbpl.plugins.MendCount;
import org.sbbpl.plugins.MendingItem;
import org.sbbpl.plugins.Slow_mending_re;

import java.util.List;

public class ExpansionCard {
    public static final NamespacedKey MODE = MendingItem.key("card_mode");
    public static final NamespacedKey FREQUENCY = MendingItem.key("card_frequency");
    public static final String identifier = "\u0005\u0002\u0000";
    private static boolean enabled, allowBeyond, allowSpecial, acceptLegacy;
    static String name, setModeText, addModeText, frequencyText;
    static List<String> usage = List.of();
    private boolean valid;
    private boolean setMode;
    private int frequency;

    public static void configure(boolean enable, boolean beyond, boolean special, boolean legacy,
                                 String cardName, String setText, String addText, String frequencyName,
                                 List<String> instructions) {
        enabled = enable;
        allowBeyond = beyond;
        allowSpecial = special;
        acceptLegacy = legacy;
        name = cardName;
        setModeText = setText;
        addModeText = addText;
        frequencyText = frequencyName;
        usage = List.copyOf(instructions);
    }

    public static boolean isEnable() { return enabled; }
    public static boolean isAllowBeyond() { return allowBeyond; }
    public static boolean isAllowSetSP() { return allowSpecial; }
    public boolean isCard() { return valid; }

    public ExpansionCard(ItemMeta meta) {
        if (meta == null) return;
        var data = meta.getPersistentDataContainer();
        if (data.has(MODE) || data.has(FREQUENCY)) {
            if (!data.has(MODE, PersistentDataType.BYTE) || !data.has(FREQUENCY, PersistentDataType.INTEGER)) return;
            Byte mode = data.get(MODE, PersistentDataType.BYTE);
            Integer value = data.get(FREQUENCY, PersistentDataType.INTEGER);
            if (mode == null || value == null || (mode != 0 && mode != 1)) return;
            setMode = mode == 1;
            frequency = value;
            valid = !setMode || value >= -3;
            return;
        }
        if (!acceptLegacy || !meta.hasLore()) return;
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty() || !identifier.equals(lore.get(0))) return;
        int modes = 0, values = 0;
        try {
            for (String line : lore) {
                if (line.equals(setModeText) || line.equals(addModeText)) {
                    modes++;
                    setMode = line.equals(setModeText);
                }
                if (line.startsWith(frequencyText)) {
                    values++;
                    frequency = Integer.parseInt(line.substring(frequencyText.length()).trim());
                }
            }
            valid = modes == 1 && values == 1 && (!setMode || frequency >= -3);
        } catch (NumberFormatException ignored) {
            valid = false;
        }
    }

    public boolean use(Player player) {
        if (!enabled || !valid) return false;
        ItemStack card = player.getInventory().getItemInMainHand();
        ExpansionCard held = new ExpansionCard(card.getItemMeta());
        if (!held.valid || held.setMode != setMode || held.frequency != frequency) return false;
        ItemStack target = player.getInventory().getItemInOffHand();
        ItemMeta meta = target.getItemMeta();
        if (!(meta instanceof Damageable) || !meta.hasEnchant(Enchantment.MENDING)) {
            player.sendMessage("§c请将带有经验修补附魔的装备放在副手！");
            return false;
        }
        String prefix = Slow_mending_re.getMend_Frequency_Lore_Name();
        List<String> oldNames = Slow_mending_re.getOld_Mend_Frequency_Lore_Name();
        try {
            int result = frequency;
            if (!setMode) {
                int current;
                try {
                    current = MendingItem.getRemainderMendFrequency(meta, prefix, oldNames);
                } catch (NoSuchFieldException e) {
                    current = Slow_mending_re.getMax_Mend_Limit_Number();
                }
                result = MendCount.add(current, frequency);
                if (result == current) {
                    player.sendMessage("§e次数没有变化，未消耗拓展卡。");
                    return false;
                }
            }
            MendCount.validate(result);
            if (result < 0 && !allowSpecial) throw new IllegalArgumentException("当前不允许通过拓展卡设置无限次数或禁用修补。");
            int maximum = Slow_mending_re.getMax_Mend_Limit_Number();
            if (!allowBeyond && maximum >= 0 && result > maximum) {
                throw new IllegalArgumentException("次数不能超出配置上限。");
            }
            if (setMode) {
                try {
                    if (MendingItem.getRemainderMendFrequency(meta, prefix, oldNames) == result) {
                        player.sendMessage("§e次数没有变化，未消耗拓展卡。");
                        return false;
                    }
                } catch (NoSuchFieldException ignored) { /* New item. */ }
            }
            MendingItem.setRemainderMendFrequency(meta, prefix, result, oldNames);
            target.setItemMeta(meta);
            player.getInventory().setItemInOffHand(target);
            card.setAmount(card.getAmount() - 1);
            player.getInventory().setItemInMainHand(card);
            player.sendMessage("§b剩余修补次数：" + MendCount.format(result));
            return true;
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c" + e.getMessage());
            return false;
        }
    }
}
