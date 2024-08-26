package io.github.lwdjd.chain.message.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final Map<String, JSONObject> configFiles = new HashMap<>();
    // 路径前缀，指向类路径中的资源文件夹
    private static final String CLASSPATH_PREFIX = "/io/github/lwdjd/chain/message/defaultConfig/";


    public static boolean loadConfig(String relativePath) {
        // 首先尝试从文件系统加载配置文件
        try {
            Path configFilePath = Paths.get(relativePath);
            if (Files.exists(configFilePath)) {
                String fileContent = new String(Files.readAllBytes(configFilePath), StandardCharsets.UTF_8);
                JSONObject config = JSON.parseObject(fileContent);
                configFiles.put(relativePath, config);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 文件系统读取失败，继续尝试从类路径加载
        }

        // 文件系统读取失败或文件不存在，尝试从类路径加载默认配置
        try (InputStream is = ConfigManager.class.getResourceAsStream(CLASSPATH_PREFIX+relativePath)) {
            if (is != null) {
                String defaultContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                JSONObject defaultConfig = JSON.parseObject(defaultContent);
                configFiles.put(relativePath, defaultConfig);
                saveDefaultConfig(relativePath);
                return true;
            } else {
                // 类路径中也未找到配置文件
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 保存配置文件的方法，接收配置的相对路径和JSONObject对象
    public static boolean saveConfig(String relativePath, JSONObject config) {
        try {
            // 获取配置文件的路径和父目录路径
            Path path = Paths.get(relativePath);
            Path dirPath = path.getParent();

            // 确保配置文件所在的目录存在
            if (dirPath != null && !Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 将JSONObject转换为JSON字符串并写入文件
            String configJson = JSON.toJSONString(config);
            Files.writeString(path, configJson);

            // 更新内存中的配置缓存
            configFiles.put(relativePath, config);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取配置文件
    public static JSONObject getConfig(String relativePath) {
        return configFiles.get(relativePath);
    }

    // 保存默认配置文件到指定路径
    public static boolean saveDefaultConfig(String relativePath) {
        // 尝试从类路径加载默认配置文件
        String defaultConfigPath = CLASSPATH_PREFIX + relativePath;
        try (InputStream is = ConfigManager.class.getResourceAsStream(defaultConfigPath)) {
            if (is != null) {
                // 如果找到了默认配置文件，读取内容
                String defaultContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                JSONObject defaultConfig = JSON.parseObject(defaultContent);
                return saveConfig(relativePath, defaultConfig);
            }
            // 如果没有找到，返回false
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updataConfig(String relativePath, JSONObject newConfig) {
        try {
            // 覆盖私有变量中的配置文件
            configFiles.put(relativePath, newConfig);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        // （可选）保存到文件系统，如果需要持久化
        // saveConfig(relativePath, newConfig);
        return true;
    }
    public static void main(String[] args) {
        InputStream is = ConfigManager.class.getResourceAsStream("/io/github/lwdjd/chain/message/defaultConfig/config.json");
        if (is != null) {
            try {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Successfully loaded default config: " + content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Default config not found in classpath.");
        }
    }
}