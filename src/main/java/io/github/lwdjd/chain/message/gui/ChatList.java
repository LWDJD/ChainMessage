package io.github.lwdjd.chain.message.gui;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatList {
    /**
     *  聊天列表
     *  Map中有三种数据
     *      chatType:
     *          value:0私聊
     *          value:1加密私聊
     *          value:2群聊
     *          value:3加密群聊
     *      address: 地址
     *      chatName：聊天名称
     */
    private static Set<Map<String,String>> chatList;
    /**
     * 聊天数据
     * Map
     *      key:聊天地址
     *          value:
     *              list:按照时间排序的聊天数据
     *                  Map:交易内容
     *
     */
    private static Map<String, List<Map<String,Object>>> chatData;

    /**
     * 获取聊天数据
     * @return
     */
    public static Map<String, List<Map<String,Object>>> getChatData() {
        return chatData;
    }

    /**
     * 添加聊天数据
     * @param address 聊天地址
     * @param list 需要添加的聊天数据
     */
    public static void addChatData(String address, List<Map<String,Object>> list) {
        if(chatData.get(address) == null){
            chatData.put(address, list);
            return;
        }else {
            for (Map<String,Object> messgae1 : chatData.get(address)){
                String messageHash1 = messgae1.get("hash").toString();
                for (Map<String,Object> messgae2 : list){
                    String messageHash2 = messgae2.get("hash").toString();
                    if (messageHash2.equals(messageHash1)){
                        list.remove(messgae2);
                    }
                }
            }
            chatData.put(address, list);
        }
    }

    /**
     * 设置聊天数据（通常用作初始化时读取数据）
     * @param chatData
     */
    public static void setChatData(Map<String, List<Map<String,Object>>> chatData) {
        ChatList.chatData = chatData;
    }

    public static Set<Map<String,String>> getChatList(){
        return  chatList;
    }

    /**
     * 设置聊天列表（通常用作初始化时读取数据）
     * @param chatList
     */
    public static void setChatList(Set<Map<String,String>> chatList){
        ChatList.chatList = chatList;
    }
    public static Boolean remove(String address){
        for(Map<String, String> i:chatList){
            if (i.get("address").equals(address)){
                chatList.remove(i);
                return true;
            }
        }
        return false;
    }
    public static void add(Map<String, String> chat){
        ChatList.chatList.add(chat);
    }
    public static void addAll(Set<Map<String, String>> chatList){
        ChatList.chatList.addAll(chatList);
    }
    public static void main(String[] args) {

    }
}
