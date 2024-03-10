package org.sbbpl.plugins.command.commands;

import org.bukkit.entity.Player;
import org.sbbpl.plugins.Slow_mending_re;
import org.sbbpl.plugins.loadPL;

import java.util.function.Supplier;

public class com_reload{
    public static void playerReload(Player player){
        Slow_mending_re SLM = Slow_mending_re.getSLM();
        SLM.getLogger().info("正在重载插件...");
        player.sendMessage("§b正在重载插件...");
        try {
            SLM.saveDefaultConfig();
            SLM.saveResource("command.yml", false);
            SLM.saveResource("ExpansionCard/cardinfo.yml", false);
            SLM.saveResource("ExpansionCard/cardconfig.yml", false);
            SLM.reloadConfig();
            loadPL.loadPlugins();
            SLM.getLogger().info("重载完毕！");
            player.sendMessage("§b重载完毕！");
        } catch (Exception e) {
            SLM.getLogger().warning("重载失败！");
            SLM.getLogger().warning((Supplier<String>) e);
            player.sendMessage("§c重载失败！");
        }
    }
    public static void consReload(){
        Slow_mending_re SLM = Slow_mending_re.getSLM();
        SLM.getLogger().info("正在重载插件...");
        try {
            SLM.saveDefaultConfig();
            SLM.saveResource("command.yml", false);
            SLM.saveResource("ExpansionCard/cardinfo.yml", false);
            SLM.saveResource("ExpansionCard/cardconfig.yml", false);
            SLM.reloadConfig();
            loadPL.loadPlugins();
            SLM.getLogger().info("重载完毕！");
        } catch (Exception e) {
            SLM.getLogger().warning("重载失败！");
            SLM.getLogger().warning((Supplier<String>) e);
        }
    }
}
