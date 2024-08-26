package io.github.lwdjd.chain.message.fxml;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class AccountSetting extends Application {
    private static Stage stage;
    @Override
    public void start(Stage primaryStage) {
        try {
            // 加载 FXML 文件
            URL location = getClass().getResource("/io/github/lwdjd/chain/message/fxml/AccountSettingGui.fxml");
            // 创建 FXMLLoader 对象
            FXMLLoader loader = new FXMLLoader(location);
//            // 设置控制器类
//            loader.setController(new ChatGuiController());
            // 加载 FXML 文档并获取根节点
            Parent root = loader.load();

            // 创建 Scene 对象，并将根节点添加到 Scene 中
            Scene scene = new Scene(root);
            primaryStage.setTitle("Chain Message - 账户设置");
            // 设置窗口无法调整大小
            primaryStage.setResizable(false);

            // 设置窗口关闭事件处理器
            primaryStage.setOnCloseRequest(event -> {
                setStage(null);
            });;
            // 设置 Stage 的场景并显示 Stage
            primaryStage.setScene(scene);
            primaryStage.show();
            setStage(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getStage() {
        return stage;
    }
    private static void setStage(Stage stage) {
        AccountSetting.stage = stage;
    }
}
