package io.github.lwdjd.chain.message.fxml;


import io.github.lwdjd.chain.message.chat.Chat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static io.github.lwdjd.chain.message.chat.Chat.saveChatListOfDisk;

public class ChatSettingController implements Initializable {
    static String createChatPassword;
    public Pane detailsPane;
    public VBox setting;
    public ListView<Chat> chatListView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 初始化列表视图
        ObservableList<Chat> chats ;

        try {
            chats = getAddChats();
            chats.get(0).unlockChat(createChatPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            chats.addAll(Chat.getChatList());
        }catch (Exception ignored){

        }
        chatListView.setCellFactory(listView -> new ChatCell());//使用自定义显示
        chatListView.setItems(chats);
        // 设置选择监听器
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldChat, newChat) -> {
            try {
                updateSettingPane(newChat);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void updateSettingPane(Chat selectedChat) {
        if (selectedChat == null) {
            detailsPane.getChildren().clear();
            return;
        }
        // 清除当前detailsPane中的所有内容
        detailsPane.getChildren().clear();

        // 创建聊天详情页面的布局
        Pane chatDetailsPane = new Pane(); // 间距为10
        chatDetailsPane.getChildren().add(ChatDetailContent(selectedChat));

        // 将账户详情页面的布局添加到setting中
        detailsPane.getChildren().add(chatDetailsPane);
    }
    private Node ChatDetailContent(Chat chat){
        // 根据chat创建详情页面的内容
        chatListView.getItems().get(0).unlockChat(createChatPassword);
        try {
            if (chat.getRemarkName().equals("添加聊天") && chat.getChatType()== Chat.ChatType.PRIVATE_CHAT&& chat.verification(createChatPassword)){
                return CreateChat.createChatPage(this);
            }
        }catch (Exception ignored){

        }

        VBox detailContent = new VBox(10);
        ImageView imageView;
        try {
            imageView = new ImageView(new Image(chat.getHeadPortrait()));
        }catch (Exception e){
            imageView = new ImageView(new Image("/io/github/lwdjd/chain/message/img/user.png"));
        }
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);

        Text nameText = new Text(chat.getRemarkName());
        TextField chatTypeTextField;
        try {
            chatTypeTextField = new TextField(chat.getChatType().toString());
        }catch (Exception e){
            e.printStackTrace();
            chatTypeTextField = new TextField("null");
        }

        chatTypeTextField.setEditable(false);

        TextField chainTextField;
        try {
            chainTextField = new TextField(chat.getChain().toString());
        }catch (Exception e){
            e.printStackTrace();
            chainTextField = new TextField("null");
        }
        chainTextField.setEditable(false);

        TextField addressTextField = new TextField();
        addressTextField.setPrefWidth(350);
        addressTextField.setEditable(false);

//        TextField publicKeyTextField = new TextField("0x"+chat.getPublicKey());
//        publicKeyTextField.setPrefWidth(350);
//        publicKeyTextField.setEditable(false);
//
//        TextField addressTextField = new TextField("0x"+chat.getAddress());
//        addressTextField.setPrefWidth(350);
//        addressTextField.setEditable(false);

        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(320);

        Button passwordButton = new Button("显示地址");
        passwordButton.setPrefWidth(80);
        passwordButton.setOnAction(event -> {
            if (chat.unlockChat(passwordField.getText())){
                addressTextField.setText(chat.getAddress());
            }else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "密码错误！！", ButtonType.OK);
                alert.showAndWait();
            }
            passwordField.setText("");
        });

        Button defeatButton = new Button("删除聊天");
        defeatButton.setPrefWidth(80);
        defeatButton.setOnAction(event -> {
            if (chat.unlockChat(passwordField.getText())){
                passwordField.setText("");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "你确定要删除聊天吗？", ButtonType.OK, ButtonType.CANCEL);
                // 设置对话框的标题
                alert.setTitle("删除聊天确认");
                alert.setHeaderText("删除聊天确认");
                // 显示对话框并等待用户响应
                Optional<ButtonType> result = alert.showAndWait();

                // 根据用户的选择执行不同的操作
                result.ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        chat.lockChat();
                        Chat.getChatList().remove(chat);
                        // 初始化聊天列表
                        ObservableList<Chat> chats ;

                        try {
                            chats = ChatSettingController.getAddChats();
                            chats.get(0).unlockChat(ChatSettingController.createChatPassword);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            chats.addAll(Chat.getChatList());
                        }catch (Exception ignored){

                        }
                        chatListView.setItems(chats);
                        ChatGuiController.chatGuiController.updateChatList();
                        try {
                            Chat.saveChatListOfDisk();
                        }catch (Exception e){
                            e.printStackTrace();
                            Alert alert_2 = new Alert(Alert.AlertType.WARNING, "删除失败,请重启！！", ButtonType.OK);
                            alert_2.showAndWait();
                        }
                    }
                });


            }else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "密码错误！！", ButtonType.OK);
                alert.showAndWait();
                passwordField.setText("");
            }
        });

        HBox addressHBox = new HBox(10);
        addressHBox.getChildren().addAll(new Text(" 地址: "), addressTextField);

//        HBox publicKeyHBox = new HBox(10);
//        publicKeyHBox.getChildren().addAll(new Text(" 公钥: "), publicKeyTextField);
//
//        HBox addressHBox = new HBox(10);
//        addressHBox.getChildren().addAll(new Text(" 地址: "), addressTextField);

        //头像和昵称的显示
        HBox chatInformation =new HBox(10);
        chatInformation.getChildren().addAll(
                new VBox(),new VBox(),new VBox(),new VBox(),new VBox(),
                imageView,
                nameText
        );

        HBox chatTypeHBox = new HBox(10);
        chatTypeHBox.getChildren().addAll(
                new Text(" 聊天类型: "),
                chatTypeTextField
        );

        HBox chainHBox = new HBox(10);
        chainHBox.getChildren().addAll(
                new Text(" 区块链: "),
                chainTextField
        );

        HBox passwordFieldHBox = new HBox(10);
        passwordFieldHBox.getChildren().addAll(
                new Text(" 输入密码: "),
                passwordField
        );

        VBox placeholder =new VBox();
        placeholder.setPrefWidth(58);

        HBox passwordButtonHBox = new HBox(10);
        passwordButtonHBox.getChildren().addAll(
                placeholder,
                passwordButton,
                defeatButton
        );

        detailContent.getChildren().addAll(
                new VBox(),new VBox(),new VBox(),
                chatInformation,
                chatTypeHBox,
                chainHBox,
                addressHBox,
                new Text("                    解锁 地址 删除聊天 功能"),
                passwordFieldHBox,
                passwordButtonHBox
        );
        return detailContent;
    }

    static ObservableList<Chat> getAddChats(){
        createChatPassword = String.valueOf(Math.random());
        return FXCollections.observableArrayList(
                Chat.crateChat("0x1d31ee30a7b6bf20de0615aa6d310c3b938ea35d", Chat.ChatType.PRIVATE_CHAT, Chat.Chain.OKTC_TEST,"添加聊天", createChatPassword)
        );
    }
    static class ChatCell extends ListCell<Chat> {
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

                imageView.setFitHeight(20);
                imageView.setFitWidth(20);
                imageView.setPreserveRatio(true); // 保持图片比例
                // 创建圆形遮罩
                Circle mask = new Circle(10); // 半径为20
                mask.setCenterX(10);
                mask.setCenterY(10);

                // 将圆形遮罩应用到ImageView上
                StackPane maskPane = new StackPane(imageView);
                maskPane.setClip(mask);


                Text text = new Text(item.getRemarkName());
                hbox.getChildren().addAll(maskPane, text);
                setGraphic(hbox);
            }
        }
    }
}
/**
 *  处理创建账户页面的相关操作
 */
class CreateChat{
    /**
     * 返回创建账户的页面
     * @return 返回创建账户的页面
     */
    public static Node createChatPage(ChatSettingController controller){
        Pane detailContent = new Pane();
        TabPane tabPane = new TabPane();
        tabPane.setPrefWidth(418);
        tabPane.setPrefHeight(400);
        Tab importChatTab = new Tab("添加聊天");
//        Tab createAccountTab = new Tab("创建账户");
        VBox importChat = new VBox(10);
//        VBox createAccount = new VBox(10);

        TextField importNameTextField = new TextField();
        TextField importAddressTextField = new TextField();
        PasswordField importPasswordField = new PasswordField();
        PasswordField importRePasswordField = new PasswordField();
        ComboBox<Chat.ChatType> typeComboBox = new ComboBox<>();
        ObservableList<Chat.ChatType> typeComboBoxItems = FXCollections.observableArrayList();
//        typeComboBoxItems.addAll(Chat.ChatType.PRIVATE_CHAT);
        typeComboBoxItems.addAll(Chat.ChatType.PUBLIC_CHAT);
        typeComboBox.setItems(typeComboBoxItems);
        typeComboBox.setMaxWidth(150);

        ComboBox<Chat.Chain> chainComboBox = new ComboBox<>();
        ObservableList<Chat.Chain> chainComboBoxItems = FXCollections.observableArrayList();
        chainComboBoxItems.addAll(Chat.Chain.OKTC_TEST);
        chainComboBox.setItems(chainComboBoxItems);
        chainComboBox.setMaxWidth(150);

//        TextField createNameTextField = new TextField();
//        PasswordField createPasswordField = new PasswordField();
//        PasswordField createRePasswordField = new PasswordField();



        Button importChatButton = new Button("添加聊天");
        importChatButton.setOnAction(event -> {
            //创建成功则更新列表
            if (importChat(importNameTextField.getText(),importAddressTextField.getText(),typeComboBox.getValue(),chainComboBox.getValue(),importPasswordField.getText(),importRePasswordField.getText())){
                // 初始化聊天列表
                ObservableList<Chat> chats ;

                try {
                    chats = ChatSettingController.getAddChats();
                    chats.get(0).unlockChat(ChatSettingController.createChatPassword);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    chats.addAll(Chat.getChatList());
                }catch (Exception ignored){

                }
                controller.chatListView.setItems(chats);

            }
        });
        importChat.getChildren().addAll(
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(
                        new Text("   名称: "),
                        importNameTextField),
                new HBox(
                        new Text("   地址: "),
                        importAddressTextField),
                new HBox(
                        new Text("   密码: "),
                        importPasswordField
                ),

                new HBox(
                        new Text(" 重复密码: "),
                        importRePasswordField
                ),
                new HBox(),
                new HBox(
                        new Text(" 选择聊天模式: "),
                        typeComboBox,
                        new Text("  选择区块链："),
                        chainComboBox
                ),
                new HBox(),
                new HBox(),
                new HBox(
                        new Text("           "),
                        importChatButton
                )
        );

//        Button createAccountButton = new Button("创建账户");
//        createAccountButton.setOnAction(event -> {
//            //创建成功则更新列表
//            if (createChat(createNameTextField.getText(),createPasswordField.getText(),createRePasswordField.getText())){
//                // 初始化账户列表
//                ObservableList<Chat> chats ;
//
//                try {
//                    chats = ChatSettingController.getAddChats();
//                    chats.get(0).unlockAccount(AccountSettingController.createAccountPassword);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//                try {
//                    chats.addAll(Chat.getChatList());
//                }catch (Exception ignored){
//
//                }
//                controller.chatListView.setItems(chats);
//            }
//        });
//        createAccount.getChildren().addAll(
//                new HBox(),
//                new HBox(),
//                new HBox(),
//                new HBox(),
//                new HBox(),
//                new HBox(
//                        new Text("   昵称: "),
//                        createNameTextField),
//                new HBox(
//                        new Text("   密码: "),
//                        createPasswordField
//                ),
//                new HBox(
//                        new Text(" 重复密码: "),
//                        createRePasswordField
//                ),
//                new HBox(),
//                new HBox(),
//                new HBox(),
//                new HBox(
//                        new Text("           "),
//                        createAccountButton
//                )
//        );
        importChatTab.setContent(importChat);
//        createAccountTab.setContent(createAccount);
        tabPane.getTabs().addAll(importChatTab);
        detailContent.getChildren().addAll(tabPane);
        return detailContent;
    }
    public static Boolean importChat(String name, String address, Chat.ChatType chatType, Chat.Chain chain, String password, String rePassword) {

        if (password.length()<8){
            Alert alert = new Alert(Alert.AlertType.WARNING, "密码不得少于8位", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        if (Objects.equals(name, "") || Objects.equals(address, "") || Objects.equals(password, "")|| chatType==null || chain==null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "请输入有效信息", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        if(!Objects.equals(password, rePassword)){
            Alert alert = new Alert(Alert.AlertType.WARNING, "两次密码不同", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        for(Chat c : Chat.getChatList()){
            if (Objects.equals(c.getRemarkName(), name)){
                Alert alert = new Alert(Alert.AlertType.WARNING, "聊天名已存在", ButtonType.OK);
                alert.showAndWait();
                return false;
            }
        }
        Chat chat = Chat.crateChat(address, chatType,chain,name, password);
        if (chat == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "导入失败,请确保地址为EVM系列链地址，并且正确，需要40位16进制数，带0x", ButtonType.OK);
            alert.setWidth(600);
            alert.showAndWait();
            return false;
        }
        Chat.addChat(chat);
        if(!saveChatListOfDisk()){
            Alert alert = new Alert(Alert.AlertType.WARNING, "聊天导入成功，但保存到磁盘失败，请重启！", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        ChatGuiController.chatGuiController.updateChatList();
        Alert alert = new Alert(Alert.AlertType.WARNING, "添加成功\n注意：\n1.不要泄漏聊天地址否则账户会被其他人知道!!\n2.请备份聊天地址或chats.json文件，否则聊天丢失后无法找回!!", ButtonType.OK);
        alert.setWidth(600);
        alert.showAndWait();
        return true;
    }
//    public static Boolean createChat(String name, String password, String rePassword) {
//        if (password.length()<8){
//            Alert alert = new Alert(Alert.AlertType.WARNING, "密码不得少于8位", ButtonType.OK);
//            alert.showAndWait();
//            return false;
//        }
//
//        if (Objects.equals(name, "") || Objects.equals(password, "")){
//            Alert alert = new Alert(Alert.AlertType.WARNING, "请输入有效信息", ButtonType.OK);
//            alert.showAndWait();
//            return false;
//        }
//        if(!Objects.equals(password, rePassword)){
//            Alert alert = new Alert(Alert.AlertType.WARNING, "两次密码不同", ButtonType.OK);
//            alert.showAndWait();
//            return false;
//        }
//
//        for(Account ac : Account.getAccountList()){
//            if (Objects.equals(ac.getName(), name)){
//                Alert alert = new Alert(Alert.AlertType.WARNING, "账户名已存在", ButtonType.OK);
//                alert.showAndWait();
//                return false;
//            }
//        }
//
//        Account account = Account.crateAccount(Account.generatePrivateKey(), name, password);
//        if (account == null){
//            Alert alert = new Alert(Alert.AlertType.WARNING, "创建失败", ButtonType.OK);
//            alert.showAndWait();
//            return false;
//        }
//        Account.addAccount(account);
//        if(!saveAccountListOfDisk()){
//            Alert alert = new Alert(Alert.AlertType.WARNING, "账户创建成功，但保存到磁盘失败，请重启！！", ButtonType.OK);
//            alert.showAndWait();
//            return false;
//        }
//        ChatGuiController.chatGuiController.updateAccountList();
//
//        Alert alert = new Alert(Alert.AlertType.WARNING, "创建成功\n注意：\n1.不要泄漏账户私钥否则账户会被盗!!\n2.请备份账户私钥或account.json文件，否则账户丢失后无法找回!!", ButtonType.OK);
//        alert.setWidth(600);
//        alert.showAndWait();
//        return true;
//    }
}
