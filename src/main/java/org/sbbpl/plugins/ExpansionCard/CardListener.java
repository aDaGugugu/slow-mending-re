package org.sbbpl.plugins.ExpansionCard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class CardListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent playerInteractEvent){
        //判断是否右键
        if(!(playerInteractEvent.getAction()== Action.RIGHT_CLICK_AIR || playerInteractEvent.getAction()== Action.RIGHT_CLICK_BLOCK)){return;}
        //获取物品
        ItemMeta itemInHand = playerInteractEvent.getItem().getItemMeta();
        if (!itemInHand.hasLore()) {return;}
        if (!(itemInHand.getLore().get(0)).equals(ExpansionCard.identifier)) {return;}
        //传递
        ExpansionCard expansionCard = new ExpansionCard(itemInHand);
        //使用卡片
        boolean canuse = expansionCard.use(playerInteractEvent.getPlayer());
        //如果卡片被使用，取消事件
        if (canuse) playerInteractEvent.setCancelled(true);
    }
}
