package org.sbbpl.plugins.command.commands;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.sbbpl.plugins.Slow_mending_re;

import java.io.File;
import java.util.List;

public class com_help {

    public static void playerHelp(Player player){
        Slow_mending_re SLM = Slow_mending_re.getSLM();
        File comconfigfile = new File(SLM.getDataFolder(), "command.yml");
        YamlConfiguration comconfig = YamlConfiguration.loadConfiguration(comconfigfile);
        List<String> help = comconfig.getStringList("Command.text.help");
        for (String text : help) {
            player.sendMessage(text);
        }
    }

    public static void consHelp(){
        Slow_mending_re SLM = Slow_mending_re.getSLM();
        File comconfigfile = new File(SLM.getDataFolder(), "command.yml");
        YamlConfiguration comconfig = YamlConfiguration.loadConfiguration(comconfigfile);
        List<String> help = comconfig.getStringList("Command.text.help");
        System.out.println(help);
    }
}
