package io.github.lwdjd.chain.message.fxml;

import io.github.lwdjd.chain.message.account.Account;
import io.github.lwdjd.chain.message.chat.Chat;
import io.github.lwdjd.chain.message.chat.ChatMessage;
import io.github.lwdjd.chain.message.processor.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.github.lwdjd.chain.message.chat.ChatMessage.loadChatMessageList;
import static io.github.lwdjd.chain.message.processor.Message.loadMessageHash;

// 控制器类
public class ChatGuiController implements Initializable {
    public Button enterButton;
    public TextArea chatMessage;
    public ListView<ChatMessage> chattingRecords;
    public MenuItem accountMenuItem;
    public MenuItem chatMenuItem;
    public MenuItem settingMenuItem;
    public ComboBox<Account> accountList = new ComboBox<>();
    public static ChatGuiController chatGuiController;
    public ListView<Chat> chatList;
    public Pane unlockPlan;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chatGuiController = this;
        chattingRecords.setFocusTraversable(false);
        chattingRecords.setCellFactory(listView -> new ChattingRecordsCell());//使用自定义显示
        chatList.setCellFactory(listView -> new ChatListCell());//使用自定义显示
        chatList.getSelectionModel().selectedItemProperty().addListener((obs, oldChattingRecordsPane, newChattingRecordsPane) -> {
            try {
                if (newChattingRecordsPane==null){
                    unlockPlan.getChildren().clear();
                    unlockPlan.setPickOnBounds(false);
                }else {
                    updateChattingUnlockRPane(newChattingRecordsPane);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        chatMessage.setWrapText(true);
        // 初始化代码
        List<Account> accountList = Account.getAccountList();
        ObservableList<Account> acList = FXCollections.observableArrayList(accountList);
        this.accountList.setItems(acList);
        this.accountList.setConverter(new StringConverter<>() {
            @Override
            public String toString(Account account) {
                if (account != null) {
                    return account.getName();
                }
                return ""; // 如果Account为null，返回空字符串
            }

            @Override
            public Account fromString(String string) {
                // 通常这个实现是不需要的，除非你需要从字符串创建Account对象
                return null;
            }
        });
        List<Chat> chatList = Chat.getChatList();
        ObservableList<Chat> cList = FXCollections.observableArrayList(chatList);
        this.chatList.setItems(cList);
    }
    @FXML
    public void enterButtonAction(ActionEvent event) {
        // 在这里添加你想要执行的代码
    }

    /**
     *处理打开账户页面事件
     */
    @FXML
    public void accountMenuItemAction(ActionEvent event){
        if (AccountSetting.getStage()==null) {
            new AccountSetting().start(new Stage());
        }
        AccountSetting.getStage().toFront();
    }
    /**
     *处理打开账户页面事件
     */
    @FXML
    public void chatMenuItemAction(ActionEvent event){
        if (ChatSetting.getStage()==null) {
            new ChatSetting().start(new Stage());
        }
        ChatSetting.getStage().toFront();
    }
    /**
     *处理打开设置页面事件
     */
    @FXML
    public void settingMenuItemAction(ActionEvent event){
        if (SettingGui.getStage()==null) {
            new SettingGui().start(new Stage());
        }
        SettingGui.getStage().toFront();
    }

    @FXML
    public void chatSettingMenuItemAction(ActionEvent event){
        new AccountSetting().start(new Stage());
    }
    public void updateAccountList(){
        try{
            List<Account> accountList = Account.getAccountList();
            ObservableList<Account> acList = FXCollections.observableArrayList(accountList);
            this.accountList.setItems(acList);
        }catch (Exception ignored){

        }
    }
    public void updateChatList(){
        try{
            List<Chat> chatList = Chat.getChatList();
            ObservableList<Chat> cList = FXCollections.observableArrayList(chatList);
            this.chatList.setItems(cList);
        }catch (Exception ignored){

        }
    }

    public void addChattingRecordsEnd(ChatMessage chatMessage){
        chattingRecords.getItems().add(0,chatMessage);
    }

    public void addChattingRecords(ChatMessage chatMessage){
        chattingRecords.getItems().add(chatMessage);
    }

    public void setChattingRecords(List<ChatMessage> chattingRecords){
        try{
            ObservableList<ChatMessage> ChatMessageList = FXCollections.observableArrayList(chattingRecords);
            this.chattingRecords.setItems(ChatMessageList);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("设置聊天记录失败");
        }
    }

    public void updateChattingUnlockRPane(Chat chat){
        unlockPlan.getChildren().clear();
        unlockPlan.setPickOnBounds(false);
        chattingRecords.setItems(FXCollections.observableArrayList());
        if (!chat.verificationKey()){
            VBox  vBox = new VBox(10);
            vBox.setPrefWidth(699);

            Text text = new Text(" 请解锁： ");
            PasswordField passwordTextField = new PasswordField();
            passwordTextField.setPromptText("请输入密码");
            passwordTextField.setPrefWidth(300);
            Button enterButton = new Button("确定");
            enterButton.setOnAction(event -> {
                if(!chat.verification(passwordTextField.getText())){
                    Alert alert = new Alert(Alert.AlertType.WARNING, "密码错误！！", ButtonType.OK);
                    alert.showAndWait();
                }else {
                    chat.unlockChat(passwordTextField.getText());
                    unlockPlan.getChildren().clear();
                    unlockPlan.setPickOnBounds(false);
                    updateChattingUnlockRPane(chat);
                }
            });

            HBox hBox = new HBox();
            hBox.setPrefHeight(200);
            HBox hBox2 = new HBox();
            hBox.setPrefHeight(100);
            vBox.getChildren().addAll(
                    hBox,
                    new HBox(10,
                            hBox2,
                            text,
                            passwordTextField,
                            enterButton
                    )

            );
            unlockPlan.getChildren().add(vBox);
            unlockPlan.setPickOnBounds(true);
        }else {
            unlockPlan.getChildren().clear();
            unlockPlan.setPickOnBounds(false);
            loadChattingRecords(chat);
        }
    }

    public void loadChattingRecords(Chat chat){
//        Sender sender = new Sender("0x0000000000000000000000000000000000000000",null);
        if ( Message.getThreads().get(chat.getAddress()) == null || Message.getThreads().get(chat.getAddress()).getState() == Thread.State.TERMINATED ){
            loadMessageHash(chat);
        }else {
            System.out.println(chat.getAddress()+"上次的哈希表更新线程未结束");
        }
        if((ChatMessage.getThreads().get(chat.getAddress()) == null || ChatMessage.getThreads().get(chat.getAddress()).getState() == Thread.State.TERMINATED)
                && Message.getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress())!=null
                && Message.getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress()).size()>0
        ){
            loadChatMessageList(chat);
        }else {
            System.out.println(chat.getAddress()+"上次的聊天记录更新线程未结束或没有聊天记录");
        }
        if (ChatMessage.getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress())!=null
                && ChatMessage.getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress()).size()>0
        ) {
            Runnable runnable = () -> {
                //            chattingRecords.getItems().addAll(
//                    ChatMessage.getCryptographicChatMapChatMessageList().get(
//                            chat.getCryptographicAddress()
//                    )
//            );
                chattingRecords.getItems().clear();
                for (ChatMessage chatMessage : ChatMessage.getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress())){
                    addChattingRecordsEnd(chatMessage);
                }
//            for (int i  =  ChatMessage.getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress()).size()-1; i >= 0; i--){
//                addChattingRecords(ChatMessage.getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress()).get(i));
//            }

            };
            new Thread(runnable).start();

        }else {
            System.out.println("聊天记录为空");
        }

//        addChattingRecordsEnd(new ChatMessage(sender,"1724623121485","是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。"));
    }



    static class ChattingRecordsCell extends ListCell<ChatMessage> {
        {
            setPrefWidth(699); // 设置列表项的宽度
        }
        @Override
        protected void updateItem(ChatMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                HBox hbox = new HBox(10); // 间距为10
                ImageView imageView;
                try {
                    imageView = new ImageView(new Image(item.getSender().getAvatarPath()));
                }catch (Exception e){
                    imageView = new ImageView(new Image("/io/github/lwdjd/chain/message/img/user.png"));
                }

                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true); // 保持图片比例
//                // 创建圆形遮罩
//                Circle mask = new Circle(20); // 半径为20
//                mask.setCenterX(20);
//                mask.setCenterY(20);
//
//                // 将圆形遮罩应用到ImageView上
//                StackPane maskPane = new StackPane(imageView);
//                maskPane.setClip(mask);
//                maskPane.getClip().setLayoutX(maskPane.getChildren().get(0).getLayoutX());
//                maskPane.getClip().setLayoutY(maskPane.getChildren().get(0).getLayoutY());


                Text name;
                if (item.getSender().getRemarkName() !=null){
                    name = new Text(item.getSender().getRemarkName());
                }else if(item.getSender().getName() !=null){
                    name = new Text(item.getSender().getName());
                }else if (item.getSender().getAddress() !=null){
                    name = new Text(item.getSender().getAddress());
                }else {
                    name = new Text("未知用户");
                }



                // 使用 long 值创建 Date 对象
                Date date = new Date(item.getTime());

                // 创建 SimpleDateFormat 对象，并定义日期时间格式
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // 设置时区为 UTC+8
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+16:00"));

                // 将 Date 对象格式化为字符串
                String formattedDate = dateFormat.format(date);

                name.setText(name.getText()+"    时间: "+formattedDate);
                TextArea messageTextArea  = new TextArea(item.getText());
                messageTextArea.setPrefWidth(500);
                messageTextArea.setWrapText(true);
                messageTextArea.setEditable(false);
                VBox vBox = new VBox(
                        name,
                        messageTextArea
                );
                vBox.setPrefWidth(500);

                hbox.getChildren().addAll(
                        imageView,
                        vBox
                );
                setGraphic(hbox);
            }
        }
    }
    static class ChatListCell extends ListCell<Chat> {
        {
            setPrefWidth(180); // 设置列表项的宽度
        }
        @Override
        protected void updateItem(Chat item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                HBox hbox = new HBox(10); // 间距为10
                ImageView imageView;
                try {
                    imageView = new ImageView(new Image(item.getHeadPortrait()));
                }catch (Exception e){
                    imageView = new ImageView(new Image("/io/github/lwdjd/chain/message/img/user.png"));
                }

                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true); // 保持图片比例
                // 创建圆形遮罩
                Circle mask = new Circle(20); // 半径为20
                mask.setCenterX(20);
                mask.setCenterY(20);

                // 将圆形遮罩应用到ImageView上
                StackPane maskPane = new StackPane(imageView);
                maskPane.setClip(mask);


                Text text = new Text("【"+item.getChatType().toString()+"】"+item.getRemarkName());
                hbox.getChildren().addAll(
                        maskPane,
                        text
                );
                setGraphic(hbox);
            }
        }
    }
}