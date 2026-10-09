package org.sbbpl.plugins;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MendingItem {
    public static final NamespacedKey REMAINING = key("remaining_mends");
    private static final NamespacedKey LORE_PREFIX = key("lore_prefix");
    private static final NamespacedKey ORIGINAL_NAME = key("original_name");
    private static final NamespacedKey BROKEN_NAME = key("broken_name");
    private static final GsonComponentSerializer JSON = GsonComponentSerializer.gson();

    private MendingItem() {}

    public static NamespacedKey key(String name) {
        return Objects.requireNonNull(NamespacedKey.fromString("slow_mending_re:" + name));
    }

    public static int getRemainderMendFrequency(ItemMeta meta, String prefix, List<String> oldNames)
            throws NoSuchFieldException {
        var data = meta.getPersistentDataContainer();
        if (data.has(REMAINING)) {
            if (!data.has(REMAINING, PersistentDataType.INTEGER)) {
                throw new IllegalArgumentException("物品的持久化次数类型无效。");
            }
            Integer value = data.get(REMAINING, PersistentDataType.INTEGER);
            if (value == null) throw new IllegalArgumentException("物品的持久化次数类型无效。");
            return MendCount.validate(value);
        }
        return readLegacyCount(meta.getLore(), prefix, oldNames);
    }

    static int readLegacyCount(List<String> lore, String prefix, List<String> oldNames)
            throws NoSuchFieldException {
        if (lore == null) throw new NoSuchFieldException("物品没有修补次数。");
        for (String line : lore) {
            for (String candidate : prefixes(prefix, oldNames, null)) {
                if (line.startsWith(candidate)) {
                    try {
                        return MendCount.parseSetting(line.substring(candidate.length()));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("物品 Lore 中的修补次数无效。", e);
                    }
                }
            }
        }
        throw new NoSuchFieldException("物品没有修补次数。");
    }

    public static ItemMeta createRemainderMendFrequency(ItemMeta meta, String prefix, int value) {
        return writeCount(meta, prefix, value, Slow_mending_re.getOld_Mend_Frequency_Lore_Name());
    }

    public static ItemMeta setRemainderMendFrequency(ItemMeta meta, String prefix, int value,
                                                    List<String> oldNames) {
        return writeCount(meta, prefix, value, oldNames);
    }

    public static ItemMeta addRemainderMendFrequency(ItemMeta meta, String prefix, int delta,
                                                    List<String> oldNames) throws NoSuchFieldException {
        return writeCount(meta, prefix, MendCount.add(getRemainderMendFrequency(meta, prefix, oldNames), delta), oldNames);
    }

    private static ItemMeta writeCount(ItemMeta meta, String prefix, int value, List<String> oldNames) {
        MendCount.validate(value);
        if (prefix == null || prefix.isBlank()) throw new IllegalArgumentException("次数 Lore 前缀不能为空。");
        var data = meta.getPersistentDataContainer();
        List<String> recognized = prefixes(prefix, oldNames, data.get(LORE_PREFIX, PersistentDataType.STRING));
        // Preserve other plugins' rich lore components, not only their plain text.
        List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
        lore.removeIf(line -> recognized.stream().anyMatch(
                candidate -> LegacyComponentSerializer.legacySection().serialize(line).startsWith(candidate)));
        lore.add(LegacyComponentSerializer.legacySection().deserialize(prefix + MendCount.format(value)));
        meta.lore(lore);
        data.set(REMAINING, PersistentDataType.INTEGER, value);
        data.set(LORE_PREFIX, PersistentDataType.STRING, prefix);
        if (value != 0 && value != -3) restoreName(meta);
        return meta;
    }

    private static List<String> prefixes(String prefix, List<String> oldNames, String stored) {
        List<String> result = new ArrayList<>();
        if (prefix != null && !prefix.isBlank()) result.add(prefix);
        if (oldNames != null) oldNames.stream().filter(s -> s != null && !s.isBlank()).forEach(result::add);
        if (stored != null && !stored.isBlank()) result.add(stored);
        result.sort((a, b) -> Integer.compare(b.length(), a.length()));
        return result;
    }

    public static ItemMeta changeBroken(ItemMeta meta, Player player, boolean changeName,
                                       String prefix, boolean sendMessage, String message) {
        if (sendMessage && message != null && !message.isEmpty()) player.sendMessage(message);
        var data = meta.getPersistentDataContainer();
        if (changeName && !data.has(BROKEN_NAME)) {
            Component original = meta.displayName();
            data.set(ORIGINAL_NAME, PersistentDataType.STRING, original == null ? "" : JSON.serialize(original));
            Component renamed = LegacyComponentSerializer.legacySection().deserialize(prefix)
                    .append(original == null ? Component.text("工具") : original);
            meta.displayName(renamed);
            data.set(BROKEN_NAME, PersistentDataType.STRING, JSON.serialize(renamed));
        }
        return meta;
    }

    private static void restoreName(ItemMeta meta) {
        var data = meta.getPersistentDataContainer();
        String broken = data.get(BROKEN_NAME, PersistentDataType.STRING);
        String original = data.get(ORIGINAL_NAME, PersistentDataType.STRING);
        if (broken == null || original == null) return;
        // Keep any subsequent anvil/plugin rename made by the player.
        if (Objects.equals(meta.displayName(), JSON.deserialize(broken))) {
            meta.displayName(original.isEmpty() ? null : JSON.deserialize(original));
        }
        data.remove(BROKEN_NAME);
        data.remove(ORIGINAL_NAME);
    }
}
