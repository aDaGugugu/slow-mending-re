package org.sbbpl.plugins.ExpansionCard;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sbbpl.plugins.Slow_mending_re;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ExpansionCard {
    static Slow_mending_re SLM = Slow_mending_re.getSLM();
    //卡片配置相关
    //是否启用
    private static boolean Enable;
    public static boolean isEnable() {
        return Enable;
    }
    public static void setEnable(boolean enable) {
        Enable = enable;
    }
    //是否允许超出限制
    private static boolean AllowBeyond;
    public static boolean isAllowBeyond() {
        return AllowBeyond;
    }
    public static void setAllowBeyond(boolean allowBeyond) {
        AllowBeyond = allowBeyond;
    }
    //是否允许特殊值
    private static boolean AllowSetSP;
    public static boolean isAllowSetSP() {
        return AllowSetSP;
    }
    public static void setAllowSetSP(boolean allowSetSP) {
        AllowSetSP = allowSetSP;
    }

    //检测字符串，添加在第一行
    public static String identifier = "\u0005\u0002\u0000";

    //是否为合法
    private boolean isCard = true;
    //卡片模式
    private boolean cardMode;

    public ExpansionCard(ItemMeta itemMeta){
        if (!isEnable()) return;
        //获取lore
        List<String> lores;
        if (itemMeta.hasLore()) {
            lores = itemMeta.getLore();
        }else {
            isCard = false;
            return;
        }

        //解析
        parseCard(lores);


    }

    private void parseCard(List<String> lores) {
        //判断字符串
        if (!lores.get(0).equals(identifier)){
            isCard = false;
            return;
        }

        //循环lore，找目标项
        for(String lore : lores) {
            //卡片模式
            if (lore.startsWith("§e模式§f：§b")){
                lore.substring(9);
                if (lore.equals("设置")) cardMode = true;
                else if (lore.equals("增加")) cardMode = false;
                else {
                    isCard =false;
                    return;
                }
            }
            //修改次数
        }
    }

    public boolean use(Player player){
        //使用
        return false;
    }

    public static ItemStack createCard(boolean isSet,int num){
        //读取配置
        File configfile = new File(SLM.getDataFolder(), "ExpansionCard/cardinfo.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configfile);
        List<String> useway = config.getStringList("text.useway");

        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        //写入


        //名称
        assert itemMeta != null;
        itemMeta.setDisplayName("§3§l经验修补拓展卡");
        //lore
        List<String> lores = new LinkedList<>();
        //识别符号
        lores.add(identifier);
        //使用方法
        lores.add("§e使用方法§f：");
        for (String text:useway){
            lores.add(text);
        }
        //卡片模式
        if (isSet) {
            lores.add("§e模式§f：§b设置");
        }else {
            lores.add("§e模式§f：§b增加");
        }
        //修改次数
        lores.add("§e修改次数§f：§3"+num);


        //打入物品信息
        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
