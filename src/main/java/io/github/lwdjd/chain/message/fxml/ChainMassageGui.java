package io.github.lwdjd.chain.message.fxml;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class ChainMassageGui extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 加载 FXML 文件
            URL location = getClass().getResource("/io/github/lwdjd/chain/message/fxml/ChainMassageGui.fxml");
            // 创建 FXMLLoader 对象
            FXMLLoader loader = new FXMLLoader(location);
//            // 设置控制器类
//            loader.setController(new ChatGuiController());
            // 加载 FXML 文档并获取根节点
            Parent root = loader.load();

            // 创建 Scene 对象，并将根节点添加到 Scene 中
            Scene scene = new Scene(root);
            primaryStage.setTitle("Chain Message GUI");
            // 设置窗口无法调整大小
            primaryStage.setResizable(false);
            // 设置窗口关闭事件处理器
            primaryStage.setOnCloseRequest(event -> {
                // 退出程序
                Platform.exit();
                System.exit(0);
            });;

            // 设置 Stage 的场景并显示 Stage
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}

