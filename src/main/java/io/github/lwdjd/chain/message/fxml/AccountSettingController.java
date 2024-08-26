package io.github.lwdjd.chain.message.fxml;

import io.github.lwdjd.chain.message.account.Account;
import io.github.lwdjd.chain.message.web3.Web3;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

import static io.github.lwdjd.chain.message.account.Account.saveAccountListOfDisk;


public class AccountSettingController implements Initializable {
    TextField balanceTextField = new TextField();
    static String createAccountPassword;
    @FXML
    public VBox setting;
    @FXML
    public ListView<Account> accountListView;
    @FXML
    public Pane detailsPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 初始化账户列表
        ObservableList<Account> accounts ;

        try {
            accounts = getAddAccounts();
            accounts.get(0).unlockAccount(createAccountPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            accounts.addAll(Account.getAccountList());
        }catch (Exception ignored){

        }
//        accountListView.setItems(FXCollections.observableArrayList());
        accountListView.setCellFactory(listView -> new AccountCell());//使用自定义显示
        accountListView.setItems(accounts);
        // 设置选择监听器
        accountListView.getSelectionModel().selectedItemProperty().addListener((obs, oldAccount, newAccount) -> {
            try {
                updateSettingPane(newAccount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void updateSettingPane(Account selectedAccount) {
        if (selectedAccount == null) {
            detailsPane.getChildren().clear();
            return;
        }
        // 清除当前detailsPane中的所有内容
        detailsPane.getChildren().clear();

        // 创建账户详情页面的布局
        Pane accountdetailsPane = new Pane(); // 间距为10
        accountdetailsPane.getChildren().add(AccountDetailContent(selectedAccount));

        // 将账户详情页面的布局添加到setting中
        detailsPane.getChildren().add(accountdetailsPane);
    }

    private Node AccountDetailContent(Account account){
        // 根据account创建详情页面的内容
        accountListView.getItems().get(0).unlockAccount(createAccountPassword);
        try {
            if (account.getName().equals("添加账户") && account.getPrivateKey().equals("ffffffffffffffff")&& account.verification(createAccountPassword)){
                return CreateAccount.createAccountPage(this);
            }
        }catch (Exception ignored){

        }

        VBox detailContent = new VBox(10);
        ImageView imageView;
        try {
            imageView = new ImageView(new Image(account.getHeadPortrait()));
        }catch (Exception e){
            imageView = new ImageView(new Image("/io/github/lwdjd/chain/message/img/user.png"));
        }
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);

        Text nameText = new Text(account.getName());
        balanceTextField.setText("");
        Text balance = new Text(" 余额:");
        balanceTextField.setPrefWidth(120);
        balanceTextField.setEditable(false);

        new Thread(() -> {
            try {
                balanceTextField.setText(Objects.requireNonNull(Web3.getBalance("0x"+account.getAddress())).toString());
            } catch (Exception e) {
                balanceTextField.setText("获取余额失败");
            }
        }).start();
        Button balanceRefreshButton = new Button("刷新");
        balanceRefreshButton.setPrefWidth(50);
        balanceRefreshButton.setOnAction(event -> {
            new Thread(() -> {
                balanceRefreshButton.setDisable(true);
                try {
                    balanceTextField.setText(Objects.requireNonNull(Web3.getBalance("0x"+account.getAddress())).toString());
                    balanceRefreshButton.setDisable(false);
                } catch (Exception e) {
                    balanceTextField.setText("获取余额失败");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    balanceRefreshButton.setDisable(false);
                    throw new RuntimeException(e);
                }
                balanceRefreshButton.setDisable(false);
            }).start();
        });



        TextField privateKeyTextField = new TextField();
        privateKeyTextField.setPrefWidth(350);
        privateKeyTextField.setEditable(false);

        TextField publicKeyTextField = new TextField("0x"+account.getPublicKey());
        publicKeyTextField.setPrefWidth(350);
        publicKeyTextField.setEditable(false);

        TextField addressTextField = new TextField("0x"+account.getAddress());
        addressTextField.setPrefWidth(350);
        addressTextField.setEditable(false);

        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(320);

        Button passwordButton = new Button("显示私钥");
        passwordButton.setPrefWidth(80);
        passwordButton.setOnAction(event -> {
            if (account.unlockAccount(passwordField.getText())){
                privateKeyTextField.setText(account.getPrivateKey());
            }else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "密码错误！！", ButtonType.OK);
                alert.showAndWait();
            }
            passwordField.setText("");
        });

        Button defeatButton = new Button("删除账户");
        defeatButton.setPrefWidth(80);
        defeatButton.setOnAction(event -> {
            if (account.unlockAccount(passwordField.getText())){
                passwordField.setText("");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "你确定要删除账户吗？", ButtonType.OK, ButtonType.CANCEL);
                // 设置对话框的标题
                alert.setTitle("删除账户确认");
                alert.setHeaderText("删除账户确认");
                // 显示对话框并等待用户响应
                Optional<ButtonType> result = alert.showAndWait();

                // 根据用户的选择执行不同的操作
                result.ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        account.lockAccount();
                        Account.getAccountList().remove(account);
                        // 初始化账户列表
                        ObservableList<Account> accounts ;

                        try {
                            accounts = AccountSettingController.getAddAccounts();
                            accounts.get(0).unlockAccount(AccountSettingController.createAccountPassword);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            accounts.addAll(Account.getAccountList());
                        }catch (Exception ignored){

                        }
                        accountListView.setItems(accounts);
                        ChatGuiController.chatGuiController.updateAccountList();
                        try {
                            Account.saveAccountListOfDisk();
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

        HBox privateKeyHBox = new HBox(10);
        privateKeyHBox.getChildren().addAll(new Text(" 私钥: "), privateKeyTextField);

        HBox publicKeyHBox = new HBox(10);
        publicKeyHBox.getChildren().addAll(new Text(" 公钥: "), publicKeyTextField);

        HBox addressHBox = new HBox(10);
        addressHBox.getChildren().addAll(new Text(" 地址: "), addressTextField);

        //头像和昵称的显示
        HBox accountInformation =new HBox(10);
        accountInformation.getChildren().addAll(
                new VBox(),new VBox(),new VBox(),new VBox(),
                imageView,
                nameText,
                new VBox(),new VBox(),
                balance,
                balanceTextField,
                balanceRefreshButton
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
                accountInformation,
                privateKeyHBox,
                publicKeyHBox,
                addressHBox,
                new Text("                    解锁 私钥 删除账户 功能"),
                passwordFieldHBox,
                passwordButtonHBox
        );
        return detailContent;
    }

    static ObservableList<Account> getAddAccounts(){
        createAccountPassword = String.valueOf(Math.random());
        return FXCollections.observableArrayList(
                Account.crateAccount("ffffffffffffffff","添加账户", createAccountPassword)
        );
    }



    static class AccountCell extends ListCell<Account> {
        {
            setPrefWidth(180); // 设置列表项的宽度
        }
        @Override
        protected void updateItem(Account item, boolean empty) {
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
                maskPane.getClip().setLayoutX(maskPane.getChildren().get(0).getLayoutX());
                maskPane.getClip().setLayoutY(maskPane.getChildren().get(0).getLayoutY());

                Text text = new Text(item.getName());
                hbox.getChildren().addAll(maskPane, text);
                setGraphic(hbox);
            }
        }
    }
}

/**
 *  处理创建账户页面的相关操作
 */
class CreateAccount{
    /**
     * 返回创建账户的页面
     * @return 返回创建账户的页面
     */
    public static Node createAccountPage(AccountSettingController controller){
        Pane detailContent = new Pane();
        TabPane tabPane = new TabPane();
        tabPane.setPrefWidth(418);
        tabPane.setPrefHeight(400);
        Tab importAccountTab = new Tab("导入账户");
        Tab createAccountTab = new Tab("创建账户");
        VBox importAccount = new VBox(10);
        VBox createAccount = new VBox(10);

        TextField importNameTextField = new TextField();
        TextField importPrivateKeyTextField = new TextField();
        PasswordField importPasswordField = new PasswordField();
        PasswordField importRePasswordField = new PasswordField();

        TextField createNameTextField = new TextField();
        PasswordField createPasswordField = new PasswordField();
        PasswordField createRePasswordField = new PasswordField();


        Button importAccountButton = new Button("导入账户");
        importAccountButton.setOnAction(event -> {
            //创建成功则更新列表
            if (importAccount(importNameTextField.getText(),importPrivateKeyTextField.getText(),importPasswordField.getText(),importRePasswordField.getText())){
                // 初始化账户列表
                ObservableList<Account> accounts ;

                try {
                    accounts = AccountSettingController.getAddAccounts();
                    accounts.get(0).unlockAccount(AccountSettingController.createAccountPassword);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    accounts.addAll(Account.getAccountList());
                }catch (Exception ignored){

                }
                controller.accountListView.setItems(accounts);

            }
        });
        importAccount.getChildren().addAll(
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(
                        new Text("   昵称: "),
                        importNameTextField),
                new HBox(
                        new Text("   私钥: "),
                        importPrivateKeyTextField),
                new HBox(
                        new Text("   密码: "),
                        importPasswordField
                ),

                new HBox(
                        new Text(" 重复密码: "),
                        importRePasswordField
                ),
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(
                        new Text("           "),
                        importAccountButton
                )
        );

        Button createAccountButton = new Button("创建账户");
        createAccountButton.setOnAction(event -> {
            //创建成功则更新列表
            if (createAccount(createNameTextField.getText(),createPasswordField.getText(),createRePasswordField.getText())){
                // 初始化账户列表
                ObservableList<Account> accounts ;

                try {
                    accounts = AccountSettingController.getAddAccounts();
                    accounts.get(0).unlockAccount(AccountSettingController.createAccountPassword);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    accounts.addAll(Account.getAccountList());
                }catch (Exception ignored){

                }
                controller.accountListView.setItems(accounts);
            }
        });
        createAccount.getChildren().addAll(
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(
                        new Text("   昵称: "),
                        createNameTextField),
                new HBox(
                        new Text("   密码: "),
                        createPasswordField
                ),
                new HBox(
                        new Text(" 重复密码: "),
                        createRePasswordField
                ),
                new HBox(),
                new HBox(),
                new HBox(),
                new HBox(
                        new Text("           "),
                        createAccountButton
                )
        );
        importAccountTab.setContent(importAccount);
        createAccountTab.setContent(createAccount);
        tabPane.getTabs().addAll(createAccountTab,importAccountTab);
        detailContent.getChildren().addAll(tabPane);
        return detailContent;
    }
    public static Boolean importAccount(String name, String privateKey, String password,String rePassword) {

        if (password.length()<8){
            Alert alert = new Alert(Alert.AlertType.WARNING, "密码不得少于8位", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        if (Objects.equals(name, "") || Objects.equals(privateKey, "") || Objects.equals(password, "")){
            Alert alert = new Alert(Alert.AlertType.WARNING, "请输入有效信息", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        if(!Objects.equals(password, rePassword)){
            Alert alert = new Alert(Alert.AlertType.WARNING, "两次密码不同", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        for(Account ac : Account.getAccountList()){
            if (Objects.equals(ac.getName(), name)){
                Alert alert = new Alert(Alert.AlertType.WARNING, "账户名已存在", ButtonType.OK);
                alert.showAndWait();
                return false;
            }
        }
        Account account = Account.crateAccount(privateKey, name, password);
        if (account == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "导入失败,请确保私钥为EVM系列网络私钥，并且正确!!\n注意: 私钥需要0x前导标识", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        Account.addAccount(account);
        if(!saveAccountListOfDisk()){
            Alert alert = new Alert(Alert.AlertType.WARNING, "账户导入成功，但保存到磁盘失败，请重启！", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        ChatGuiController.chatGuiController.updateAccountList();
        Alert alert = new Alert(Alert.AlertType.WARNING, "导入成功\n注意：\n1.不要泄漏账户私钥否则账户会被盗!!\n2.请备份账户私钥或account.json文件，否则账户丢失后无法找回!!", ButtonType.OK);
        alert.setWidth(600);
        alert.showAndWait();
        return true;
    }
    public static Boolean createAccount(String name, String password,String rePassword) {
        if (password.length()<8){
            Alert alert = new Alert(Alert.AlertType.WARNING, "密码不得少于8位", ButtonType.OK);
            alert.showAndWait();
            return false;
        }

        if (Objects.equals(name, "") || Objects.equals(password, "")){
            Alert alert = new Alert(Alert.AlertType.WARNING, "请输入有效信息", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        if(!Objects.equals(password, rePassword)){
            Alert alert = new Alert(Alert.AlertType.WARNING, "两次密码不同", ButtonType.OK);
            alert.showAndWait();
            return false;
        }

        for(Account ac : Account.getAccountList()){
            if (Objects.equals(ac.getName(), name)){
                Alert alert = new Alert(Alert.AlertType.WARNING, "账户名已存在", ButtonType.OK);
                alert.showAndWait();
                return false;
            }
        }

        Account account = Account.crateAccount(Account.generatePrivateKey(), name, password);
        if (account == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "创建失败", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        Account.addAccount(account);
        if(!saveAccountListOfDisk()){
            Alert alert = new Alert(Alert.AlertType.WARNING, "账户创建成功，但保存到磁盘失败，请重启！！", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        ChatGuiController.chatGuiController.updateAccountList();

        Alert alert = new Alert(Alert.AlertType.WARNING, "创建成功\n注意：\n1.不要泄漏账户私钥否则账户会被盗!!\n2.请备份账户私钥或account.json文件，否则账户丢失后无法找回!!", ButtonType.OK);
        alert.setWidth(600);
        alert.showAndWait();
        return true;
    }
}