package io.github.lwdjd.chain.message.fxml;

import com.alibaba.fastjson2.JSONObject;
import io.github.lwdjd.chain.message.account.Account;
import io.github.lwdjd.chain.message.config.ConfigManager;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingGuiController implements Initializable {

    public VBox setting;
    public Pane detailsPane;
    public ListView<String> settingListView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingListView.getItems().addAll(
                "数据",
                "X-API-KEY"
                );
        settingListView.getSelectionModel().selectedItemProperty().addListener((obs, oldAccount, newAccount) -> {
            try {
                updateSettingPane(newAccount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void updateSettingPane(String selectedSetting) {
        if (selectedSetting == null) {
            detailsPane.getChildren().clear();
            return;
        }
        // 清除当前detailsPane中的所有内容
        detailsPane.getChildren().clear();

        // 创建账户详情页面的布局
        Pane accountdetailsPane = new Pane(); // 间距为10
        accountdetailsPane.getChildren().add(AccountDetailContent(selectedSetting));

        // 将账户详情页面的布局添加到setting中
        detailsPane.getChildren().add(accountdetailsPane);
    }
    private Node AccountDetailContent(String selectedSetting){
        switch (selectedSetting){
            case "数据":
                return DataSettingGui();
            case "X-API-KEY":
                return XApiKeySettingGui();
            default:
                return null;

        }
    }
    private Node DataSettingGui(){
        VBox detailContent = new VBox(10);



        detailContent.getChildren().addAll(

        );
        return detailContent;
    }
    private Node XApiKeySettingGui(){
        VBox detailContent = new VBox(10);


        HBox xApiKeyHBox = new HBox(10);
        Text xApiKeyText = new Text(" X-API-KEY: ");

        TextField xApiKeyTextField = new TextField();
        xApiKeyTextField.setPrefWidth(200);
        try {
            xApiKeyTextField.setText(ConfigManager.getConfig("config.json").getString("X-API-KEY"));
        }catch (Exception ignored){

        }


        Button saveButton = new Button("保存");
        saveButton.setOnAction(event -> {
            JSONObject config;
            try {
                config =ConfigManager.getConfig("config.json");
                if (config == null){
                    config = new JSONObject();
                }
            }catch (Exception e){
                e.printStackTrace();
                config = new JSONObject();
            }

            config.put("X-API-KEY", xApiKeyTextField.getText());
            if (!ConfigManager.saveConfig("config.json",config)){
                Alert alert = new Alert(Alert.AlertType.WARNING, "保存到磁盘失败，请重启！！", ButtonType.OK);
                ConfigManager.loadConfig("config.json");
                alert.showAndWait();
            }


        });

        xApiKeyHBox.getChildren().addAll(xApiKeyText,
                xApiKeyTextField,
                saveButton
        );
        detailContent.getChildren().addAll(
                new HBox(),
                xApiKeyHBox
        );
        return detailContent;
    }
}
