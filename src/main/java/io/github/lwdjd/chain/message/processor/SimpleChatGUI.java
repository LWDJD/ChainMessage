package io.github.lwdjd.chain.message.processor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimpleChatGUI extends JFrame {
    private JTextField messageInput;
    private JButton sendButton;
    private JTextArea chatHistory;

    public SimpleChatGUI() {
        super("简单聊天GUI");
        this.setSize(400, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        // 创建聊天历史文本区域
        chatHistory = new JTextArea();
        chatHistory.setEditable(false);
        this.add(new JScrollPane(chatHistory), BorderLayout.CENTER);

        // 创建消息输入框
        messageInput = new JTextField();
        this.add(messageInput, BorderLayout.SOUTH);

        // 创建发送按钮
        sendButton = new JButton("发送");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageInput.getText();
                if (!message.isEmpty()) {
                    chatHistory.append("我: " + message + "\n");
                    messageInput.setText(""); // 清空输入框
                }
            }
        });

        // 添加发送按钮到面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        this.add(buttonPanel, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimpleChatGUI().setVisible(true);
            }
        });
    }
}