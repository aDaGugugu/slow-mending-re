package org.sbbpl.plugins.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.sbbpl.plugins.*;
import org.sbbpl.plugins.ExpansionCard.*;
import org.sbbpl.plugins.command.commands.com_give;
import org.sbbpl.plugins.command.CommandTabCompleter;
import org.sbbpl.plugins.command.SLMCommand;

import java.lang.reflect.Proxy;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

/** Same Java 17 test JAR runs on disposable Paper 1.20–26.3 servers and stops them afterwards. */
public final class ServerTests extends JavaPlugin {
    private int assertions;
    private Inventory storage;
    private ItemStack offhand = new ItemStack(Material.AIR);
    private boolean bypass;
    private Player player;
    private final MendEventListener success = new MendEventListener(bound -> 0);
    private final MendEventListener failure = new MendEventListener(bound -> bound - 1);
    private String prefix;
    private final List<String> messages = new ArrayList<>();

    @Override public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                runTests();
                Files.writeString(Path.of("slow-mending-test-result.txt"), "PASS " + assertions + " assertions\n");
                getLogger().info("SERVER TESTS PASSED: " + assertions + " assertions on " + Bukkit.getVersion());
            } catch (Throwable e) {
                getLogger().log(Level.SEVERE, "SERVER TESTS FAILED", e);
                try { Files.writeString(Path.of("slow-mending-test-result.txt"), "FAIL " + e + "\n"); }
                catch (Exception ignored) { }
            } finally {
                Bukkit.shutdown();
            }
        }, 5L);
    }

    private void runTests() throws Exception {
        check(Slow_mending_re.getSLM().isEnabled(), "plugin enabled");
        prefix = Slow_mending_re.getMend_Frequency_Lore_Name();
        storage = Bukkit.createInventory(null, 36);
        PlayerInventory inventory = proxy(PlayerInventory.class, (method, args) -> switch (method) {
            case "getItemInMainHand" -> storage.getItem(0) == null ? new ItemStack(Material.AIR) : storage.getItem(0);
            case "setItemInMainHand" -> { storage.setItem(0, (ItemStack) args[0]); yield null; }
            case "getItemInOffHand" -> offhand;
            case "setItemInOffHand" -> { offhand = (ItemStack) args[0]; yield null; }
            case "getStorageContents" -> storage.getContents();
            case "getMaxStackSize" -> storage.getMaxStackSize();
            case "addItem" -> storage.addItem((ItemStack[]) args[0]);
            default -> null;
        });
        player = proxy(Player.class, (method, args) -> switch (method) {
            case "getInventory" -> inventory;
            case "hasPermission" -> args[0].equals("slowmending.bypass") ? bypass : true;
            case "getName" -> "SlowMendingTest";
            case "getUniqueId" -> new UUID(0, 1);
            case "sendMessage" -> { if (args[0] instanceof String message) messages.add(message); yield null; }
            default -> null;
        });

        ItemStack fresh = tool();
        success.onPlayerPlayerItemMend(mend(fresh));
        check(count(fresh) == 999, "fresh item charged once");
        ItemStack withLore = tool();
        var meta = withLore.getItemMeta();
        Component other = Component.text("other plugin lore", NamedTextColor.GOLD);
        meta.lore(List.of(other));
        withLore.setItemMeta(meta);
        success.onPlayerPlayerItemMend(mend(withLore));
        check(count(withLore) == 999, "custom lore item charged once, not twice");
        check(withLore.getItemMeta().lore().contains(other), "unrelated rich lore preserved");

        ItemStack previousCancelled = tool();
        var cancelled = mend(previousCancelled);
        cancelled.setCancelled(true);
        success.onPlayerPlayerItemMend(cancelled);
        check(!previousCancelled.getItemMeta().getPersistentDataContainer().has(MendingItem.REMAINING), "cancelled event untouched");
        var zero = mend(previousCancelled);
        zero.setRepairAmount(0);
        success.onPlayerPlayerItemMend(zero);
        check(!previousCancelled.getItemMeta().getPersistentDataContainer().has(MendingItem.REMAINING), "zero repair untouched");

        Slow_mending_re.setOld_Mend_Frequency_Lore_Name(List.of("old:"));
        ItemStack old = tool();
        meta = old.getItemMeta();
        meta.setLore(List.of("other", "old:7"));
        old.setItemMeta(meta);
        success.onPlayerPlayerItemMend(mend(old));
        check(count(old) == 6, "legacy quota migrated");
        check(!old.getItemMeta().getLore().contains("old:7"), "legacy count display replaced");
        meta = old.getItemMeta();
        meta.setLore(List.of(prefix + "999999"));
        old.setItemMeta(meta);
        check(count(old) == 6, "PDC takes precedence over edited lore");
        MendingItem.setRemainderMendFrequency(meta, "new:", 8, List.of());
        old.setItemMeta(meta);
        check(old.getItemMeta().getLore().equals(List.of("new:8")), "stored display prefix survives config rename");
        check(count(ItemStack.deserializeBytes(old.serializeAsBytes())) == 8, "PDC persists through item serialization");

        ItemStack invalid = tool();
        meta = invalid.getItemMeta();
        meta.setLore(List.of(prefix + "invalid"));
        invalid.setItemMeta(meta);
        var invalidEvent = mend(invalid);
        success.onPlayerPlayerItemMend(invalidEvent);
        check(invalidEvent.isCancelled(), "malformed lore denied without exception or fresh quota");

        var unlimited = itemWithCount(-1);
        var unlucky = mend(unlimited);
        failure.onPlayerPlayerItemMend(unlucky);
        check(unlucky.isCancelled() && count(unlimited) == -1, "-1 still slowed");
        var exempt = itemWithCount(-2);
        var exemptEvent = mend(exempt);
        failure.onPlayerPlayerItemMend(exemptEvent);
        check(!exemptEvent.isCancelled() && count(exempt) == -2, "-2 bypasses chance");
        for (int value : List.of(-1, -2, -3, 0)) {
            ItemStack legacyMode = tool();
            meta = legacyMode.getItemMeta();
            meta.setLore(List.of(prefix + value));
            legacyMode.setItemMeta(meta);
            success.onPlayerPlayerItemMend(mend(legacyMode));
            check(count(legacyMode) == value, "old numeric mode retained: " + value);
            check(legacyMode.getItemMeta().getLore().equals(List.of(prefix + MendCount.format(value))),
                    "old mode lore replaced with words: " + value);
            check(count(ItemStack.deserializeBytes(legacyMode.serializeAsBytes())) == value,
                    "readable mode survives serialization: " + value);
            ItemStack setCard = CreateCard.createCard(true, value);
            check(setCard.getItemMeta().getLore().stream().anyMatch(line -> line.endsWith(MendCount.format(value))),
                    "set card displays named state: " + value);
            check(new ExpansionCard(setCard.getItemMeta()).isCard(), "readable set card keeps numeric PDC: " + value);
        }
        for (int value : List.of(0, -3)) {
            var blocked = mend(itemWithCount(value));
            success.onPlayerPlayerItemMend(blocked);
            check(blocked.isCancelled(), "blocked state " + value);
        }
        var attempt = itemWithCount(2);
        failure.onPlayerPlayerItemMend(mend(attempt));
        check(count(attempt) == 1, "default attempt mode retained");
        Slow_mending_re.setCountSuccessfulOnly(true);
        var successfulOnly = itemWithCount(2);
        failure.onPlayerPlayerItemMend(mend(successfulOnly));
        check(count(successfulOnly) == 2, "failed attempt not charged in success-only mode");
        success.onPlayerPlayerItemMend(mend(successfulOnly));
        check(count(successfulOnly) == 1, "successful attempt charged");
        Slow_mending_re.setCountSuccessfulOnly(false);

        var named = itemWithCount(1);
        Component name = Component.text("custom name", NamedTextColor.AQUA);
        meta = named.getItemMeta();
        meta.displayName(name);
        named.setItemMeta(meta);
        success.onPlayerPlayerItemMend(mend(named));
        check(count(named) == 0 && !named.getItemMeta().displayName().equals(name), "last quota marks broken");
        meta = named.getItemMeta();
        MendingItem.setRemainderMendFrequency(meta, prefix, 10, List.of());
        named.setItemMeta(meta);
        check(named.getItemMeta().displayName().equals(name), "refill restores exact rich name");

        Slow_mending_re.setAHI_Mend(false);
        var global = mend(tool());
        success.onPlayerPlayerItemMend(global);
        check(global.isCancelled(), "global disable");
        bypass = true;
        var bypassEvent = mend(tool());
        success.onPlayerPlayerItemMend(bypassEvent);
        check(!bypassEvent.isCancelled(), "explicit bypass permission");
        bypass = false;
        Slow_mending_re.setAHI_Mend(true);
        Slow_mending_re.setMax_Mend_Limit(false);
        var noLimit = tool();
        var noLimitEvent = mend(noLimit);
        failure.onPlayerPlayerItemMend(noLimitEvent);
        check(noLimitEvent.isCancelled() && !noLimit.getItemMeta().getPersistentDataContainer().has(MendingItem.REMAINING), "limits disabled independently of slowing");
        Slow_mending_re.setMax_Mend_Limit(true);

        CardListener listener = new CardListener();
        listener.onPlayerInteract(interact(null, EquipmentSlot.HAND));
        check(true, "empty hand interaction safe");
        ItemStack card = CreateCard.createCard(false, 5);
        card.setAmount(2);
        storage.setItem(0, card);
        offhand = itemWithCount(4);
        listener.onPlayerInteract(interact(card, EquipmentSlot.OFF_HAND));
        check(count(offhand) == 4 && storage.getItem(0).getAmount() == 2, "offhand event ignored");
        var denied = interact(card, EquipmentSlot.HAND);
        denied.setUseItemInHand(Event.Result.DENY);
        listener.onPlayerInteract(denied);
        check(count(offhand) == 4 && storage.getItem(0).getAmount() == 2, "denied item use respected");
        listener.onPlayerInteract(interact(card, EquipmentSlot.HAND));
        check(count(offhand) == 9 && storage.getItem(0).getAmount() == 1, "card consumed exactly once");

        ItemStack deduction = CreateCard.createCard(false, -1);
        check(deduction.getItemMeta().getLore().stream().anyMatch(line -> line.endsWith("减少 1 次")),
                "negative add card displays a deduction");
        storage.setItem(0, deduction);
        listener.onPlayerInteract(interact(deduction, EquipmentSlot.HAND));
        check(count(offhand) == 8, "readable negative add card deducts instead of granting unlimited mode");
        storage.setItem(0, CreateCard.createCard(true, -1));
        listener.onPlayerInteract(interact(storage.getItem(0), EquipmentSlot.HAND));
        check(count(offhand) == -1 && messages.get(messages.size() - 1).endsWith(MendCount.format(-1)),
                "set card applies unlimited mode and reports it in words");

        var completer = new CommandTabCompleter();
        var setChoices = completer.onTabComplete(player, null, "slmend", new String[]{"set", "SlowMendingTest", ""});
        check(setChoices.containsAll(List.of("无限", "无限不减速", "禁用")) && !setChoices.contains("-1"),
                "set completion suggests named states");
        var addChoices = completer.onTabComplete(player, null, "slmend", new String[]{"add", "SlowMendingTest", ""});
        check(addChoices.contains("-1") && !addChoices.contains("无限"), "add completion suggests numeric deltas");
        check(completer.onTabComplete(player, null, "slmend", new String[]{"givecard", ""}).equals(List.of("set", "add")),
                "givecard completes mode before player");
        check(completer.onTabComplete(player, null, "slmend", new String[]{"givecard", "set", "Steve", ""})
                .equals(List.of("1", "16", "64")), "givecard completes quantity after player");
        var cardSetChoices = completer.onTabComplete(player, null, "slmend", new String[]{"givecard", "SET", "Steve", "1", ""});
        check(cardSetChoices.containsAll(List.of("无限", "无限不减速", "禁用")), "givecard set completes named modes");
        var cardAddChoices = completer.onTabComplete(player, null, "slmend", new String[]{"givecard", "add", "Steve", "1", ""});
        check(cardAddChoices.contains("-1") && !cardAddChoices.contains("无限"), "givecard add only completes numeric deltas");
        new SLMCommand().onCommand(player, null, "slmend", new String[]{"help"});
        check(messages.stream().anyMatch(message -> message.contains("<次数|无限|无限不减速|禁用>")),
                "existing help configuration receives named-state instructions");
        check(messages.stream().anyMatch(message -> message.contains("givecard <set|add> <player> <数量>")),
                "help includes mode-first givecard syntax");

        meta = card.getItemMeta();
        meta.setLore(List.of("cosmetic text changed"));
        card.setItemMeta(meta);
        check(new ExpansionCard(card.getItemMeta()).isCard(), "new card independent of lore");
        ItemStack malformed = new ItemStack(Material.WRITTEN_BOOK);
        meta = malformed.getItemMeta();
        meta.setLore(List.of(ExpansionCard.identifier));
        malformed.setItemMeta(meta);
        check(!new ExpansionCard(meta).isCard(), "incomplete old card rejected");
        meta.setLore(List.of(ExpansionCard.identifier, "§e模式§f：§b增加", "§e修改次数§f：§35"));
        malformed.setItemMeta(meta);
        check(new ExpansionCard(meta).isCard(), "complete legacy card recognized");
        meta.getPersistentDataContainer().set(ExpansionCard.MODE, PersistentDataType.BYTE, (byte) 1);
        check(!new ExpansionCard(meta).isCard(), "partial new card cannot fall back to legacy");
        meta.getPersistentDataContainer().set(ExpansionCard.MODE, PersistentDataType.STRING, "wrong type");
        meta.getPersistentDataContainer().set(ExpansionCard.FREQUENCY, PersistentDataType.INTEGER, 5);
        check(!new ExpansionCard(meta).isCard(), "wrong PDC type rejected without exception");

        var wrongTypeItem = tool();
        meta = wrongTypeItem.getItemMeta();
        meta.getPersistentDataContainer().set(MendingItem.REMAINING, PersistentDataType.STRING, "5");
        wrongTypeItem.setItemMeta(meta);
        var wrongTypeEvent = mend(wrongTypeItem);
        success.onPlayerPlayerItemMend(wrongTypeEvent);
        check(wrongTypeEvent.isCancelled(), "wrong count PDC type denied without fresh quota");

        offhand = new ItemStack(Material.STONE);
        storage.setItem(0, CreateCard.createCard(false, 5));
        listener.onPlayerInteract(interact(storage.getItem(0), EquipmentSlot.HAND));
        check(storage.getItem(0).getAmount() == 1, "invalid target does not consume card");
        offhand = itemWithCount(Integer.MAX_VALUE);
        listener.onPlayerInteract(interact(storage.getItem(0), EquipmentSlot.HAND));
        check(count(offhand) == Integer.MAX_VALUE && storage.getItem(0).getAmount() == 1, "overflow rejected without consumption");

        storage.clear();
        ItemStack heldTool = tool();
        storage.setItem(0, heldTool);
        com_give.give(player, 5, false, 33);
        int delivered = 0;
        for (ItemStack slot : storage.getContents()) {
            if (slot != null && new ExpansionCard(slot.getItemMeta()).isCard()) {
                delivered += slot.getAmount();
                check(slot.getAmount() <= slot.getMaxStackSize(), "card stacks obey material limit");
            }
        }
        check(delivered == 33 && storage.getItem(0).equals(heldTool), "delivery preserves held equipment");
        for (int i = 0; i < 36; i++) storage.setItem(i, new ItemStack(Material.STONE, 64));
        try {
            com_give.give(player, 5, false, 1);
            throw new AssertionError("full inventory accepted");
        } catch (IllegalArgumentException expected) { check(true, "full inventory delivery rejected"); }
        check(storage.getItem(0).getType() == Material.STONE, "full inventory not overwritten");

        Path configPath = Slow_mending_re.getSLM().getDataFolder().toPath().resolve("config.yml");
        String original = Files.readString(configPath);
        int originalFactor = Slow_mending_re.getMitigation_Factor();
        try {
            Files.writeString(configPath, original.replace("Mitigation_Factor: 5", "Mitigation_Factor: 0"));
            try {
                loadPL.loadPlugins();
                throw new AssertionError("invalid factor accepted");
            } catch (IllegalArgumentException expected) { check(true, "bad config rejected"); }
            check(Slow_mending_re.getMitigation_Factor() == originalFactor, "failed reload preserves prior settings");
        } finally {
            Files.writeString(configPath, original);
            loadPL.loadPlugins();
        }
        Path cardPath = Slow_mending_re.getSLM().getDataFolder().toPath().resolve("ExpansionCard/cardconfig.yml");
        String originalCards = Files.readString(cardPath);
        try {
            Files.writeString(cardPath, originalCards.replace("Enable: true", "Enable: false"));
            loadPL.loadPlugins();
            offhand = itemWithCount(4);
            storage.setItem(0, CreateCard.createCard(false, 5));
            Bukkit.getPluginManager().callEvent(interact(storage.getItem(0), EquipmentSlot.HAND));
            check(count(offhand) == 4, "disable card feature on reload");
            Files.writeString(cardPath, originalCards);
            loadPL.loadPlugins();
            Bukkit.getPluginManager().callEvent(interact(storage.getItem(0), EquipmentSlot.HAND));
            check(count(offhand) == 9, "re-enable without duplicate listener registration");
        } finally {
            Files.writeString(cardPath, originalCards);
            loadPL.loadPlugins();
        }
    }

    private ItemStack tool() {
        var item = new ItemStack(Material.DIAMOND_PICKAXE);
        var meta = (Damageable) item.getItemMeta();
        meta.setDamage(100);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack itemWithCount(int value) {
        ItemStack item = tool();
        var meta = item.getItemMeta();
        MendingItem.setRemainderMendFrequency(meta, prefix, value, List.of());
        item.setItemMeta(meta);
        return item;
    }

    private int count(ItemStack item) throws Exception {
        return MendingItem.getRemainderMendFrequency(item.getItemMeta(), prefix, List.of("old:"));
    }

    private PlayerItemMendEvent mend(ItemStack item) {
        ExperienceOrb orb = proxy(ExperienceOrb.class, (method, args) -> method.equals("getExperience") ? 1 : null);
        try {
            // Modern Paper adds explicit consumed XP; old Paper exposes the five-argument constructor.
            Constructor<PlayerItemMendEvent> modern = PlayerItemMendEvent.class.getConstructor(
                    Player.class, ItemStack.class, EquipmentSlot.class, ExperienceOrb.class, int.class, int.class);
            return modern.newInstance(player, item, EquipmentSlot.HAND, orb, 2, 1);
        } catch (NoSuchMethodException oldPaper) {
            return new PlayerItemMendEvent(player, item, EquipmentSlot.HAND, orb, 2);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to construct test mending event", e);
        }
    }

    private PlayerInteractEvent interact(ItemStack item, EquipmentSlot hand) {
        return new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, item, null, BlockFace.SELF, hand);
    }

    private void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        assertions++;
        getLogger().info("PASS: " + description);
    }

    @FunctionalInterface private interface Invoke { Object call(String method, Object[] args); }
    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invoke invoke) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (object, method, args) -> {
            if (method.getName().equals("toString")) return "Test" + type.getSimpleName();
            if (method.getName().equals("hashCode")) return System.identityHashCode(object);
            if (method.getName().equals("equals")) return object == args[0];
            Object result = invoke.call(method.getName(), args);
            if (result != null || !method.getReturnType().isPrimitive() || method.getReturnType() == void.class) return result;
            if (method.getReturnType() == boolean.class) return false;
            if (method.getReturnType() == long.class) return 0L;
            if (method.getReturnType() == double.class) return 0.0;
            if (method.getReturnType() == float.class) return 0.0f;
            return 0;
        });
    }
}
