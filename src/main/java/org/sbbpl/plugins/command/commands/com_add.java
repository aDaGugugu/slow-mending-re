package org.sbbpl.plugins.command.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.sbbpl.plugins.MendingItem;
import org.sbbpl.plugins.Slow_mending_re;

public class com_add {
    private static int add(int num, Player player , boolean hand){
        //获取物品
        ItemMeta itemMeta;
        if (hand) itemMeta = player.getEquipment().getItemInMainHand().getItemMeta();
        else itemMeta = player.getEquipment().getItemInOffHand().getItemMeta();

        //判断是否持有物品
        if (itemMeta == null) {
            player.sendMessage("§c请将目标物品拿在手上！");
            return -1;
        }
        //判断是否含有
        if (!MendingItem.isHasMendFrequency(itemMeta, Slow_mending_re.getMend_Frequency_Lore_Name())){
            player.sendMessage("§c请将目标物品拿在手上！");
            return -2;
        }
        //尝试更改
        try {
            itemMeta = MendingItem.addRemainderMendFrequency(itemMeta, Slow_mending_re.getMend_Frequency_Lore_Name(),num,Slow_mending_re.getOld_Mend_Frequency_Lore_Name());
            //修改物品
            if (hand){
                player.getEquipment().getItemInMainHand().setItemMeta(itemMeta);
            }else {
                player.getEquipment().getItemInOffHand().setItemMeta(itemMeta);
            }
            player.sendMessage("§b当前持有物品剩余修补次数已增加："+num);
            return 0;
        }catch (NoSuchFieldException e){
            player.sendMessage("§c未能设置次数！");
            return -3;
        }
    }

    public static void playerAdd(Player player ,int num ,Player sender ,boolean hand){
        int result = add(num,player,hand);
        String whichhand;

        if (hand) whichhand = "主手";
        else whichhand = "副手";

        if (result == 0) sender.sendMessage("§b已将目标"+whichhand+"工具增加："+num);
        else if (result == -1) sender.sendMessage("§c未能设置次数！目标指定物品栏为空！");
        else if (result == -2) sender.sendMessage("§c未能设置次数！目标物品未包含次数信息！");
        else if (result == -3) sender.sendMessage("§c未能设置次数！");
    }

    public static void consAdd(Player player ,int num ,boolean hand){
        Slow_mending_re SLM = Slow_mending_re.getSLM();

        int result = add(num,player,hand);
        String whichhand;

        if (hand) whichhand = "主手";
        else whichhand = "副手";

        if (result == 0) SLM.getLogger().info("§b已将目标"+whichhand+"工具增加："+num);
        else if (result == -1) SLM.getLogger().info("§c未能设置次数！目标指定物品栏为空！");
        else if (result == -2) SLM.getLogger().info("§c未能设置次数！目标物品未包含次数信息！");
        else if (result == -3) SLM.getLogger().info("§c未能设置次数！");
    }
}
