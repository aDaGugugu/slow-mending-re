package org.sbbpl.plugins.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.LinkedList;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> com = new LinkedList<>();
        if (strings.length == 1) {
            com.add("help");
            com.add("version");
            com.add("set");
            com.add("reload");
            com.add("add");
            com.add("givecard");
            return com;
        }
        if (strings.length == 2){
            switch (strings[0]){
                case ("set") ,("add"), ("givecard") ->{
                    return null;
                }
            }
        }
        if (strings.length == 3){
            switch (strings[0]){
                case ("set") ,("add")->{
                    com.add("<次数>");
                    return com;
                }
                case ("givecard") ->{
                    com.add("<数量>");
                    return com;
                }
            }
        }
        if (strings.length == 4){
            switch (strings[0]){
                case ("set") ,("add") ->{
                    com.add("off");
                    com.add("main");
                    return com;
                }
                case ("givecard") ->{
                    com.add("<次数>");
                    return com;
                }
            }
        }
        if (strings.length == 5){
            switch (strings[0]){
                case ("givecard") ->{
                    com.add("set");
                    com.add("add");
                    return com;
                }
            }
        }
        return null;
    }
}
