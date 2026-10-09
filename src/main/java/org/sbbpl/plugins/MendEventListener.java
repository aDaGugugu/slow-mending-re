package org.sbbpl.plugins;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

public class MendEventListener implements Listener {
    private final IntUnaryOperator random;

    public MendEventListener() {
        this(bound -> ThreadLocalRandom.current().nextInt(bound));
    }

    public MendEventListener(IntUnaryOperator random) {
        this.random = random;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPlayerItemMend(PlayerItemMendEvent event) {
        if (event.isCancelled() || event.getPlayer().hasPermission("slowmending.bypass")) return;
        if (!Slow_mending_re.isAHI_Mend()) {
            event.setCancelled(true);
            return;
        }
        ItemMeta meta = event.getItem().getItemMeta();
        if (!(meta instanceof Damageable damage) || !damage.hasDamage() || event.getRepairAmount() <= 0) return;

        int remaining = -1;
        String prefix = Slow_mending_re.getMend_Frequency_Lore_Name();
        var oldNames = Slow_mending_re.getOld_Mend_Frequency_Lore_Name();
        if (Slow_mending_re.isMax_Mend_Limit()) {
            try {
                remaining = MendingItem.getRemainderMendFrequency(meta, prefix, oldNames);
            } catch (NoSuchFieldException e) {
                remaining = Slow_mending_re.getMax_Mend_Limit_Number();
            } catch (IllegalArgumentException e) {
                // Do not turn damaged data into a fresh quota.
                event.setCancelled(true);
                return;
            }
            MendingItem.setRemainderMendFrequency(meta, prefix, remaining, oldNames);
            event.getItem().setItemMeta(meta);
            if (remaining == -2) return;
            if (remaining == 0 || remaining == -3) {
                event.setCancelled(true);
                return;
            }
        }

        boolean accepted = !Slow_mending_re.isSlow_Mend_Enable()
                || random.applyAsInt(Slow_mending_re.getMitigation_Factor()) == 0;
        if (!accepted) event.setCancelled(true);
        // Preserve the old attempt-counting default, with an opt-in success-only mode.
        if (Slow_mending_re.isMax_Mend_Limit() && remaining > 0
                && (accepted || !Slow_mending_re.isCountSuccessfulOnly())) {
            MendingItem.setRemainderMendFrequency(meta, prefix, remaining - 1, oldNames);
            if (remaining == 1) {
                MendingItem.changeBroken(meta, event.getPlayer(), Slow_mending_re.isChange_Item_Name(),
                        Slow_mending_re.getBroken_Prefix(), Slow_mending_re.isSendMSG(), Slow_mending_re.getBroken_Message());
            }
            event.getItem().setItemMeta(meta);
        }
    }
}
