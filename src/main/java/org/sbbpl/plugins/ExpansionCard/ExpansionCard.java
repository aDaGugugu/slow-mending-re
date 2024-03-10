package org.sbbpl.plugins.ExpansionCard;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sbbpl.plugins.MendingItem;
import org.sbbpl.plugins.Slow_mending_re;

import java.io.File;
import java.util.List;
import java.util.Objects;

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

    //卡片数据
    //卡片模式
    private boolean cardMode;
    //检测字符串，添加在第一行
    public static String identifier = "\u0005\u0002\u0000";
    //次数
    private int frequency;

    //是否为合法
    private boolean isCard = true;


    //名称
    String name;
    String set_mode;
    String add_mode;
    String frequencyName;

    public ExpansionCard(ItemMeta itemMeta){
        //读取配置
        File configfile = new File(SLM.getDataFolder(), "ExpansionCard/cardinfo.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configfile);

        name = config.getString("text.name");
        set_mode = config.getString("text.set_mode");
        add_mode = config.getString("text.add_mode");
        frequencyName = config.getString("text.frequency");

        //判断是否启用
        if (!isEnable()) return;
        //获取lore
        List<String> lores;
        //初步判断
        if (itemMeta.hasLore()) {
            lores = itemMeta.getLore();
            assert lores != null;
            if (!lores.get(0).equals(identifier)) isCard =false;
        }else {
            isCard = false;
            return;
        }

        //尝试解析
        try{
            parseCard(lores);
        } catch (Exception e) {
            isCard = false;
        }


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
            if (lore.equals(set_mode)){
                cardMode = true;
            }
            if (lore.equals(add_mode)) {
                cardMode = false;
            }
            //修改次数
            if (lore.startsWith(frequencyName)) {
                lore = lore.substring(frequencyName.length());
                frequency = Integer.parseInt(lore);
            }
        }
    }

    public boolean use(Player player){
        //使用
        //是否启用
        if (!isEnable()) return false;
        //预处理
        if (!isCard) {
            player.sendMessage("§c无法解析拓展卡！");
            return false;
        }

        //获取玩家物品
        ItemStack itemStack = Objects.requireNonNull(player.getEquipment()).getItemInOffHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            //没有持有物品
            player.sendMessage("§c请将要修复的物品放置在副手！");
            return true;
        }

        //先检测当前装备是否有lore
        if (!itemMeta.hasLore()) {
            //没有lore
            player.sendMessage("§c未检测到物品信息！");
            return true;
        }

        //判断模式并进行赋值操作======================================
        if (cardMode){
            //如果为设置模式

            //判断是否为特殊值
            if (!isAllowSetSP()){
                if (frequency<0) {
                    player.sendMessage("§c无法为物品设置特殊次数！");
                    return true;
                }
            }

            //如果超出限制并禁止了超出
            if (frequency > Slow_mending_re.getMax_Mend_Limit_Number() && !isAllowBeyond()) {
                player.sendMessage("§c无法为超出限制的物品设置次数！");
                return true;
            }
            //设置玩家的次数
            try {
                itemMeta = MendingItem.setRemainderMendFrequency(itemMeta, Slow_mending_re.getMend_Frequency_Lore_Name(), frequency, Slow_mending_re.getOld_Mend_Frequency_Lore_Name());
            } catch (NoSuchFieldException e) {
                player.sendMessage("§c无法读取物品信息！");
                return true;
            }
        }
        else {
            //如果为add模式

            //当前次数
            int nowFre;
            //尝试获取次数
            try {
                nowFre = MendingItem.getRemainderMendFrequency(itemMeta,Slow_mending_re.getMend_Frequency_Lore_Name(),Slow_mending_re.getOld_Mend_Frequency_Lore_Name());
            } catch (NoSuchFieldException e) {
                return true;
            }
            //增加
            nowFre += frequency;

            //判断是否为特殊值
            if (!isAllowSetSP()){
                if (nowFre<0) {
                    player.sendMessage("§c无法为物品设置特殊次数！");
                    return true;
                }
            }
            //判断是否超出
            if (nowFre > Slow_mending_re.getMax_Mend_Limit_Number() && !isAllowBeyond()) {
                player.sendMessage("§c无法为超出限制的物品设置次数！");
                return true;
            }
            //修改玩家次数
            try {
                itemMeta = MendingItem.addRemainderMendFrequency(itemMeta,Slow_mending_re.getMend_Frequency_Lore_Name(),frequency,Slow_mending_re.getOld_Mend_Frequency_Lore_Name());
            }catch (Exception e){
                player.sendMessage("§c无法读取物品信息！");
                return true;
            }
        }
        //赋值结束================================================

        //把物品给玩家
        itemStack.setItemMeta(itemMeta);
        player.getEquipment().setItemInOffHand(itemStack);

        //清除卡片
        ItemStack card = player.getEquipment().getItemInMainHand();
        card.setAmount(card.getAmount()-1);
        player.getEquipment().setItemInMainHand(card);


        return true;
    }

}
