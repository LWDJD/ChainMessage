package io.github.lwdjd.chain.message;

import javax.swing.*;

public class ChainMassageGui {
    private JPanel root;
    private JPanel Chat;
    private JTextArea MessageText;
    private JButton EntetButton;
    private JTextArea ChatMessage;
    private JLabel ChatTitle;
    private JList Chatlist;
    private JButton SettingButton;

    public ChainMassageGui(){

        EntetButton.addActionListener(e -> {
            new Thread(() -> {
                EntetButton.setEnabled(false);
                EntetButton.setText("处理中");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                ChatMessage.append("我："+MessageText.getText()+"\n");
                MessageText.setText("");
                EntetButton.setText("发送");
                EntetButton.setEnabled(true);

            }).start();

        });
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("ChainMassageGui");
        frame.setContentPane(new ChainMassageGui().root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(900,600);
        frame.setVisible(true);
    }
}
