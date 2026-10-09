package org.sbbpl.plugins.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.sbbpl.plugins.MendCount;
import org.sbbpl.plugins.MendingItem;
import org.sbbpl.plugins.Slow_mending_re;
import org.sbbpl.plugins.command.commands.com_give;
import org.sbbpl.plugins.loadPL;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

public class SLMCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("slowmending.command")) {
            sender.sendMessage("§c你没有权限！");
            return true;
        }
        String sub = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
        Slow_mending_re plugin = Slow_mending_re.getSLM();
        try {
            switch (sub) {
                case "reload" -> {
                    require(args.length == 1, "/slmend reload");
                    try {
                        plugin.ensureResources();
                        loadPL.loadPlugins();
                        plugin.reloadConfig();
                        sender.sendMessage("§b配置已重载。");
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "重载失败，保留之前的有效配置。", e);
                        sender.sendMessage("§c重载失败，保留之前的有效配置：" + e.getMessage());
                    }
                }
                case "version" -> sender.sendMessage("§bSlow Mending Re " + plugin.getDescription().getVersion()
                        + " | Paper 1.20–26.3 | 原作者 super_boy_520");
                case "set", "add", "info" -> edit(sender, sub, args);
                case "givecard" -> {
                    GiveCardArguments card = GiveCardArguments.parse(args);
                    Player target = player(card.playerName());
                    com_give.give(target, card.frequency(), card.setMode(), card.quantity());
                    sender.sendMessage("§b已给予 " + target.getName() + " " + card.quantity() + " 张拓展卡："
                            + (card.setMode() ? "设置为 " + MendCount.format(card.frequency())
                            : MendCount.formatDelta(card.frequency())) + "。");
                }
                default -> {
                    var config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "command.yml"));
                    for (String line : config.getStringList("Command.text.help")) sender.sendMessage(line);
                    // Include this for existing command.yml files, which are intentionally not overwritten.
                    sender.sendMessage("§b/slmend info <player> [main|off] - 查看修补次数及模式。");
                    sender.sendMessage("§b/slmend set <player> <次数|无限|无限不减速|禁用> [main|off]");
                    sender.sendMessage("§b" + GiveCardArguments.USAGE + " - 发放拓展卡，先选择 set 或 add。");
                    sender.sendMessage("§bset 模式的拓展卡也支持上述名称；add 只接受增减整数，例如 -1 表示减少 1 次。");
                }
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数量及 add 增减量必须为 32 位整数；只有 set 支持修补模式名称。");
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c" + e.getMessage());
        }
        return true;
    }

    private void edit(CommandSender sender, String mode, String[] args) {
        boolean info = mode.equals("info");
        int base = info ? 2 : 3;
        require(args.length == base || args.length == base + 1,
                "/slmend " + mode + " <player> " + (info ? "" : mode.equals("set")
                        ? "<次数|无限|无限不减速|禁用> " : "<增减次数> ") + "[main|off]");
        Player target = player(args[1]);
        String hand = args.length > base ? args[base].toLowerCase(Locale.ROOT) : "main";
        require(hand.equals("main") || hand.equals("off"), "手持位置必须为 main 或 off。");
        ItemStack item = hand.equals("main") ? target.getInventory().getItemInMainHand() : target.getInventory().getItemInOffHand();
        var meta = item.getItemMeta();
        require(meta instanceof Damageable, "目标手中必须是具有耐久度的装备。");
        String prefix = Slow_mending_re.getMend_Frequency_Lore_Name();
        var oldNames = Slow_mending_re.getOld_Mend_Frequency_Lore_Name();
        int value;
        if (mode.equals("set")) {
            value = MendCount.parseSetting(args[2]);
        } else {
            try {
                value = MendingItem.getRemainderMendFrequency(meta, prefix, oldNames);
            } catch (NoSuchFieldException e) {
                value = Slow_mending_re.getMax_Mend_Limit_Number();
            }
            if (!info) value = MendCount.add(value, Integer.parseInt(args[2]));
        }
        if (!info) {
            MendingItem.setRemainderMendFrequency(meta, prefix, value, oldNames);
            item.setItemMeta(meta);
            if (hand.equals("main")) target.getInventory().setItemInMainHand(item);
            else target.getInventory().setItemInOffHand(item);
        }
        sender.sendMessage("§b" + target.getName() + " " + (hand.equals("main") ? "主手" : "副手")
                + " 剩余修补次数：" + MendCount.format(value));
    }

    private static Player player(String name) {
        Player player = Bukkit.getPlayerExact(name);
        require(player != null, "未找到在线玩家：" + name);
        return player;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
