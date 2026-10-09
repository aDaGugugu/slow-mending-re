package org.sbbpl.plugins.ExpansionCard;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class CardListener implements Listener {
    // Air interactions may already be cancelled because the vanilla item has no action.
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!ExpansionCard.isEnable() || event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.useItemInHand() == Event.Result.DENY || event.getItem() == null) return;
        ExpansionCard card = new ExpansionCard(event.getItem().getItemMeta());
        if (!card.isCard()) return;
        // Suppress the book/block action even if the target or card values are rejected.
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        card.use(event.getPlayer());
    }
}
