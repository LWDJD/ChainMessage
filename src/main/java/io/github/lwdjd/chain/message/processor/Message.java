package io.github.lwdjd.chain.message.processor;

import io.github.lwdjd.chain.message.chat.Chat;
import io.github.lwdjd.chain.message.chat.ChatMessage;
import io.github.lwdjd.chain.message.config.ConfigManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    private static final Lock cryptographicChatMapTxHashListLock = new ReentrantLock(); // 显式锁
    /**
     * Map<String, List<String[0]>>是哈希
     * Map<String, List<String[1]>>是时间
     */
    private static Map<String, List<String[]>> cryptographicChatMapTxHashList = new HashMap<>();
    private static final Lock threadsLock = new ReentrantLock(); // 显式锁
    private static Map<String,Thread> threads =new HashMap<>();
//    public static String hexToUtf8String(String hexStr) {
//        // 去除字符串开头的"0x"或"0X"
//        hexStr = hexStr.replaceAll("0x|0X", "").trim();
//        // 将字符串转换为小写以统一处理
//        hexStr = hexStr.toLowerCase();
//
//        // 处理中文字符，因为中文字符可能占用多个字节
//        int len = hexStr.length();
//        int start = 0;
//        StringBuilder sb = new StringBuilder();
//        while (start < len) {
//            if (hexStr.startsWith("00", start) && (start + 4 <= len)) {
//                // 跳过UTF-8编码中的00字节
//                start += 4;
//            } else {
//                // 每两个字符为一组，表示一个字节
//                String byteString = hexStr.substring(start, start + 2);
//                int byteValue = Integer.parseInt(byteString, 16);
//                sb.append((char) byteValue);
//                start += 2;
//            }
//        }
//        return sb.toString();
//    }
    /**
     * 将16进制字符串转换为UTF-8字符串。
     *
     * @param hexString 16进制字符串
     * @return UTF-8 编码的字符串
     */
    public static String hexToUtf8(String hexString) {
        // 去除字符串中的空格和0x
        hexString = hexString.replaceAll(" ", "");
        hexString = hexString.replaceAll("0X","");
        hexString = hexString.replaceAll("0x","");
        // 确保16进制字符串长度为偶数
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");

        }
        byte[] bytes ;
        try {
            // 将16进制字符串转换为字节数组
            bytes = new byte[hexString.length() / 2];
            for (int i = 0; i < hexString.length(); i += 2) {
                // 将两个16进制字符转换为一个字节
                String byteString = hexString.substring(i, i + 2);
                bytes[i / 2] = (byte) (Integer.parseInt(byteString, 16) & 0xFF);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Invalid hex string");
        }
        // 使用UTF-8编码创建字符串
        return new String(bytes, StandardCharsets.UTF_8);
    }


    public static String utf8StringToHex(String str) {
        StringBuilder hex = new StringBuilder();
        // 将字符串转换为字节数组
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        // 遍历字节数组，将每个字节转换为16进制表示
        for (byte b : strBytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    public synchronized static void loadMessageHash(Chat chat) {
        if (!chat.verificationKey()){
            Alert alert = new Alert(Alert.AlertType.WARNING, "请先解锁 "+chat.getRemarkName()+" 聊天，再加载聊天哈希!", ButtonType.OK);
            alert.showAndWait();
        }else {
            System.out.println("聊天是否解锁："+chat.verificationKey());
            Runnable runnable = () -> {
                System.out.println("X-API-KEY: "+ConfigManager.getConfig("config.json").get("X-API-KEY").toString());
                List<String[]> txHashList = (getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress())==null?new ArrayList<>():getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress()));
                for(int i=0;i<=9999;) {
                    List<Map<String, Object>> txList = HtmlPageFetcher.getArrayTxList(ConfigManager.getConfig("config.json").get("X-API-KEY").toString(), chat.getAddress(), chat.getChain().getDisplayID(),i, 100);
                    //在此处返回列表更新全部完成监听
                    if (txList != null && txList.size() != 0) {
                        i = i + txList.size();
                        A:
                        for (Map<String, Object> tx : txList) {
                            try {
                                String txHash = Chat.encrypt(tx.get("hash").toString(), chat.getKey());
                                String txTime = Chat.encrypt(tx.get("blocktime").toString(), chat.getKey());

                                boolean isAdd=false;

                                for(String[] t : txHashList) {
                                    isAdd = Arrays.equals(t, new String[]{txHash, txTime});
                                    if (isAdd){
                                        break ;
                                    }
                                };
                                if (isAdd) {
//                                    System.out.println("没有添加：" + txHash + ":" + txTime);
                                } else {
//                                    System.out.println("添加：" + txHash + ":" + txTime);
                                    int index = 0;
                                    for (; index < txHashList.size(); index++) {
                                        try {
                                            if (Long.parseLong(Chat.decrypt(txHashList.get(index)[1], chat.getKey())) <= Long.parseLong(tx.get("blocktime").toString())) {
                                                break;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            continue A;
                                        }

                                    }
                                    if (index == txHashList.size()) {
                                        txHashList.add(new String[]{txHash, txTime});
                                    } else {
                                        txHashList.add(index, new String[]{txHash, txTime});
                                    }

                                }
//                                txHashList.add();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        putCryptographicChatMapTxHashList(chat.getCryptographicAddress(), txHashList);
//                        System.out.println("加载聊天 " + chat.getCryptographicAddress() + " 交易哈希表加载进度：" + i + "/" + 10000);
                        //在此处返回列表更新监听
                        Runnable  runnable_2 = () -> ChatMessage.loadChatMessageList(chat);
                        new Thread(runnable_2).start();
                    }else {
                        break;
                    }
                }
//                System.out.println("加载聊天 " + chat.getCryptographicAddress() + " 加载完成，交易哈希数量：" + (getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress())==null?0: getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress()).size()));

            };
            Thread thread = new Thread(runnable);
            thread.start();
            putThreads(chat.getAddress(),thread );
        }
    }

    public static Map<String, List<String[]>> getCryptographicChatMapTxHashList() {
        cryptographicChatMapTxHashListLock.lock(); // 获取锁
        try {
            // 受保护的代码
            // 如果这里执行的代码需要一些时间，其他线程将等待直到锁被释放
            return cryptographicChatMapTxHashList;
        } finally {
            cryptographicChatMapTxHashListLock.unlock(); // 释放锁
        }

    }

    public static void setCryptographicChatMapTxHashList(Map<String, List<String[]>> cryptographicChatMapTxHashList) {

        cryptographicChatMapTxHashListLock.lock(); // 获取锁
        try {
            // 受保护的代码
            // 如果这里执行的代码需要一些时间，其他线程将等待直到锁被释放
            Message.cryptographicChatMapTxHashList = cryptographicChatMapTxHashList;
        } finally {
            cryptographicChatMapTxHashListLock.unlock(); // 释放锁
        }
    }
    public static void putCryptographicChatMapTxHashList(String address, List<String[]> txHashList) {
        cryptographicChatMapTxHashListLock.lock(); // 获取锁
        try {
            // 受保护的代码
            // 如果这里执行的代码需要一些时间，其他线程将等待直到锁被释放
            cryptographicChatMapTxHashList.put(address, txHashList);
        } finally {
            cryptographicChatMapTxHashListLock.unlock(); // 释放锁
        }

    }
    public static Map<String, Thread> getThreads() {
        threadsLock.lock();
        try {
            return threads;
        }finally {
            threadsLock.unlock();
        }

    }

    public static void setThreads(Map<String, Thread> threads) {
        threadsLock.lock();
        try {
            Message.threads = threads;
        }finally {
            threadsLock.unlock();
        }
    }

    public static void putThreads(String address,Thread thread) {
        threadsLock.lock();
        try {
            threads.put(address, thread);
        }finally {
            threadsLock.unlock();
        }
    }


    /**
     * 源码链接：<a href="https://blog.csdn.net/qq_35661171/article/details/114284157">...</a>
     * 判断字符是否为中文
     * @param c
     * @return 字符是中文返回 true, 否则返回false
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }


    /**
     * 源码链接：<a href="https://blog.csdn.net/qq_35661171/article/details/114284157">...</a>
     * 判断字符串是否包含乱码
     * @param strText  需要判断的字符串
     * @return 字符串包含乱码则返回true, 字符串不包含乱码则返回false
     */
    public static boolean isMessyCode(String strText) {
        Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
        Matcher m = p.matcher(strText);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = 0 ;
        float count = 0;
        for (char c : ch) {
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
                chLength++;
            }
        }
        float result = count / chLength ;
        return result > 0.4;
    }

}
