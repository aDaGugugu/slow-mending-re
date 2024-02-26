package org.sbbpl.plugins;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.sbbpl.plugins.command.CommandTabCompleter;
import org.sbbpl.plugins.command.SLMCommand;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private static List<String> Old_Mend_Frequency_Lore_Name;
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

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("SLMR:\n==================================\n        Slow mending re\n==================================");

        //一言功能（误
        try {System.out.println("\n" + new BufferedReader(new InputStreamReader(new URL("https://v1.jinrishici.com/rensheng.txt").openStream(), StandardCharsets.UTF_8)).readLine() + "\n");} catch (Exception ignored) {}


        //正式开始加载
        this.getLogger().info("正在加载插件...");

        //传递超类
        SLM = this;

        //注册监听器
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);

        //注册命令监听器
        Bukkit.getPluginCommand("slowmending").setExecutor(new SLMCommand());

        //注册补全
        Bukkit.getPluginCommand("slowmending").setTabCompleter(new CommandTabCompleter());

        //初始化配置文件:
        //config配置
        saveDefaultConfig();
        //command配置
        this.saveResource("command.yml", false);

        //开始加载配置
        try {
            loadPL.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //加载完成（大概
        this.getLogger().info("加载完成！");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        //一言功能（误
        try {System.out.println("\n" + new BufferedReader(new InputStreamReader(new URL("https://v1.jinrishici.com/rensheng.txt").openStream(), StandardCharsets.UTF_8)).readLine() + "\n");} catch (Exception ignored) {}
        getLogger().info("插件已卸载。");
    }

//    public void loadPlugins() throws Exception {
//        //加载Sitting
//
//        //加载Debug_Mode
//        if (this.getConfig().contains("Setting.Debug_Mode")){
//            try {
//                Slow_mending_re.setDebug_Mode(getConfig().getBoolean("Setting.Debug_Mode"));
//            }catch (NullPointerException exception){
//                Slow_mending_re.setDebug_Mode(false);
//            }
//        }else Slow_mending_re.setDebug_Mode(false);
//
//        //加载AHI_Mend
//        if (this.getConfig().contains("Setting.AHI_Mend")){
//            try {
//                Slow_mending_re.setAHI_Mend(getConfig().getBoolean("Setting.AHI_Mend"));
//            }catch (NullPointerException exception){
//                Slow_mending_re.setAHI_Mend(true);
//            }
//        }else Slow_mending_re.setAHI_Mend(true);
//
//
//        //加载slowmend相关项:
//        //是否启用
//        if (this.getConfig().contains("Setting.Slow_Mend.Enable")){
//            try {
//                Slow_mending_re.setSlow_Mend_Enable(getConfig().getBoolean("Setting.Slow_Mend.Enable"));
//            }catch (NullPointerException exception){
//                Slow_mending_re.setSlow_Mend_Enable(true);
//            }
//        }else Slow_mending_re.setSlow_Mend_Enable(true);
//        //如果启用，加载剩余项
//        if (Slow_mending_re.isSlow_Mend_Enable()){
//            //加载缓慢系数
//            if (this.getConfig().contains("Setting.Slow_Mend.Mitigation_Factor")){
//                try {
//                    Slow_mending_re.setMitigation_Factor(getConfig().getInt("Setting.Slow_Mend.Mitigation_Factor"));
//                }catch (NullPointerException exception){
//                    Slow_mending_re.setMitigation_Factor(5);
//                }
//            }else Slow_mending_re.setMitigation_Factor(5);
//        }
//
//        //加载经验修补上限相关项
//        //是否启用
//        if (this.getConfig().contains("Setting.Max_Mend_Limit.Enable")){
//            try {
//                Slow_mending_re.setMax_Mend_Limit(getConfig().getBoolean("Setting.Max_Mend_Limit.Enable"));
//            }catch (NullPointerException exception){
//                Slow_mending_re.setMax_Mend_Limit(true);
//            }
//        }else Slow_mending_re.setMax_Mend_Limit(true);
//        //如果启用，加载剩余项
//        if (Slow_mending_re.isMax_Mend_Limit()){
//            //加载最大次数
//            if (this.getConfig().contains("Setting.Max_Mend_Limit.Max_Number")){
//                try {
//                    Slow_mending_re.setMax_Mend_Limit_Number(getConfig().getInt("Setting.Max_Mend_Limit.Max_Number"));
//                }catch (NullPointerException exception){
//                    Slow_mending_re.setMax_Mend_Limit_Number(1000);
//                }
//            }else Slow_mending_re.setMax_Mend_Limit_Number(1000);
//            //加载是否启用发送破损信息
//            if (this.getConfig().contains("Setting.Max_Mend_Limit.Deactivate_Message")){
//                try {
//                    Slow_mending_re.setSendMSG(getConfig().getBoolean("Setting.Max_Mend_Limit.Deactivate_Message"));
//                }catch (NullPointerException exception){
//                    Slow_mending_re.setSendMSG(true);
//                }
//            }else Slow_mending_re.setSendMSG(true);
//            //发送的信息内容
//            if (this.getConfig().contains("Setting.Max_Mend_Limit.Broken_Message")){
//                try {
//                    Slow_mending_re.setBroken_Message(getConfig().getString("Setting.Max_Mend_Limit.Broken_Message"));
//                }catch (NullPointerException exception){
//                    Slow_mending_re.setBroken_Message("你的这件装备已经很破旧了，是时候换一个了。");
//                }
//            }else Slow_mending_re.setBroken_Message("你的这件装备已经很破旧了，是时候换一个了。");
//            //装备达到上限后是否更改名称
//            if (this.getConfig().contains("Setting.Max_Mend_Limit.Change_Item_Name")){
//                try {
//                    Slow_mending_re.setChange_Item_Name(getConfig().getBoolean("Setting.Max_Mend_Limit.Change_Item_Name"));
//                }catch (NullPointerException exception){
//                    Slow_mending_re.setChange_Item_Name(true);
//                }
//            }else Slow_mending_re.setChange_Item_Name(true);
//            //加载更改名字前缀的内容
//            if (this.getConfig().contains("Setting.Max_Mend_Limit.Broken_Prefix")){
//                try {
//                    Slow_mending_re.setBroken_Prefix(getConfig().getString("Setting.Max_Mend_Limit.Broken_Prefix"));
//                }catch (NullPointerException exception){
//                    Slow_mending_re.setBroken_Prefix("§7破损的-");
//                }
//            }else Slow_mending_re.setBroken_Prefix("§7破损的-");
//            //加载用于存储次数的lore的名称
//            if (this.getConfig().contains("Setting.Max_Mend_Limit.Mend_Frequency_Lore_Name")){
//                try {
//                    Slow_mending_re.setMend_Frequency_Lore_Name(getConfig().getString("Setting.Max_Mend_Limit.Mend_Frequency_Lore_Name"));
//                }catch (NullPointerException exception){
//                    Slow_mending_re.setMend_Frequency_Lore_Name(null);
//                    throw new Exception("请检查配置文件Mend_Frequency_Lore_Name！！！,无法读取！");
//                }
//            }else {
//                Slow_mending_re.setMend_Frequency_Lore_Name(null);
//                throw new Exception("请检查配置文件Mend_Frequency_Lore_Name！！！，位置为空！");
//            }
//            //加载旧名称
//            if (this.getConfig().contains("Setting.Max_Mend_Limit.Old_Mend_Frequency_Lore_Name")){
//                try {
//                    Slow_mending_re.setOld_Mend_Frequency_Lore_Name(getConfig().getStringList("Setting.Max_Mend_Limit.Old_Mend_Frequency_Lore_Name"));
//                }catch (NullPointerException exception){
//                    this.getLogger().warning("无法加载Old_Mend_Frequency_Lore_Name！请检查配置确保没有非法内容！");
//                }
//            }
//        }
//    }


}
