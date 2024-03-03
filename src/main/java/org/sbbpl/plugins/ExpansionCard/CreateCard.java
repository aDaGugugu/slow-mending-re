package org.sbbpl.plugins.ExpansionCard;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sbbpl.plugins.Slow_mending_re;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.sbbpl.plugins.ExpansionCard.ExpansionCard.identifier;

public class CreateCard {
    private static Slow_mending_re SLM = Slow_mending_re.getSLM();
    public static ItemStack createCard(boolean isSet, int num){
        //读取配置
        File configfile = new File(SLM.getDataFolder(), "ExpansionCard/cardinfo.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configfile);

        String name = config.getString("text.name");
        List<String> useway = config.getStringList("text.useway");
        String set_mode = config.getString("text.set_mode");
        String add_mode = config.getString("text.add_mode");
        String frequency = config.getString("text.frequency");


        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        //写入


        //名称
        assert itemMeta != null;
        itemMeta.setDisplayName(name);
        //lore
        List<String> lores = new LinkedList<>();
        //识别符号
        lores.add(identifier);
        //使用方法
        lores.addAll(useway);
        //卡片模式
        if (isSet) {
            lores.add(set_mode);
        }else {
            lores.add(add_mode);
        }
        //修改次数
        lores.add(frequency+num);


        //打入物品信息
        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
