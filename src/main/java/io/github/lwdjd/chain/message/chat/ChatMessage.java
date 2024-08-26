package io.github.lwdjd.chain.message.chat;

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
    private static final Lock cryptographicChatMapChatMessageListLock = new ReentrantLock();
    private static Map<String,List<ChatMessage>> cryptographicChatMapChatMessageList = new HashMap<>();
    private static final Lock threadsLock = new ReentrantLock();
    private static Map<String,Thread> threads =new HashMap<>();


    public boolean equals(ChatMessage o) {
        return this.sender.equals(o.sender) &&
                this.time.equals(o.time) &&
                this.text.equals(o.text);


    }

    public ChatMessage(Sender sender,Long time, String text){
        this.sender = sender;
        this.time = time;
        this.text = text;
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

    public synchronized static void loadChatMessageList(Chat chat){
        if(!chat.verificationKey()){
            Alert alert = new Alert(Alert.AlertType.WARNING, "请先解锁 "+chat.getRemarkName()+" 聊天，再加载聊天记录!", ButtonType.OK);
            alert.showAndWait();
        }else {
            Runnable runnable = () -> {

                Sender loadFailure = new Sender("0x0000000000000000000000000000000000000000", null);

                List<ChatMessage> cryptographicChatMessageListTemp = (getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress())==null?new ArrayList<>():getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress()));
                List<String[]> CryptographicChatMapTxHashList = Message.getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress());
                A:
                for (String[] strings : CryptographicChatMapTxHashList) {
                    Transaction transaction = null;
                    ChatMessage loadFailureChatMessage = null;
                    try {
                        loadFailureChatMessage = new ChatMessage(
                                loadFailure,
                                Long.parseLong(Chat.decrypt(strings[1], chat.getKey())),
                                Chat.decrypt(strings[0], chat.getKey()) + "加载失败!!"
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        transaction = Web3.getTransaction("0x"+Chat.decrypt(strings[0], chat.getKey()));
//                        System.out.println(Web3.getTransactionUtf8InputData(transaction) +" "+Chat.decrypt(strings[0], chat.getKey()));
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    if (transaction != null) {
                        ChatMessage chatMessage = null;
                        try {
                            chatMessage = new ChatMessage(
                                    new Sender(
                                            transaction.getFrom(), null
                                    ),
                                    Long.parseLong(Web3.getBlock(transaction.getBlockHash()).getBlock().getTimestampRaw().replaceAll("0x",""),16)*1000,
                                    Web3.getTransactionUtf8InputData(transaction)
                            );
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
                }

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