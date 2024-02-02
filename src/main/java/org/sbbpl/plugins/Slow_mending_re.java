package org.sbbpl.plugins;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public final class Slow_mending_re extends JavaPlugin {

    //调试模式
    private static boolean Debug_Mode;
    public static boolean isDebug_Mode() {
        return Debug_Mode;
    }

    //是否启用全局经验修补
    private static boolean AHI_Mend;
    public static boolean isAHI_Mend() {
        return AHI_Mend;
    }

    //是否启用缓慢修补功能
    private static boolean Slow_Mend_Enable;
    public static boolean isSlow_Mend_Enable() {
        return Slow_Mend_Enable;
    }

    //经验修补的减弱系数
    private static int Mitigation_Factor;
    public static int getMitigation_Factor() {
        return Mitigation_Factor;
    }

    //是否限制经验修补次数
    private static boolean Max_Mend_Limit;
    public static boolean isMax_Mend_Limit() {
        return Max_Mend_Limit;
    }

    //最大修补次数
    private static int Max_Mend_Limit_Number;
    public static int getMax_Mend_Limit_Number() {
        return Max_Mend_Limit_Number;
    }

    //是否发送破损信息
    private static boolean sendMSG;
    public static boolean isSendMSG() {
        return sendMSG;
    }

    //物品破损信息
    private static String Broken_Message;
    public static String getBroken_Message() {
        return Broken_Message;
    }

    //是否更改名称
    private static boolean Change_Item_Name;
    public static boolean isChange_Item_Name() {
        return Change_Item_Name;
    }

    //破损物品前缀
    private static String Broken_Prefix;
    public static String getBroken_Prefix() {
        return Broken_Prefix;
    }

    //用于存储剩余次数的lore名称
    private static String Mend_Frequency_Lore_Name;
    public static String getMend_Frequency_Lore_Name() {
        return Mend_Frequency_Lore_Name;
    }


    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("SLMR:\n==================================\n        Slow mending re\n==================================");

        //一言功能（误
        try {System.out.println("\n" + new BufferedReader(new InputStreamReader(new URL("https://v1.jinrishici.com/rensheng.txt").openStream(), StandardCharsets.UTF_8)).readLine() + "\n");} catch (Exception ignored) {}


        //正式开始加载
        this.getLogger().info("正在加载插件...");

        //注册监听器
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);

        //初始化配置文件:
        //config配置
        saveDefaultConfig();
        //command配置
        this.saveResource("command.yml", false);

        //开始加载配置
        try {
            loadPlugins();
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
    }

    public void loadPlugins() throws Exception {
        //加载Sitting

        //加载Debug_Mode
        if (this.getConfig().contains("Setting.Debug_Mode")){
            try {
                Debug_Mode = getConfig().getBoolean("Setting.Debug_Mode");
            }catch (NullPointerException exception){
                Debug_Mode = false;
            }
        }else Debug_Mode = false;

        //加载AHI_Mend
        if (this.getConfig().contains("Setting.AHI_Mend")){
            try {
                AHI_Mend = getConfig().getBoolean("Setting.AHI_Mend");
                System.out.println(0);
                System.out.println(AHI_Mend);
            }catch (NullPointerException exception){
                System.out.println(1);
                AHI_Mend = true;
            }
        }else {
            AHI_Mend = true;
            System.out.println(2);
        }

        //加载slowmend相关项:
        //是否启用
        if (this.getConfig().contains("Setting.Slow_Mend.Enable")){
            try {
                Slow_Mend_Enable = getConfig().getBoolean("Setting.Slow_Mend.Enable");
            }catch (NullPointerException exception){
                Slow_Mend_Enable = true;
            }
        }else Slow_Mend_Enable = true;
        //如果启用，加载剩余项
        if (Slow_Mend_Enable){
            //加载缓慢系数
            if (this.getConfig().contains("Setting.Slow_Mend.Mitigation_Factor")){
                try {
                    Mitigation_Factor = getConfig().getInt("Setting.Slow_Mend.Mitigation_Factor");
                }catch (NullPointerException exception){
                    Mitigation_Factor = 5;
                }
            }else Mitigation_Factor = 5;
        }

        //加载经验修补上限相关项
        //是否启用
        if (this.getConfig().contains("Setting.Max_Mend_Limit.Enable")){
            try {
                Max_Mend_Limit = getConfig().getBoolean("Setting.Max_Mend_Limit.Enable");
            }catch (NullPointerException exception){
                Max_Mend_Limit = true;
            }
        }else Max_Mend_Limit = true;
        //如果启用，加载剩余项
        if (Max_Mend_Limit){
            //加载最大次数
            if (this.getConfig().contains("Setting.Max_Mend_Limit.Max_Number")){
                try {
                    Max_Mend_Limit_Number = getConfig().getInt("Setting.Max_Mend_Limit.Max_Number");
                }catch (NullPointerException exception){
                    Max_Mend_Limit_Number = 1000;
                }
            }else Max_Mend_Limit_Number = 1000;
            //加载是否启用发送破损信息
            if (this.getConfig().contains("Setting.Max_Mend_Limit.Deactivate_Message")){
                try {
                    sendMSG = getConfig().getBoolean("Setting.Max_Mend_Limit.Deactivate_Message");
                }catch (NullPointerException exception){
                    sendMSG = true;
                }
            }else sendMSG = true;
            //发送的信息内容
            if (this.getConfig().contains("Setting.Max_Mend_Limit.Broken_Message")){
                try {
                    Broken_Message = getConfig().getString("Setting.Max_Mend_Limit.Broken_Message");
                }catch (NullPointerException exception){
                    Broken_Message = "你的这件装备已经很破旧了，是时候换一个了。";
                }
            }else Broken_Message = "你的这件装备已经很破旧了，是时候换一个了。";
            //装备达到上限后是否更改名称
            if (this.getConfig().contains("Setting.Max_Mend_Limit.Change_Item_Name")){
                try {
                    Change_Item_Name = getConfig().getBoolean("Setting.Max_Mend_Limit.Change_Item_Name");
                }catch (NullPointerException exception){
                    Change_Item_Name = true;
                }
            }else Change_Item_Name = true;
            //加载更改名字前缀的内容
            if (this.getConfig().contains("Setting.Max_Mend_Limit.Broken_Prefix")){
                try {
                    Broken_Prefix = getConfig().getString("Setting.Max_Mend_Limit.Broken_Prefix");
                }catch (NullPointerException exception){
                    Broken_Prefix = "§7破损的-";
                }
            }else Broken_Prefix = "§7破损的-";
            //加载用于存储次数的lore的名称
            if (this.getConfig().contains("Setting.Max_Mend_Limit.Mend_Frequency_Lore_Name")){
                try {
                    Mend_Frequency_Lore_Name = getConfig().getString("Setting.Max_Mend_Limit.Mend_Frequency_Lore_Name");
                }catch (NullPointerException exception){
                    Mend_Frequency_Lore_Name = null;
                    throw new Exception("请检查配置文件Mend_Frequency_Lore_Name！！！,无法读取！");
                }
            }else throw new Exception("请检查配置文件Mend_Frequency_Lore_Name！！！，位置为空！");
        }
    }
}
