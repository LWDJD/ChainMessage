package io.github.lwdjd.chain.message.chat;

import io.github.lwdjd.chain.message.fxml.ChatGuiController;
import io.github.lwdjd.chain.message.processor.Message;
import io.github.lwdjd.chain.message.web3.Web3;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.web3j.protocol.core.methods.response.Transaction;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatMessage {
    private final Sender sender;
    private final Long time;//unix时间戳，毫秒级
    private final String text;
    private final String TxHash;
    private static final Lock cryptographicChatMapChatMessageListLock = new ReentrantLock();
    private static Map<String,List<ChatMessage>> cryptographicChatMapChatMessageList = new HashMap<>();
    private static final Lock threadsLock = new ReentrantLock();
    private static Map<String,Thread> threads =new HashMap<>();
    private static Map<String,Set<String>> ShieldTxHash = new HashMap<>();


    public boolean equals(ChatMessage o) {
        return this.sender.equals(o.sender) &&
                this.time.equals(o.time) &&
                this.text.equals(o.text) &&
                this.TxHash.equals(o.TxHash);
    }

    public ChatMessage(Sender sender,Long time, String text,String txHash){
        this.sender = sender;
        this.time = time;
        this.text = text;
        this.TxHash =txHash;
    }
    public Sender getSender() {
        return sender;
    }

    public Long getTime() {
        return time;
    }

    public String getText() {
        return text;
    }
    public String getTxHash(){
        return TxHash;
    }

    public synchronized static void loadChatMessageList(Chat chat){
        if(!chat.verificationKey()){
            Alert alert = new Alert(Alert.AlertType.WARNING, "请先解锁 "+chat.getRemarkName()+" 聊天，再加载聊天记录!", ButtonType.OK);
            alert.showAndWait();
        }else {
            Runnable runnable = () -> {

                Sender loadFailure = new Sender("0x0000000000000000000000000000000000000000", null);

                List<ChatMessage> cryptographicChatMessageListTemp = (getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress()) == null ? new ArrayList<>() : getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress()));
                List<String[]> CryptographicChatMapTxHashList = new ArrayList<>(Message.getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress())) ;
//                List<String[]> Temp = new ArrayList<>();
//                System.out.println("CryptographicChatMapTxHashList.size(): "+CryptographicChatMapTxHashList.size());
//                if (cryptographicChatMessageListTemp.size() == 0) {
//                    Temp=CryptographicChatMapTxHashList;
//                } else{
//                    for (String[] TxHash : CryptographicChatMapTxHashList) {
//
//                        for (ChatMessage chatMessage : cryptographicChatMessageListTemp) {
//
//                            try {
//                                System.out.println(TxHash[0] + "   " + Chat.decrypt(chatMessage.getTxHash(), chat.getKey()));
//                                if (!TxHash[0].equals(Chat.decrypt(chatMessage.getTxHash(), chat.getKey()))) {
//                                    Temp.add(TxHash);
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                Temp.add(TxHash);
//                            }
//                        }
//
//                    }
//                }
//                System.out.println("Temp.size(): "+Temp.size());
//                CryptographicChatMapTxHashList.clear();
////                System.out.println(Temp);
//                CryptographicChatMapTxHashList.;
//                System.out.println("CryptographicChatMapTxHashList.size(): "+CryptographicChatMapTxHashList.size());
                // 假设 getCryptographicChatMapChatMessageList() 和 Message.getCryptographicChatMapTxHashList() 是有效的方法
// 并且它们返回的 map 包含 chat.getCryptographicAddress() 作为键
                System.out.println("CryptographicChatMapTxHashList.size() start:  "+CryptographicChatMapTxHashList.size());
                if (ShieldTxHash.get(chat.getCryptographicAddress())!=null) {
                    System.out.println("cryptographicShieldTxHash.get(chat.getCryptographicAddress()).size():"+ ShieldTxHash.get(chat.getCryptographicAddress()).size());
                    for (String hash : ShieldTxHash.get(chat.getCryptographicAddress())) {
                        for(String[] strings:new ArrayList<>(CryptographicChatMapTxHashList)){
                            try {
                                if (Chat.decrypt(strings[0],chat.getKey()).equals(hash)){
                                    CryptographicChatMapTxHashList.remove(strings);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (CryptographicChatMapTxHashList.size()==0){
                    return;
                }
                // 遍历消息列表
                for (ChatMessage message : cryptographicChatMessageListTemp) {
                    // 解密消息的交易哈希
                    String decryptedHash = null;
                    try {
                        decryptedHash = message.getTxHash();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 检查解密后的哈希是否存在于交易哈希列表中
                    for (String[] txHashEntry : new ArrayList<>(CryptographicChatMapTxHashList)) { // 使用副本以避免修改列表时的并发修改异常
//                        try {
//                            System.out.println(decryptedHash+"   "+ ("0x"+Chat.decrypt(txHashEntry[0],chat.getKey()).toLowerCase()));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        try {
                            if (("0x"+Chat.decrypt(txHashEntry[0],chat.getKey()).toLowerCase()).equals(decryptedHash)) { // 假设 txHashEntry 是一个包含单个哈希的数组
                                // 如果找到匹配的哈希，从列表中删除该元素
                                CryptographicChatMapTxHashList.remove(txHashEntry);
                                break; // 跳出内层循环，继续检查下一个消息
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("CryptographicChatMapTxHashList.size() end:  "+CryptographicChatMapTxHashList.size());

                if (CryptographicChatMapTxHashList.size()==0){
                    return;
                }
                System.out.println("执行到我了");


                A:
                for (String[] strings : CryptographicChatMapTxHashList) {
                    Transaction transaction = null;
                    ChatMessage loadFailureChatMessage = null;
//                    try {
//                        loadFailureChatMessage = new ChatMessage(
//                                loadFailure,
//                                Long.parseLong(Chat.decrypt(strings[1], chat.getKey())),
//                                Chat.decrypt(strings[0], chat.getKey()) + "加载失败!!",
//                                null
//                        );
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    try {
                        transaction = Web3.getTransaction("0x"+Chat.decrypt(strings[0], chat.getKey()));
//                        System.out.println(Web3.getTransactionUtf8InputData(transaction) +" "+Chat.decrypt(strings[0], chat.getKey()));
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    if (transaction != null) {
                        ChatMessage chatMessage = null;
                        if (!transaction.getTo().equals(chat.getAddress())){
                            ShieldTxHash.computeIfAbsent(chat.getCryptographicAddress(), k -> new HashSet<>());
                            try {
                                ShieldTxHash.get(chat.getCryptographicAddress()).add(Chat.decrypt(strings[0],chat.getKey()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                            System.out.println("shieldTxHash:"+transaction.getHash());
                            continue;
                        }
                        try {
                            chatMessage = new ChatMessage(
                                    new Sender(
                                            transaction.getFrom(), null
                                    ),
                                    Long.parseLong(Web3.getBlock(transaction.getBlockHash()).getBlock().getTimestampRaw().replaceAll("0x",""),16)*1000,
                                    Web3.getTransactionUtf8InputData(transaction),
                                    transaction.getHash()
                            );
//                            System.out.println(chatMessage.getText());
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        boolean isAdd = false;
                        if (chatMessage!=null) {
                            for (ChatMessage cryptographicChatMessage : cryptographicChatMessageListTemp) {
                                isAdd = cryptographicChatMessage.equals(chatMessage);
                                if (isAdd) {
                                    break;
                                }
                            }
                        }
                        if (isAdd || chatMessage == null) {
                            if (chatMessage == null) {
                                System.out.println("chatMessage is null");
                            }
                        } else {
//                            System.out.println(chatMessage + " is add");
                            int index = 0;
                            for (; index < cryptographicChatMessageListTemp.size(); index++) {
                                try {
                                    if (chatMessage.getTime() >= cryptographicChatMessageListTemp.get(index).getTime()) {
                                        break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    continue A;
                                }
                            }
                            if (index == cryptographicChatMessageListTemp.size()) {
                                cryptographicChatMessageListTemp.add(chatMessage);
                            } else {
                                cryptographicChatMessageListTemp.add(index, chatMessage);
                            }
                        }
                    } else {
                        int index = 0;
                        for (; index < cryptographicChatMessageListTemp.size(); index++) {
                            try {
                                loadFailureChatMessage = new ChatMessage(
                                        loadFailure,
                                        Long.parseLong(Chat.decrypt(strings[1], chat.getKey())),
                                        Chat.decrypt(strings[0], chat.getKey()) + "加载失败!!",
                                        Chat.decrypt(strings[0], chat.getKey())
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                if (loadFailureChatMessage != null) {
                                    if (loadFailureChatMessage.getTime() >= cryptographicChatMessageListTemp.get(index).getTime()) {
                                        break;
                                    }
                                } else {
                                    System.out.println("loadFailureChatMessage is null");
                                    continue A;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if(index == cryptographicChatMessageListTemp.size()){
                            cryptographicChatMessageListTemp.add(loadFailureChatMessage);
                        }else {
                            cryptographicChatMessageListTemp.add(index, loadFailureChatMessage);
                        }
                    }
                    putCryptographicChatMapChatMessageList(chat.getCryptographicAddress(), cryptographicChatMessageListTemp);
                    //监听返回


                }
                //监听返回
//                System.out.println("执行到runnable_2 loadMessage B了");
                Runnable  runnable_2 = () -> {
                    if (ChatGuiController.chatGuiController.chatList.getSelectionModel().getSelectedItem().equals(chat)) {
                        ChatGuiController.loadMessage(chat);
                    }
                };
                new Thread(runnable_2).start();

            };
            Thread thread = new Thread(runnable);
            thread.start();
            putThreads(chat.getAddress(),thread);
        }

    }
    public static void setCryptographicChatMapChatMessageList(Map<String,List<ChatMessage>> cryptographicChatMapChatMessageList){
        cryptographicChatMapChatMessageListLock.lock();
        try {
            ChatMessage.cryptographicChatMapChatMessageList = cryptographicChatMapChatMessageList;
        }finally {
            cryptographicChatMapChatMessageListLock.unlock();
        }
    }
    public static void putCryptographicChatMapChatMessageList(String address,List<ChatMessage> chatMessageList){
        cryptographicChatMapChatMessageListLock.lock();
        try {
            ChatMessage.cryptographicChatMapChatMessageList.put(address, chatMessageList);
        }finally {
            cryptographicChatMapChatMessageListLock.unlock();
        }
    }

    public static Map<String,List<ChatMessage>> getCryptographicChatMapChatMessageList(){
        cryptographicChatMapChatMessageListLock.lock();
        try {
            return cryptographicChatMapChatMessageList;
        }finally {
            cryptographicChatMapChatMessageListLock.unlock();
        }
    }
    public static Map<String,Thread> getThreads() {
        threadsLock.lock();
        try {
            return threads;
        }finally {
            threadsLock.unlock();
        }
    }

    public static void setThreads(Map<String,Thread> threads) {
        threadsLock.lock();
        try {
            ChatMessage.threads = threads;
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



}