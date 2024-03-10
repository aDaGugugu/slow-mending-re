package org.sbbpl.plugins;

import org.bukkit.configuration.file.YamlConfiguration;
import org.sbbpl.plugins.ExpansionCard.ExpansionCard;

import java.io.File;

public class loadPL {
    public static void loadPlugins() throws Exception {
        //加载超类
        Slow_mending_re SLM = Slow_mending_re.getSLM();
        //加载Sitting

        //加载Debug_Mode
        if (SLM.getConfig().contains("Setting.Debug_Mode")){
            try {
                Slow_mending_re.setDebug_Mode(SLM.getConfig().getBoolean("Setting.Debug_Mode"));
            }catch (NullPointerException exception){
                Slow_mending_re.setDebug_Mode(false);
            }
        }else Slow_mending_re.setDebug_Mode(false);

        //加载AHI_Mend
        if (SLM.getConfig().contains("Setting.AHI_Mend")){
            try {
                Slow_mending_re.setAHI_Mend(SLM.getConfig().getBoolean("Setting.AHI_Mend"));
            }catch (NullPointerException exception){
                Slow_mending_re.setAHI_Mend(true);
            }
        }else Slow_mending_re.setAHI_Mend(true);


        //加载slowmend相关项:
        //是否启用
        if (SLM.getConfig().contains("Setting.Slow_Mend.Enable")){
            try {
                Slow_mending_re.setSlow_Mend_Enable(SLM.getConfig().getBoolean("Setting.Slow_Mend.Enable"));
            }catch (NullPointerException exception){
                Slow_mending_re.setSlow_Mend_Enable(true);
            }
        }else Slow_mending_re.setSlow_Mend_Enable(true);
        //如果启用，加载剩余项
        if (Slow_mending_re.isSlow_Mend_Enable()){
            //加载缓慢系数
            if (SLM.getConfig().contains("Setting.Slow_Mend.Mitigation_Factor")){
                try {
                    Slow_mending_re.setMitigation_Factor(SLM.getConfig().getInt("Setting.Slow_Mend.Mitigation_Factor"));
                }catch (NullPointerException exception){
                    Slow_mending_re.setMitigation_Factor(5);
                }
            }else Slow_mending_re.setMitigation_Factor(5);
        }

        //加载经验修补上限相关项
        //是否启用
        if (SLM.getConfig().contains("Setting.Max_Mend_Limit.Enable")){
            try {
                Slow_mending_re.setMax_Mend_Limit(SLM.getConfig().getBoolean("Setting.Max_Mend_Limit.Enable"));
            }catch (NullPointerException exception){
                Slow_mending_re.setMax_Mend_Limit(true);
            }
        }else Slow_mending_re.setMax_Mend_Limit(true);
        //如果启用，加载剩余项
        if (Slow_mending_re.isMax_Mend_Limit()){
            //加载最大次数
            if (SLM.getConfig().contains("Setting.Max_Mend_Limit.Max_Number")){
                try {
                    Slow_mending_re.setMax_Mend_Limit_Number(SLM.getConfig().getInt("Setting.Max_Mend_Limit.Max_Number"));
                }catch (NullPointerException exception){
                    Slow_mending_re.setMax_Mend_Limit_Number(1000);
                }
            }else Slow_mending_re.setMax_Mend_Limit_Number(1000);
            //加载是否启用发送破损信息
            if (SLM.getConfig().contains("Setting.Max_Mend_Limit.Deactivate_Message")){
                try {
                    Slow_mending_re.setSendMSG(SLM.getConfig().getBoolean("Setting.Max_Mend_Limit.Deactivate_Message"));
                }catch (NullPointerException exception){
                    Slow_mending_re.setSendMSG(true);
                }
            }else Slow_mending_re.setSendMSG(true);
            //发送的信息内容
            if (SLM.getConfig().contains("Setting.Max_Mend_Limit.Broken_Message")){
                try {
                    Slow_mending_re.setBroken_Message(SLM.getConfig().getString("Setting.Max_Mend_Limit.Broken_Message"));
                }catch (NullPointerException exception){
                    Slow_mending_re.setBroken_Message("你的这件装备已经很破旧了，是时候换一个了。");
                }
            }else Slow_mending_re.setBroken_Message("你的这件装备已经很破旧了，是时候换一个了。");
            //装备达到上限后是否更改名称
            if (SLM.getConfig().contains("Setting.Max_Mend_Limit.Change_Item_Name")){
                try {
                    Slow_mending_re.setChange_Item_Name(SLM.getConfig().getBoolean("Setting.Max_Mend_Limit.Change_Item_Name"));
                }catch (NullPointerException exception){
                    Slow_mending_re.setChange_Item_Name(true);
                }
            }else Slow_mending_re.setChange_Item_Name(true);
            //加载更改名字前缀的内容
            if (SLM.getConfig().contains("Setting.Max_Mend_Limit.Broken_Prefix")){
                try {
                    Slow_mending_re.setBroken_Prefix(SLM.getConfig().getString("Setting.Max_Mend_Limit.Broken_Prefix"));
                }catch (NullPointerException exception){
                    Slow_mending_re.setBroken_Prefix("§7破损的-");
                }
            }else Slow_mending_re.setBroken_Prefix("§7破损的-");
            //加载用于存储次数的lore的名称
            if (SLM.getConfig().contains("Setting.Max_Mend_Limit.Mend_Frequency_Lore_Name")){
                try {
                    Slow_mending_re.setMend_Frequency_Lore_Name(SLM.getConfig().getString("Setting.Max_Mend_Limit.Mend_Frequency_Lore_Name"));
                }catch (NullPointerException exception){
                    Slow_mending_re.setMend_Frequency_Lore_Name(null);
                    throw new Exception("请检查配置文件Mend_Frequency_Lore_Name！！！,无法读取！");
                }
            }else {
                Slow_mending_re.setMend_Frequency_Lore_Name(null);
                throw new Exception("请检查配置文件Mend_Frequency_Lore_Name！！！，位置为空！");
            }
            //加载旧名称
            if (SLM.getConfig().contains("Setting.Max_Mend_Limit.Old_Mend_Frequency_Lore_Name")){
                try {
                    Slow_mending_re.setOld_Mend_Frequency_Lore_Name(SLM.getConfig().getStringList("Setting.Max_Mend_Limit.Old_Mend_Frequency_Lore_Name"));
                }catch (NullPointerException exception){
                    SLM.getLogger().warning("无法加载Old_Mend_Frequency_Lore_Name！请检查配置确保没有非法内容！");
                }
            }
        }
        //加载卡片
        File cardConfigfile = new File(SLM.getDataFolder(), "ExpansionCard/cardconfig.yml");
        YamlConfiguration cardConfig = YamlConfiguration.loadConfiguration(cardConfigfile);
        //是否启用
        if (cardConfig.contains("ExpansionCard.Enable")){
            try {
                ExpansionCard.setEnable(cardConfig.getBoolean("ExpansionCard.Enable"));
            }catch (NullPointerException exception){
                SLM.getLogger().warning("无法加载ExpansionCard.Enable！请检查配置确保没有非法内容！");
            }
        }else {
            throw new Exception("请检查配置文件ExpansionCard.Enable！！！，位置为空！");
        }
        if (ExpansionCard.isEnable()) {
            //加载是否允许超出
            if (cardConfig.contains("ExpansionCard.AllowBeyond")){
                try {
                    ExpansionCard.setAllowBeyond(cardConfig.getBoolean("ExpansionCard.AllowBeyond"));
                }catch (NullPointerException exception){
                    SLM.getLogger().warning("无法加载ExpansionCard.AllowBeyond！请检查配置确保没有非法内容！");
                }
            }else {
                throw new Exception("请检查配置文件ExpansionCard.AllowBeyond！！！，位置为空！");
            }
            //加载是否允许特殊值
            if (cardConfig.contains("ExpansionCard.AllowSetSP")){
                try {
                    ExpansionCard.setAllowSetSP(cardConfig.getBoolean("ExpansionCard.AllowSetSP"));
                }catch (NullPointerException exception){
                    SLM.getLogger().warning("无法加载ExpansionCard.AllowSetSP！请检查配置确保没有非法内容！");
                }
            }else {
                throw new Exception("请检查配置文件ExpansionCard.AllowSetSP！！！，位置为空！");
            }
        }
    }
}
