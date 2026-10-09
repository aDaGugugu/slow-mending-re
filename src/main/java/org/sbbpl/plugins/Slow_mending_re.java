package org.sbbpl.plugins;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.sbbpl.plugins.ExpansionCard.CardListener;
import org.sbbpl.plugins.ExpansionCard.ExpansionCard;
import org.sbbpl.plugins.command.CommandTabCompleter;
import org.sbbpl.plugins.command.SLMCommand;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.List;



public final class Slow_mending_re extends JavaPlugin implements CommandExecutor {

    //调试模式
    private static boolean Debug_Mode;
    public static boolean isDebug_Mode() {
        return Debug_Mode;
    }
    public static void setDebug_Mode(boolean debug_Mode) {
        Debug_Mode = debug_Mode;
    }

    //是否启用全局经验修补
    private static boolean AHI_Mend;
    public static boolean isAHI_Mend() {
        return AHI_Mend;
    }
    public static void setAHI_Mend(boolean AHI_Mend) {
        Slow_mending_re.AHI_Mend = AHI_Mend;
    }

    //是否启用缓慢修补功能
    private static boolean Slow_Mend_Enable;
    public static boolean isSlow_Mend_Enable() {
        return Slow_Mend_Enable;
    }
    public static void setSlow_Mend_Enable(boolean slow_Mend_Enable) {
        Slow_Mend_Enable = slow_Mend_Enable;
    }

    //经验修补的减弱系数
    private static int Mitigation_Factor;
    public static int getMitigation_Factor() {
        return Mitigation_Factor;
    }
    public static void setMitigation_Factor(int mitigation_Factor) {
        Mitigation_Factor = mitigation_Factor;
    }

    //是否限制经验修补次数
    private static boolean Max_Mend_Limit;
    public static boolean isMax_Mend_Limit() {
        return Max_Mend_Limit;
    }
    public static void setMax_Mend_Limit(boolean max_Mend_Limit) {
        Max_Mend_Limit = max_Mend_Limit;
    }

    //最大修补次数
    private static int Max_Mend_Limit_Number;
    public static int getMax_Mend_Limit_Number() {
        return Max_Mend_Limit_Number;
    }
    public static void setMax_Mend_Limit_Number(int max_Mend_Limit_Number) {
        Max_Mend_Limit_Number = max_Mend_Limit_Number;
    }

    //是否发送破损信息
    private static boolean sendMSG;
    public static boolean isSendMSG() {
        return sendMSG;
    }
    public static void setSendMSG(boolean sendMSG) {
        Slow_mending_re.sendMSG = sendMSG;
    }

    //物品破损信息
    private static String Broken_Message;
    public static String getBroken_Message() {
        return Broken_Message;
    }
    public static void setBroken_Message(String broken_Message) {
        Broken_Message = broken_Message;
    }

    //是否更改名称
    private static boolean Change_Item_Name;
    public static boolean isChange_Item_Name() {
        return Change_Item_Name;
    }
    public static void setChange_Item_Name(boolean change_Item_Name) {
        Change_Item_Name = change_Item_Name;
    }

    //破损物品前缀
    private static String Broken_Prefix;
    public static String getBroken_Prefix() {
        return Broken_Prefix;
    }
    public static void setBroken_Prefix(String broken_Prefix) {
        Broken_Prefix = broken_Prefix;
    }

    //用于存储剩余次数的lore名称
    private static String Mend_Frequency_Lore_Name;
    public static String getMend_Frequency_Lore_Name() {
        return Mend_Frequency_Lore_Name;
    }
    public static void setMend_Frequency_Lore_Name(String mend_Frequency_Lore_Name) {
        Mend_Frequency_Lore_Name = mend_Frequency_Lore_Name;
    }

    //曾用名列表
    private static List<String> Old_Mend_Frequency_Lore_Name = List.of();
    public static List<String> getOld_Mend_Frequency_Lore_Name() {
        return Old_Mend_Frequency_Lore_Name;
    }

    public static void setOld_Mend_Frequency_Lore_Name(List<String> old_Mend_Frequency_Lore_Name) {
        Old_Mend_Frequency_Lore_Name = old_Mend_Frequency_Lore_Name;
    }

    //超类
    private static Slow_mending_re SLM;
    public static Slow_mending_re getSLM() {
        return SLM;
    }


    private static boolean countSuccessfulOnly;
    public static boolean isCountSuccessfulOnly() { return countSuccessfulOnly; }
    public static void setCountSuccessfulOnly(boolean value) { countSuccessfulOnly = value; }

    public void ensureResources() {
        saveDefaultConfig();
        for (String resource : List.of("command.yml", "ExpansionCard/cardinfo.yml", "ExpansionCard/cardconfig.yml")) {
            if (!new File(getDataFolder(), resource).isFile()) saveResource(resource, false);
        }
    }

    @Override
    public void onEnable() {
        SLM = this;
        try {
            ensureResources();
            loadPL.loadPlugins();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "配置加载失败，插件已禁用。", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(new MendEventListener(), this);
        // Register exactly once; the listener checks the current enable flag after reload.
        Bukkit.getPluginManager().registerEvents(new CardListener(), this);
        var command = Objects.requireNonNull(getCommand("slowmending"));
        command.setExecutor(new SLMCommand());
        command.setTabCompleter(new CommandTabCompleter());
        getLogger().info("Slow Mending Re " + getDescription().getVersion() + " 已加载。");
    }

    @Override
    public void onDisable() {
        getLogger().info("插件已卸载。");
        SLM = null;
    }
}
