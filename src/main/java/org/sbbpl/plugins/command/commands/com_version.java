package org.sbbpl.plugins.command.commands;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.sbbpl.plugins.Slow_mending_re;

import java.io.File;
import java.util.List;

public class com_version {
    public static void playerVersion(Player player) {
        Slow_mending_re SLM = Slow_mending_re.getSLM();
        File comconfigfile = new File(SLM.getDataFolder(), "command.yml");
        YamlConfiguration comconfig = YamlConfiguration.loadConfiguration(comconfigfile);
        List<String> version = comconfig.getStringList("Command.text.version");
        for (String text : version) {
            player.sendMessage(text);
        }
    }
    public static void consVersion() {
        Slow_mending_re SLM = Slow_mending_re.getSLM();
        File comconfigfile = new File(SLM.getDataFolder(), "command.yml");
        YamlConfiguration comconfig = YamlConfiguration.loadConfiguration(comconfigfile);
        List<String> version = comconfig.getStringList("Command.text.version");
        System.out.println(version);
    }
}
