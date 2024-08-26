package io.github.lwdjd.chain.message.chat;

import java.util.ArrayList;
import java.util.List;

public class Sender {
    private String address;
    private String name;
    private String remarkName;
    private String avatarPath; // 头像图片路径
    private static List<Sender> savedSenderList = new ArrayList<>();

    public boolean equals(Sender sender){
        return this.address.equals(sender.address);
    }

    public Sender(String address, String name){
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public static List<Sender> getsavedSenderList() {
        return savedSenderList;
    }

    public static void setsavedSenderList(List<Sender> savedSenderList) {
        Sender.savedSenderList = savedSenderList;
    }
}
