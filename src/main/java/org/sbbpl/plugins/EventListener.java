package org.sbbpl.plugins;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.joml.Random;

import java.util.Objects;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerPlayerItemMend(PlayerItemMendEvent playerItemMendEvent){

        //初始化变量
        //剩余修补次数
        int remainderMendFrequency = -4;

        //判断是否启用了全局经验修补
        if(!Slow_mending_re.isAHI_Mend()) {
            //如果没有启用，就取消事件，然后结束
            playerItemMendEvent.setCancelled(true);
            return;
        }

        //============================================================

        //判断是否启用了修补次数限制
        if (Slow_mending_re.isMax_Mend_Limit()){
            //如果启用了
            //读取玩家当前装备的次数：
            //先检测当前装备是否有lore
            if (!Objects.requireNonNull(playerItemMendEvent.getItem().getItemMeta()).hasLore()){
                //没有lore，就说明该物品没有存储相应的数据
                //创建一个lore存储
                ItemMeta itemMeta = MendingItem.createRemainderMendFrequency(playerItemMendEvent.getItem().getItemMeta(), Slow_mending_re.getMend_Frequency_Lore_Name(), Slow_mending_re.getMax_Mend_Limit_Number());
                //然后修改玩家的物品
                playerItemMendEvent.getItem().setItemMeta(itemMeta);
            }

            //获取次数然
            try{
                remainderMendFrequency = MendingItem.getRemainderMendFrequency(playerItemMendEvent.getItem().getItemMeta(), Slow_mending_re.getMend_Frequency_Lore_Name(),Slow_mending_re.getOld_Mend_Frequency_Lore_Name());
            }catch (NoSuchFieldException exception){
                //如果未找到对应lore
                //创建一个lore存储然后给它的上限减一
                ItemMeta itemMeta = MendingItem.createRemainderMendFrequency(playerItemMendEvent.getItem().getItemMeta(), Slow_mending_re.getMend_Frequency_Lore_Name(), Slow_mending_re.getMax_Mend_Limit_Number()-1);
                //然后修改玩家的物品
                playerItemMendEvent.getItem().setItemMeta(itemMeta);
                //获取次数
                remainderMendFrequency = Slow_mending_re.getMax_Mend_Limit_Number()-1;
            }

            //判断是否成功获取
            assert !(remainderMendFrequency == -4);

            //判断是否可以进行修复
            switch (remainderMendFrequency){
                //判断是否为特殊值
                //如：-1为无限，-2为无限且不受减缓限制，-3为禁止经验修补
                case (-1) ->{}//无限
                case (-2) ->{return;}//无限且不受限
                case (-3) ->{
                    playerItemMendEvent.setCancelled(true);
                    return;
                }//禁止经验修补

                default -> {//不符合特殊值
                    //判断是否大于0
                    if (remainderMendFrequency > 0){
                        //大于0，将剩余减一
                        try{
                            //将lor减一
                            ItemMeta itemMeta = MendingItem.addRemainderMendFrequency(playerItemMendEvent.getItem().getItemMeta(), Slow_mending_re.getMend_Frequency_Lore_Name(), -1, Slow_mending_re.getOld_Mend_Frequency_Lore_Name());
                            //修改玩家物品
                            playerItemMendEvent.getItem().setItemMeta(itemMeta);
                        }catch (NoSuchFieldException exception){
                            //不应该到达的地方！
                            System.out.println(exception.toString());
                        }
                    }else {//如果数量耗尽，禁止修复
                        playerItemMendEvent.setCancelled(true);
                        return;
                    }
                }
            }

            //判断玩家物品是否转换为破损
            if (remainderMendFrequency == 1){
                ItemMeta itemMeta = MendingItem.changeBroken(
                        playerItemMendEvent.getItem().getItemMeta(),
                        playerItemMendEvent.getPlayer(),
                        Slow_mending_re.isChange_Item_Name(),
                        Slow_mending_re.getBroken_Prefix(),
                        Slow_mending_re.isSendMSG(),
                        Slow_mending_re.getBroken_Message()
                );
                playerItemMendEvent.getItem().setItemMeta(itemMeta);
            }
        }


        //=====================================================

        //判断是否启用了缓慢经验修补
        if(Slow_mending_re.isSlow_Mend_Enable()){
            //如果启用了，开始判断缓慢经验修补
            int Mitigation_Factor = Slow_mending_re.getMitigation_Factor();
            //随机获取一个数用于判断是否进行修补
            Random radInt = new Random();
            int radMendFactor = radInt.nextInt(Mitigation_Factor);
            //判断是否为0，如果为否，取消事件，然后结束
            if (!(radMendFactor == 0)){
                playerItemMendEvent.setCancelled(true);
                return;
            }
        }
    }
}













