package io.github.lwdjd.chain.message.account;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.lwdjd.chain.message.config.ConfigManager;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class Account {
    private String cryptographicPrivateKey;
    private String publicKey;
    private String address;
    private String name;
    private String verificationCode;
    private SecretKey key;
    private String headPortrait;
    private static List<Account> accountsList = new ArrayList<>();

    /**
     * 账户的构造函数（通常用作初始化时从文件读取数据）
     * @param cryptographicPrivateKey 加密后私钥
     * @param name 用户的名称
     * @param verificationCode 验证码
     */
    public Account(String cryptographicPrivateKey,String publicKey,String address,String name,String verificationCode){
        this.cryptographicPrivateKey = cryptographicPrivateKey;
        this.publicKey = publicKey;
        this.address = address;
        this.name = name;
        this.verificationCode = verificationCode;
        this.key = null;
    }
    public Account(){}

    /**
     * 验证密码是否正确
     * @param password 需要被验证的密码
     * @return 是否验证成功
     */
    public boolean verification(String password) {
        try {
            return decrypt(verificationCode, generateKeyFromPassword(password)).equals(name);
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
            return decrypt(verificationCode,key).equals(name);
        }catch (Exception e){
            return false;
        }

    }

    /**
     * 重命名
     * @param password 需要使用密码
     * @param newName 新的名字
     * @return 是否重命名成功
     */
    public boolean reName(String password,String newName){
        try {
            if (verification(password)) {
                verificationCode = encrypt(newName, generateKeyFromPassword(password));
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
                cryptographicPrivateKey = encrypt(decrypt(cryptographicPrivateKey, generateKeyFromPassword(password)), generateKeyFromPassword(newPassword));
                verificationCode = encrypt(name, generateKeyFromPassword(newPassword));
                return true;
            } else {
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    public String getName(){
        return name;
    }

    /**
     * 解锁账户
     * @param password 密码
     * @return 是否解锁成功
     */
    public boolean unlockAccount(String password){
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
     * 锁定账户
     */
    public void lockAccount(){
        key = null;
    }

    /**
     * 获取原始私钥
     * @return 返回原始私钥（null为获取失败，可能是没有解锁账户或账户被重新锁定）
     * @throws Exception
     */
    public String getPrivateKey() {
        if (key != null && verificationKey()){
            try {
                return decrypt(cryptographicPrivateKey,key);
            } catch (Exception e) {
                return null;
            }
        }else {
            return null;
        }
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

    public String getPublicKey() {
        return publicKey;
    }

    public String getAddress() {
        return address;
    }

    /**
     * 创建账户
     * @param privateKey 输入原始私钥
     * @param name 输入名称
     * @param password 设置密码
     * @return 返回Account账户
     */
    public static Account crateAccount(String privateKey,String name,String password)  {
        try {
            //获取私钥的 ECKeyPair
            ECKeyPair keyPair =ECKeyPair.create(Numeric.toBigInt(privateKey));
            // 获取公钥字符串
            String publicKey = Numeric.toHexStringNoPrefix(keyPair.getPublicKey());
            // 将公钥转换为地址
            String address = Keys.getAddress(keyPair);
            return  new Account(
                    encrypt(privateKey,generateKeyFromPassword(password)),
                    publicKey,
                    address,
                    name,
                    encrypt(name,generateKeyFromPassword(password))
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static void addAccount(Account account){
        accountsList.add(account);
    }
    public static void setAccountList(List<Account> accountList){
        accountsList=accountList;
    }
    public static List<Account> getAccountList(){
        return accountsList;
    }

    /**
     * 保存当前的账户列表到磁盘
     * @return 返回是否保存成功
     */
    public static boolean saveAccountListOfDisk(){
        try {
            JSONArray accounts = new JSONArray();
            for (Account account : accountsList) {
                JSONObject account_2 = new JSONObject();
                account_2.put("cryptographicPrivateKey", account.cryptographicPrivateKey);
                account_2.put("publicKey", account.publicKey);
                account_2.put("address", account.address);
                account_2.put("name", account.name);
                account_2.put("verificationCode", account.verificationCode);
                account_2.put("headPortrait", account.headPortrait);
                accounts.add(account_2);
            }
            JSONObject accounts_json = new JSONObject();
            accounts_json.put("data", accounts);
            ConfigManager.saveConfig("accounts.json", accounts_json);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean loadAccountListFromDisk(){
        try {
            ConfigManager.loadConfig("accounts.json");
            JSONObject accounts_json = ConfigManager.getConfig("accounts.json");
            JSONArray accounts = accounts_json.getJSONArray("data");
            List<Account> accounts_memory = new ArrayList<>();
            for (int i = 0; i < accounts.size(); i++) {
                JSONObject account_json = accounts.getJSONObject(i);
                Account account = new Account();
                account.cryptographicPrivateKey = account_json.getString("cryptographicPrivateKey");
                account.publicKey = account_json.getString("publicKey");
                account.address = account_json.getString("address");
                account.name = account_json.getString("name");
                account.verificationCode = account_json.getString("verificationCode");
                account.headPortrait = account_json.getString("headPortrait");
                accounts_memory.add(account);
            }
            Account.accountsList = accounts_memory;
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
    /**
     * 生成一个随机的私钥。
     *
     * @return 私钥的十六进制字符串表示。
     */
    public static String generatePrivateKey() {
        // 创建一个SecureRandom实例来生成安全的随机数
        SecureRandom random = new SecureRandom();

        // 生成一个256位的随机数作为私钥
        byte[] privateKeyBytes = new byte[32]; // 256位 / 8位每字节 = 32字节
        random.nextBytes(privateKeyBytes);

        // 将字节数组转换为十六进制字符串
        StringBuilder sb = new StringBuilder(privateKeyBytes.length * 2);
        for (byte b : privateKeyBytes) {
            sb.append(String.format("%02x", b));
        }

        // 返回十六进制表示的私钥
        return sb.toString();
    }
//单元测试，不用管他
    public static void main(String[] args) throws Exception {
//        try {
//            String password = "userStrongPassword"; // 用户密码
//            SecretKey key = generateKeyFromPassword(password); // 从密码生成密钥
//            System.out.println("Key: " + key.getFormat());
//            // 加密解密示例
//
//            String data = "Secret data that needs to be encrypted";
//            String encryptedData = encrypt(data, key); // 加密数据
//            System.out.println("Encrypted: " + encryptedData);
//
//            String decryptedData = decrypt(encryptedData, key); // 解密数据
//            System.out.println("Decrypted: " + decryptedData);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Scanner  scanner = new Scanner(System.in);
        System.out.print("请输入密码:");
        String password = scanner.nextLine();
        System.out.print("请输入名称:");
        String newName = scanner.nextLine();

        Account account = crateAccount("0x100a1a48cd4b4f8",newName,password);
        System.out.println("name："+account.getName()+"\n");
        System.out.print("请输入密码:");
        String password_2 = scanner.nextLine();
        System.out.println("是否解锁成功:"+account.unlockAccount(password_2));
        System.out.print("私钥："+account.getPrivateKey());

//        System.out.print("请输入密码:");
//        String password_2 = scanner.nextLine();
//        System.out.print("请输入名称:");
//        String newName_2 = scanner.nextLine();
//        System.out.print("请输入新密码:");
//        String password_3 = scanner.nextLine();
//        Boolean re = account.reName(password_2,newName_2);
//        System.out.println("name："+account.getName()+"\n"+"是否修改成功："+re);
//        System.out.println("修改密码是否成功:"+account.rePassword(password_2,password_3));
//        System.out.print("验证密码:");
//
//        System.out.println("密码是否正确:"+account.verification(scanner.nextLine()));
    }
}
