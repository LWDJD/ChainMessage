package io.github.lwdjd.chain.message.fxml;

import io.github.lwdjd.chain.message.account.Account;
import io.github.lwdjd.chain.message.chat.Chat;
import io.github.lwdjd.chain.message.config.ConfigManager;

public class GuiStart {
    public static void main(String[] args){
        System.out.println("config.json文件是否加载成功:"+ConfigManager.loadConfig("config.json"));
        System.out.println("accounts.json文件是否加载成功:"+ Account.loadAccountListFromDisk());
        System.out.println("chats.json文件是否加载成功:"+ Chat.loadChatListFromDisk());
        ChainMassageGui.main(args);
    }
}
