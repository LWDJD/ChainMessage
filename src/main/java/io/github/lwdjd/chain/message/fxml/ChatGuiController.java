package io.github.lwdjd.chain.message.fxml;

import io.github.lwdjd.chain.message.account.Account;
import io.github.lwdjd.chain.message.chat.Chat;
import io.github.lwdjd.chain.message.chat.ChatMessage;
import io.github.lwdjd.chain.message.processor.Message;
import io.github.lwdjd.chain.message.web3.Web3;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static io.github.lwdjd.chain.message.processor.Message.isMessyCode;
import static io.github.lwdjd.chain.message.processor.Message.loadMessageHash;

// 控制器类
public class ChatGuiController implements Initializable {
    public Button sendMessageButton;
    public TextArea chatMessage;
    public ListView<ChatMessage> chattingRecords;
    public MenuItem accountMenuItem;
    public MenuItem chatMenuItem;
    public MenuItem settingMenuItem;
    public String thisChatAddress = "";
    public ComboBox<Account> accountList = new ComboBox<>();
    public static ChatGuiController chatGuiController;
    public ListView<Chat> chatList;
    public Pane unlockPlan;
    public Label chatLabel = new Label("");
    public static Map<String, Boolean> chatRefreshMap =new HashMap<>();
    public Button refresh;
    public Button unlockingedAccountButton;


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
                } else {
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
        this.accountList.getSelectionModel().selectedItemProperty().addListener((obs, oldAccount, newAccount) ->{
            if (newAccount!=null) {
                if (newAccount.verificationKey()){
                    unlockingedAccountButton.setText("锁定账户");
                }else {
                    unlockingedAccountButton.setText("解锁账户");
                }
            }else {
                unlockingedAccountButton.setText("解锁账户");
            }
        });
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
//    @FXML
//    public void unlockingedAccountAction(ActionEvent event){
//        if (accountList.getSelectionModel().getSelectedItem() != null){
//            if(accountList.getSelectionModel().getSelectedItem().verificationKey()){
//                accountList.getSelectionModel().getSelectedItem().lockAccount();
//                unlockingedAccountButton.setText("解锁账户");
//            }else {
//                // 创建一个自定义的Alert
//                Alert alert = new Alert(Alert.AlertType.INFORMATION, "输入密码解锁账户", ButtonType.OK, ButtonType.CANCEL);
//                alert.setHeaderText("解锁账户");
//
//                // 创建一个PasswordField并添加到Alert中
//                PasswordField passwordField = new PasswordField();
//                passwordField.setPromptText("请输入密码");
//
//                // 使用GridPane布局来组织内容
//                GridPane grid = new GridPane();
//                grid.add(passwordField, 0, 1);
//
//                alert.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
//                alert.getDialogPane().setContent(grid);
//
////                // 设置Alert的图标和标题
////                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("path/to/icon.png"));
////                alert.initModality(Modality.APPLICATION_MODAL);
//
//                // 显示Alert并等待用户响应
//                alert.showAndWait().ifPresent(button -> {
//                    if (button == ButtonType.OK) {
//                        // 用户点击了OK，获取PasswordField中的内容并尝试解锁
//                        if (!accountList.getSelectionModel().getSelectedItem().unlockAccount(passwordField.getText())){
//                            Alert alert_2 = new Alert(Alert.AlertType.WARNING, "密码错误!!", ButtonType.OK);
//                            alert_2.showAndWait();
//                        }else {
//                            unlockingedAccountButton.setText("锁定账户");
//                        }
//                    }
//                });
//            }
//
//        }else {
//            Alert alert = new Alert(Alert.AlertType.WARNING, "请选择一个账户再解锁(锁定)!!", ButtonType.OK);
//            alert.showAndWait();
//        }
//    }
@FXML
public void unlockingedAccountAction(ActionEvent event) {
    final Account selectedAccount = accountList.getSelectionModel().getSelectedItem();
    if (selectedAccount != null) {
        try {
            if (!selectedAccount.verificationKey()) {
                // 显示输入密码的对话框
                showUnlockAccountPopup();
            } else {
                selectedAccount.lockAccount();
                unlockingedAccountButton.setText("解锁账户");
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "发生错误: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    } else {
        new Alert(Alert.AlertType.WARNING, "请选择一个账户再解锁(锁定)!!", ButtonType.OK).showAndWait();
    }
}

    public void showUnlockAccountPopup() {
        // 创建密码输入对话框
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("解锁账户 - " + accountList.getSelectionModel().getSelectedItem().getName());

        // 创建密码输入框
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");

        // 创建确认解锁按钮
        Button unlockButton = new Button("确认解锁");
        unlockButton.setOnAction(event -> {
            // 获取密码并进行解锁操作
            String password = passwordField.getText();
            if (password != null && !password.isEmpty()) {
                unlockAccount(password);
                dialog.close(); // 关闭对话框
            }
        });



        // 使用GridPane布局来组织内容
        GridPane grid = new GridPane();
        grid.add(passwordField, 0, 0);
        grid.add(unlockButton, 1, 0);
        grid.setHgap(5); // 设置水平间距
        grid.setVgap(5); // 设置垂直间距

        // 设置对话框的内容和按钮
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        // 显示对话框并等待用户响应
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.CANCEL) {
                // 用户点击了取消按钮，不执行任何操作
            }
        });
    }

    private void unlockAccount(String password) {
        // 根据获取的密码进行解锁账户的逻辑
        // 例如:
        // if (account.authenticate(password)) {
        //     account.unlock();
        //     unlockingedAccountButton.setText("锁定账户");
        // } else {
        //     // 密码错误处理
        // }
        if (password != null && accountList.getSelectionModel().getSelectedItem().unlockAccount(password)) {
            unlockingedAccountButton.setText("锁定账户");
        } else {
            new Alert(Alert.AlertType.WARNING, "密码错误!!", ButtonType.OK).showAndWait();
        }


    }
    @FXML
    public void enterButtonAction(ActionEvent event) {

        // 在这里添加你想要执行的代码
        if (accountList.getSelectionModel().getSelectedItem() != null){
            if (chatList.getSelectionModel().getSelectedItem() != null){
                if (chatList.getSelectionModel().getSelectedItem().verificationKey()) {
                    if (accountList.getSelectionModel().getSelectedItem().verificationKey()){
                        System.out.println( "由 "+accountList.getSelectionModel().getSelectedItem().getName()+" 发送至 "+
                                chatList.getSelectionModel().getSelectedItem().getRemarkName()+" 的消息：\n"+
                                chatMessage.getText());
                        new Thread(() ->sendMessage(
                                chatList.getSelectionModel().getSelectedItem(),
                                accountList.getSelectionModel().getSelectedItem(),
                                chatMessage.getText()
                        )).start();
                    }else {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "请解锁账户再发送!!", ButtonType.OK);
                        alert.showAndWait();
                    }
                }else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "请解锁聊天再发送!!", ButtonType.OK);
                    alert.showAndWait();
                }
            }else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "请选择聊天再发送!!", ButtonType.OK);
                alert.showAndWait();
            }
        }else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "请选择账户再发送!!", ButtonType.OK);
            alert.showAndWait();
        }


    }

    public static void sendMessage(Chat chat,Account account,String message){
        BigInteger gasPrice;
        try {
            gasPrice = Web3.getGasPrice().divide(BigInteger.valueOf(1000));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("悲报 · 发送失败");
                alert.setHeaderText("gas价格获取失败");
                alert.setContentText("gas价格获取失败,无法发送！！");
                alert.showAndWait();
            });
            return;
        }
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        long byteCount = bytes.length; // 获取字节数组的长度
        EthSendTransaction ethSendTransaction = null;
        try {
            ethSendTransaction = Web3.sendTransaction(
                                        account.getPrivateKey(),
                                        chat.getAddress(),
                                        message,
                                        gasPrice,
                                        BigInteger.valueOf(byteCount).multiply(BigInteger.valueOf(68)).add(BigInteger.valueOf(21000))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ethSendTransaction!=null&&ethSendTransaction.getError()==null){
            ChatGuiController.chatGuiController.chatMessage.clear();
            EthSendTransaction finalEthSendTransaction = ethSendTransaction;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("喜讯 · 消息提交成功");
                alert.setHeaderText("消息提交成功(不代表发送成功)");
                alert.setContentText("Hash : " + finalEthSendTransaction.getTransactionHash());
                alert.showAndWait();
                ChatGuiController.chatGuiController.reFlushChattingRecords();
            });
        }else {
            if (ethSendTransaction != null) {
                EthSendTransaction finalEthSendTransaction1 = ethSendTransaction;
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("悲报 · 发送失败");
                    alert.setHeaderText("发送失败");
                    alert.setContentText("发送失败 : " + finalEthSendTransaction1.getError().getMessage());
                    alert.showAndWait();
                });
            }else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("悲报 · 发送失败");
                    alert.setHeaderText("发送失败");
                    alert.setContentText("发送失败 ：无返回数据");
                    alert.showAndWait();
                });
            }
        }
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

    public void reFlushChattingRecords(){
        if (chatList.getSelectionModel().getSelectedItem()!=null&& chatList.getSelectionModel().getSelectedItem().verificationKey()) {
            loadChattingRecords(chatList.getSelectionModel().getSelectedItem());
        }
    }

    public void addChattingRecordsEnd(ChatMessage chatMessage){
        chattingRecords.getItems().add(0,chatMessage);
    }
    public void addChattingRecordsAll(List<ChatMessage> chatMessage){
        chattingRecords.getItems().addAll(chatMessage);
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
            thisChatAddress = "";
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
//            chatLabel.setText("【"+chat.getChatType()+"】"+chat.getRemarkName());
        }
    }

    public void loadChattingRecords(Chat chat){
        chatRefreshMap.put(chat.getAddress(),true);
        thisChatAddress = chat.getAddress();
//        Sender sender = new Sender("0x0000000000000000000000000000000000000000",null);
        if ( Message.getThreads().get(chat.getAddress()) == null || Message.getThreads().get(chat.getAddress()).getState() == Thread.State.TERMINATED ){
            new Thread(() -> loadMessageHash(chat)).start();
        }else {
            System.out.println(chat.getAddress()+"上次的哈希表更新线程未结束");
        }
//        if((ChatMessage.getThreads().get(chat.getAddress()) == null || ChatMessage.getThreads().get(chat.getAddress()).getState() == Thread.State.TERMINATED)
//                && Message.getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress())!=null
//                && Message.getCryptographicChatMapTxHashList().get(chat.getCryptographicAddress()).size()>0
//        ){
//            loadChatMessageList(chat);
//        }else {
//            System.out.println(chat.getAddress()+"上次的聊天记录更新线程未结束或没有聊天记录");
//        }

        new Thread(() -> loadMessage(chat)).start();


//        addChattingRecordsEnd(new ChatMessage(sender,"1724623121485","是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。是的，这是一个测试。"));
    }

    public static void loadMessage(Chat chat){
        if (ChatMessage.getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress())!=null
                && ChatMessage.getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress()).size()>0
        ) {
            List<ChatMessage> chatMessageList=new ArrayList<>();
            for (ChatMessage chatMessage : ChatMessage.getCryptographicChatMapChatMessageList().get(chat.getCryptographicAddress())){

                if (isMessyCode(chatMessage.getText()) || Objects.equals(chatMessage.getText(), "") ||chatMessage.getText()==null){
//                    System.out.println("字符串包含乱码或为空");
                }else {
//                    System.out.println("字符串是有效的UTF-8编码");
                    chatMessageList.add(0,chatMessage);
                }
            }
            if (chatRefreshMap.get(chat.getAddress())) {
                Platform.runLater(() -> {
                    // 更新UI组件的代码
                    System.out.println("更新UI");
                    chatGuiController.chattingRecords.getItems().clear();
                    chatGuiController.addChattingRecordsAll(chatMessageList);

                    ChatGuiController.chatGuiController.chattingRecords.scrollTo(ChatGuiController.chatGuiController.chattingRecords.getItems().size());
                    chatRefreshMap.put(chat.getAddress(),false);

                });
            }

        }else {
            System.out.println("聊天记录为空");
        }
    }


    //历史聊天元素设置
    static class ChattingRecordsCell extends ListCell<ChatMessage> {
        {
            setPrefWidth(640); // 设置列表项的宽度
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


                Text name = new Text("");
                for(Account account : Account.getAccountList()){
                    if (Objects.equals("0x"+account.getAddress(), item.getSender().getAddress())){
                        name = new Text("[本地账户]"+account.getName());
                    }
                }
                if(item.getSender().getAddress().equals(chatGuiController.thisChatAddress)) {
                    name = new Text("[本频道主]");
                }
                if (name.getText().equals("")||name.getText().equals("[本频道主]")){
                    if (item.getSender().getRemarkName() !=null){
                        name = new Text(name.getText()+item.getSender().getRemarkName());
                    }else if(item.getSender().getName() !=null){
                        name = new Text(name.getText()+item.getSender().getName());
                    }else if (item.getSender().getAddress() !=null){
                        if (!name.getText().equals("[本频道主]")) {
                            name = new Text(name.getText() + item.getSender().getAddress());
                        }
                    }else {
                        name = new Text(name.getText()+"未知用户");
                    }
                }



                // 使用 long 值创建 Date 对象
                Date date = new Date(item.getTime());

                // 创建 SimpleDateFormat 对象，并定义日期时间格式
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // 设置时区为 UTC+8
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));

                // 将 Date 对象格式化为字符串
                String formattedDate = dateFormat.format(date);

                name.setText(name.getText()+"    时间: "+formattedDate);


                Label label = new Label(item.getText());
                label.setWrapText(true); // 启用自动换行
                label.setMaxWidth(500); // 设置最大宽度
                // 监听文本变化，自动调整宽度
                label.textProperty().addListener((observable, oldValue, newValue) -> {
                    double currentWidth = label.prefWidth(-1); // 获取当前首选宽度
                    label.setPrefWidth(Math.max(currentWidth, 500)); // 至少500px宽
                });

//                VBox root = new VBox(label);

//                TextArea messageTextArea  = new TextArea(item.getText());
//                messageTextArea.setPrefWidth(500);
//                messageTextArea.setWrapText(true);
//                messageTextArea.setEditable(false);


                VBox vBox = new VBox(
                        name,
                        new Bubble(item.getText())
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
class Bubble extends StackPane {
    public Bubble(String message) {
        Text text = new Text(message);
        text.setFill(Color.WHITE);
        text.setFont(Font.font(12));
        text.setWrappingWidth(500);
        // 创建文本框显示消息
        getChildren().add(text);
        // 将文本框添加至气泡
        setStyle("-fx-background-color: DodgerBlue; -fx-background-radius: 15; -fx-padding: 10;");
        // 设置样式
        setMaxWidth(text.getWrappingWidth()); // 自动根据内容宽度调整气泡宽度 }}
    }
}
