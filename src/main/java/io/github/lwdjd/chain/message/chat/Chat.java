package io.github.lwdjd.chain.message.chat;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.lwdjd.chain.message.config.ConfigManager;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Chat{
    /**
     *PRIVATE_CHAT 私聊
     *CRY_PRIVATE_CHAT 加密私聊
     *PUBLIC_CHAT 群聊
     *CRY_PUBLIC_CHAT 加密群聊
     **/
    public enum  ChatType {
        PRIVATE_CHAT("私聊"),CRY_PRIVATE_CHAT("加密私聊"),PUBLIC_CHAT("频道"),CRY_PUBLIC_CHAT("加密频道");

        private final String displayName;

        ChatType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum Chain{
        OKTC_TEST("OKTC test","okexchain_test");
        private final String displayName;
        private final String displayID;

        Chain(String displayName,String displayID) {
            this.displayName = displayName;
            this.displayID = displayID;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public String getDisplayID(){
            return displayID;
        }
    }
    private String cryptographicAddress;
    private String remarkName;
    private String verificationCode;
    private SecretKey key;
    private String headPortrait;
    private ChatType chatType;
    private Chain chain;
    private static List<Chat> chatList = new ArrayList<>();

    /**
     * 账户的构造函数（通常用作初始化时从文件读取数据）
     * @param cryptographicAddress 加密后地址
     * @param remarkName 地址的备注名
     * @param verificationCode 验证码
     */
    public Chat(String cryptographicAddress,ChatType chatType,Chain chain,String remarkName,String verificationCode){
        this.cryptographicAddress = cryptographicAddress;
        this.remarkName = remarkName;
        this.verificationCode = verificationCode;
        this.key = null;
        this.chatType = chatType;
        this.chain = chain;
    }
    public Chat(){}

    /**
     * 验证密码是否正确
     * @param password 需要被验证的密码
     * @return 是否验证成功
     */
    public boolean verification(String password) {
        try {
            return decrypt(verificationCode, generateKeyFromPassword(password)).equals(remarkName);
        }catch (Exception e){
            return false;
        }

    }
    /**
     * 验证key是否正确
     * @return 是否验证成功
     */
    public boolean verificationKey() {
        try {
            return decrypt(verificationCode,key).equals(remarkName);
        }catch (Exception e){
            return false;
        }

    }

    /**
     * 重命名
     * @param password 需要使用密码
     * @param newRemarkName 新的名字
     * @return 是否重命名成功
     */
    public boolean reRemarkName(String password,String newRemarkName){
        try {
            if (verification(password)) {
                verificationCode = encrypt(newRemarkName, generateKeyFromPassword(password));
                return true;
            } else {
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 修改密码
     * @param password 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    public boolean rePassword(String password,String newPassword) {
        try {
            if (verification(password)) {
                cryptographicAddress = encrypt(decrypt(cryptographicAddress, generateKeyFromPassword(password)), generateKeyFromPassword(newPassword));
                verificationCode = encrypt(remarkName, generateKeyFromPassword(newPassword));
                return true;
            } else {
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    public String getRemarkName(){
        return remarkName;
    }

    /**
     * 解锁聊天
     * @param password 密码
     * @return 是否解锁成功
     */
    public boolean unlockChat(String password){
        if (verification(password)){
            try {
                key = generateKeyFromPassword(password);
            } catch (NoSuchAlgorithmException e) {
                return false;
            }
            return true;
        }else {
            return false;
        }
    }

    /**
     * 锁定聊天
     */
    public void lockChat(){
        key = null;
    }

    /**
     * 获取原始地址
     * @return 返回原始地址（null为获取失败，可能是没有解锁聊天或聊天被重新锁定）
     */
    public String getAddress() {
        if (key != null && verificationKey()){
            try {
                return decrypt(cryptographicAddress,key);
            } catch (Exception e) {
                return null;
            }
        }else {
            return null;
        }
    }
    public String getCryptographicAddress(){
        return cryptographicAddress;
    }

    /**
     * 设置头像
     * @param headPortrait 头像的绝对路径或相对路径，或类路径
     */
    public void setHeadPortrait(String headPortrait){
        this.headPortrait = headPortrait;
    }

    /**
     * 获取头像所在路径
     * @return 返回头像所在路径
     */
    public String getHeadPortrait(){
        return headPortrait;
    }

    public ChatType getChatType(){
        return chatType;
    }

    public Chain getChain(){
        return chain;
    }

    public SecretKey getKey() {
        return key;
    }

    /**
     * 创建账户
     * @param address 输入原始私钥
     * @param remarkName 输入名称
     * @param password 设置密码
     * @return 返回Chat聊天
     */
    public static Chat crateChat(String address,ChatType chatType,Chain chain, String remarkName, String password)  {
        if(!(address.startsWith("0x") && address.length() == 42)){
            return null;
        }
        try {
            return  new Chat(
                    encrypt(address,generateKeyFromPassword(password)),
                    chatType,
                    chain,
                    remarkName,
                    encrypt(remarkName,generateKeyFromPassword(password))
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static void addChat(Chat chat){
        chatList.add(chat);
    }
    public static void setChatList(List<Chat> chatList){
        Chat.chatList = chatList;
    }
    public static List<Chat> getChatList(){
        return chatList;
    }

    /**
     * 保存当前的聊天列表到磁盘
     * @return 返回是否保存成功
     */
    public static boolean saveChatListOfDisk(){
        try {
            JSONArray chats = new JSONArray();
            for (Chat chat : chatList) {
                JSONObject chat_2 = new JSONObject();
                chat_2.put("cryptographicAddress", chat.cryptographicAddress);
                chat_2.put("name", chat.remarkName);
                chat_2.put("verificationCode", chat.verificationCode);
                chat_2.put("headPortrait", chat.headPortrait);
                chat_2.put("chatType", chat.chatType);
                chat_2.put("chain",chat.chain);
                chats.add(chat_2);
            }
            JSONObject chat_json = new JSONObject();
            chat_json.put("data", chats);
            ConfigManager.saveConfig("chats.json", chat_json);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean loadChatListFromDisk(){
        try {
            ConfigManager.loadConfig("chats.json");
            JSONObject accounts_json = ConfigManager.getConfig("chats.json");
            JSONArray chats = accounts_json.getJSONArray("data");
            List<Chat> chats_memory = new ArrayList<>();
            for (int i = 0; i < chats.size(); i++) {
                JSONObject chat_json = chats.getJSONObject(i);
                Chat chat = new Chat();
                chat.cryptographicAddress = chat_json.getString("cryptographicAddress");
                chat.remarkName = chat_json.getString("name");
                chat.verificationCode = chat_json.getString("verificationCode");
                chat.headPortrait = chat_json.getString("headPortrait");
                try {
                    chat.chatType = ChatType.valueOf(chat_json.getString("chatType"));
                }catch (Exception e){
                    e.printStackTrace();
                    chat.chatType = null;
                }
                try {
                    chat.chain = Chain.valueOf(chat_json.getString("chain"));
                }catch (Exception e){
                    e.printStackTrace();
                    chat.chain = null;
                }
                chats_memory.add(chat);
            }
            chatList = chats_memory;
            return true;
        }catch (Exception e){
            return false;
        }
    }


    /**
     * 使用密码生成SHA-256哈希值作为AES密钥
     * @param password 密码
     * @return 返回SecretKey密钥
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateKeyFromPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] hashedPassword = digest.digest(passwordBytes);
        return new SecretKeySpec(hashedPassword, "AES");
    }

    /**
     * 加密方法
     * @param data 原始数据
     * @param key SecretKey密钥
     * @return 加密后的数据
     * @throws Exception
     */
    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 解密方法
     * @param encryptedData 加密数据
     * @param key SecretKey密钥
     * @return 原始数据
     * @throws Exception 解密失败会抛出异常，记得处理哦！
     */
    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedEncryptedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedEncryptedData);
        return new String(decryptedData);
    }

}
