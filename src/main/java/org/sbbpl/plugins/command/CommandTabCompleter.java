package org.sbbpl.plugins.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.sbbpl.plugins.Slow_mending_re;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class CommandTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("slowmending.command") || args.length == 0) return List.of();
        String sub = args[0].toLowerCase(Locale.ROOT);
        List<String> choices = List.of();
        if (args.length == 1) choices = List.of("help", "version", "set", "add", "info", "reload", "givecard");
        else if (sub.equals("givecard")) {
            if (args.length == 2) choices = List.of("set", "add");
            else if (GiveCardArguments.isMode(args[1])) {
                choices = switch (args.length) {
                    case 3 -> playerNames();
                    case 4 -> List.of("1", "16", "64");
                    case 5 -> args[1].equalsIgnoreCase("set") ? settingChoices() : deltaChoices();
                    default -> List.of();
                };
            }
        } else if (args.length == 2 && List.of("set", "add", "info").contains(sub)) {
            choices = playerNames();
        } else if ((args.length == 3 && sub.equals("info"))
                || (args.length == 4 && List.of("set", "add").contains(sub))) choices = List.of("main", "off");
        else if (args.length == 3 && sub.equals("add")) choices = deltaChoices();
        else if (args.length == 3 && sub.equals("set")) choices = settingChoices();
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return choices.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix)).distinct().toList();
    }

    private static List<String> playerNames() {
        return Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).toList();
    }

    private static List<String> deltaChoices() {
        return List.of("1", "10", "100", "-1", "-10");
    }

    private static List<String> settingChoices() {
        var choices = new ArrayList<>(List.of("0", "1", "100", "无限", "无限不减速", "禁用",
                "unlimited", "unlimited-fast", "disabled"));
        int initial = Slow_mending_re.getMax_Mend_Limit_Number();
        if (initial > 0) choices.add(Integer.toString(initial));
        return choices;
    }
}
