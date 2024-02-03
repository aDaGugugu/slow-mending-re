package org.sbbpl.plugins;

import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedList;
import java.util.List;

public class MendingItem {
    //这是一个提供关于lore存储的数据的函数的类

    //获取剩余修补次数
    public static int getRemainderMendFrequency(ItemMeta itemMeta ,String mendFrequencyLoreName ,List<String> oldNames) throws NoSuchFieldException {
        //判断是否存在lore
        if (!itemMeta.hasLore()) throw new NoSuchFieldException();
        //获取lore列表
        List<String> rawLore = itemMeta.getLore();
        //遍历列表，找到存储mend数据的lore
        int frequency;//定义数据变量
        for (String lore : rawLore){
            System.out.println(oldNames);
            //寻找名称
            if (lore.startsWith(mendFrequencyLoreName)){//找到
                //截取数据并进行转换
                frequency = Integer.parseInt(lore.substring(mendFrequencyLoreName.length()));
                return frequency;
            }
            //寻找旧名称
            for (String oldname : oldNames){
                System.out.println(oldname);
                if (lore.startsWith(oldname)){//找到
                    //读取名称
                    frequency = Integer.parseInt(lore.substring(oldname.length()));
                    return frequency;
                }
            }
        }
        throw new NoSuchFieldException();//未找到对应lore
    }

    //创建一个lore用于存储剩余次数并赋值
    public static ItemMeta createRemainderMendFrequency(ItemMeta itemMeta ,String mendFrequencyLoreName,int initialFrequency){
        List<String> lore;
        //判断是否存在lore
        //获取lore列表
        if (!itemMeta.hasLore()) lore = new LinkedList<>();
        else lore = itemMeta.getLore();
        //在列表最后插入用于存储的lore
        lore.add(mendFrequencyLoreName+initialFrequency);
        //赋值给itemmate
        itemMeta.setLore(lore);
        return itemMeta;
    }

    //将剩余次数设置为一个数
    public static ItemMeta setRemainderMendFrequency(ItemMeta itemMeta ,String mendFrequencyLoreName ,int setNum ,List<String> oldNames) throws NoSuchFieldException {
        //判断是否存在lore
        if (!itemMeta.hasLore()) throw new NoSuchFieldException();
        //获取lore列表
        List<String> rawLore = itemMeta.getLore();
        System.out.println("rawl:"+rawLore);
        //遍历列表，找到存储mend数据的lore
        for (int i = 0 ; i <= rawLore.size() ;i++){
            System.out.println("i:"+i);
            String lore = rawLore.get(i);
            System.out.println("lore:"+lore);
            if (lore.startsWith(mendFrequencyLoreName)){//找到
                //构建新lore
                String newlore;
                newlore = lore.substring(0,mendFrequencyLoreName.length())+setNum;
                //替换原lore
                rawLore.set(i,newlore);
                itemMeta.setLore(rawLore);
                return itemMeta;
            }
            //寻找旧名称
            for (String oldname : oldNames){
                System.out.println(oldname);
                if (lore.startsWith(oldname)){//找到
                    //删除原名称
                    rawLore.remove(i);
                    itemMeta.setLore(rawLore);
                    return createRemainderMendFrequency(itemMeta,mendFrequencyLoreName,setNum);

                }
            }
        }
        throw new NoSuchFieldException();//未找到对应lore
    }

    //将剩余次数加（减）去一个数
    public static ItemMeta addRemainderMendFrequency(ItemMeta itemMeta ,String mendFrequencyLoreName ,int addNum, List<String> oldName) throws NoSuchFieldException {
        int num = getRemainderMendFrequency(itemMeta,mendFrequencyLoreName,oldName);
        num += addNum;
        return setRemainderMendFrequency(itemMeta,mendFrequencyLoreName,num,oldName);
    }

    //将玩家的装备转换为破损状态
    public static ItemMeta changeBroken(ItemMeta itemMeta, Player player, boolean changeName, String brokePrefix,boolean sendmsg, String brokenMessage){
        //发送破损信息
        if (sendmsg) player.sendMessage(brokenMessage);

        //判断玩家是否更改工具的名称
        if (changeName){
            if (!itemMeta.hasDisplayName()) {//如果没有
                String toolName = brokePrefix + "工具";
                itemMeta.setDisplayName(toolName);
                return itemMeta;
            }

            //获取名称并添加前缀
            String toolName = itemMeta.getDisplayName();
            toolName = brokePrefix + toolName;
            itemMeta.setDisplayName(toolName);
            return itemMeta;
        }
        return itemMeta;
    }

    public static boolean checkWhitelist(ItemMeta itemMeta ,List<String> name){
        return false;
    }
}
