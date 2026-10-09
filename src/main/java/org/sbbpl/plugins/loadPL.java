package org.sbbpl.plugins;

import org.bukkit.configuration.file.YamlConfiguration;
import org.sbbpl.plugins.ExpansionCard.ExpansionCard;

import java.io.File;
import java.util.List;

/** Strict, transactional config loading: a failed reload leaves all live values intact. */
public final class loadPL {
    private loadPL() {}

    public static void loadPlugins() throws Exception {
        Slow_mending_re plugin = Slow_mending_re.getSLM();
        var config = read(new File(plugin.getDataFolder(), "config.yml"));
        var cards = read(new File(plugin.getDataFolder(), "ExpansionCard/cardconfig.yml"));
        var text = read(new File(plugin.getDataFolder(), "ExpansionCard/cardinfo.yml"));
        boolean debug = bool(config, "Setting.Debug_Mode", false);
        boolean global = bool(config, "Setting.AHI_Mend", true);
        boolean slow = bool(config, "Setting.Slow_Mend.Enable", true);
        int factor = integer(config, "Setting.Slow_Mend.Mitigation_Factor", 5);
        if (factor < 1) throw new IllegalArgumentException("Mitigation_Factor 必须 >= 1。");
        boolean limit = bool(config, "Setting.Max_Mend_Limit.Enable", true);
        int maximum = MendCount.validate(integer(config, "Setting.Max_Mend_Limit.Max_Number", 1000));
        boolean successOnly = bool(config, "Setting.Max_Mend_Limit.Count_Successful_Only", false);
        boolean message = bool(config, "Setting.Max_Mend_Limit.Deactivate_Message", true);
        boolean rename = bool(config, "Setting.Max_Mend_Limit.Change_Item_Name", true);
        String brokenMessage = string(config, "Setting.Max_Mend_Limit.Broken_Message", "你的这件装备已经很破旧了，是时候换一个了。");
        String brokenPrefix = string(config, "Setting.Max_Mend_Limit.Broken_Prefix", "§7破损的-");
        String prefix = string(config, "Setting.Max_Mend_Limit.Mend_Frequency_Lore_Name", "§9剩余修补次数：");
        if (prefix.isBlank()) throw new IllegalArgumentException("Mend_Frequency_Lore_Name 不能为空。");
        List<String> oldNames = strings(config, "Setting.Max_Mend_Limit.Old_Mend_Frequency_Lore_Name");
        boolean enabled = bool(cards, "ExpansionCard.Enable", true);
        boolean beyond = bool(cards, "ExpansionCard.AllowBeyond", true);
        boolean special = bool(cards, "ExpansionCard.AllowSetSP", true);
        boolean legacy = bool(cards, "ExpansionCard.AcceptLegacyCards", true);
        String cardName = string(text, "text.name", "§3§l经验修补拓展卡");
        String setMode = string(text, "text.set_mode", "§e模式§f：§b设置");
        String addMode = string(text, "text.add_mode", "§e模式§f：§b增加");
        String frequency = string(text, "text.frequency", "§e修改次数§f：§3");
        List<String> usage = strings(text, "text.useway");
        if (setMode.isBlank() || addMode.isBlank() || setMode.equals(addMode) || frequency.isBlank()) {
            throw new IllegalArgumentException("卡片模式名称必须非空且不同，次数前缀不能为空。");
        }
        // Commit only after every file and value has been validated.
        Slow_mending_re.setDebug_Mode(debug);
        Slow_mending_re.setAHI_Mend(global);
        Slow_mending_re.setSlow_Mend_Enable(slow);
        Slow_mending_re.setMitigation_Factor(factor);
        Slow_mending_re.setMax_Mend_Limit(limit);
        Slow_mending_re.setMax_Mend_Limit_Number(maximum);
        Slow_mending_re.setCountSuccessfulOnly(successOnly);
        Slow_mending_re.setSendMSG(message);
        Slow_mending_re.setChange_Item_Name(rename);
        Slow_mending_re.setBroken_Message(brokenMessage);
        Slow_mending_re.setBroken_Prefix(brokenPrefix);
        Slow_mending_re.setMend_Frequency_Lore_Name(prefix);
        Slow_mending_re.setOld_Mend_Frequency_Lore_Name(oldNames);
        ExpansionCard.configure(enabled, beyond, special, legacy, cardName, setMode, addMode, frequency, usage);
    }

    private static YamlConfiguration read(File file) throws Exception {
        var config = new YamlConfiguration();
        config.load(file);
        return config;
    }

    static boolean bool(YamlConfiguration config, String path, boolean fallback) {
        Object value = config.get(path);
        if (value == null) return fallback;
        if (value instanceof Boolean b) return b;
        throw new IllegalArgumentException(path + " 必须为 true/false。");
    }

    static int integer(YamlConfiguration config, String path, int fallback) {
        Object value = config.get(path);
        if (value == null) return fallback;
        if (value instanceof Integer i) return i;
        throw new IllegalArgumentException(path + " 必须为 32 位整数。");
    }

    private static String string(YamlConfiguration config, String path, String fallback) {
        Object value = config.get(path);
        if (value == null) return fallback;
        if (value instanceof String s) return s;
        throw new IllegalArgumentException(path + " 必须为文本。");
    }

    private static List<String> strings(YamlConfiguration config, String path) {
        Object value = config.get(path);
        if (value == null) return List.of();
        if (value instanceof List<?> list && list.stream().allMatch(String.class::isInstance)) {
            return list.stream().map(String.class::cast).toList();
        }
        throw new IllegalArgumentException(path + " 必须为文本列表。");
    }
}
