package io.github.lwdjd.chain.message.Network;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Network {
    private final HttpClient client = HttpClient.newHttpClient();
    private HttpRequest.Builder builder = null;
    public Network(HttpRequest.Builder builder){
        this.builder = builder;
    }
    public String get(String url) throws IOException, InterruptedException {
        HttpRequest request =  builder.uri(URI.create(url)).build();
        // 发送请求，获取响应对象
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 返回响应体的字符串内容
        return response.body();
    }
    /**
     * 不使用代理发送GET请求
     *
     * @param url 请求的url
     * @return 返回请求的结果
     */
    public static String get(String url,String key) throws Exception {
        // 创建一个HttpClient对象
        HttpClient client = HttpClient.newHttpClient();

        // 创建一个HttpRequest对象，设置请求方法为GET，请求地址为url加上params
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                //添加Key
                .header("Ok-Access-Key",key)
                .build();

        // 发送请求，获取响应对象
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 返回响应体的字符串内容
        return response.body();

    }
    /**
     * 不使用代理发送GET请求
     *
     * @param url 请求的url
     * @return 返回请求的结果
     */
    public static String getN(String url,String key) throws Exception {
        // 创建一个HttpClient对象
        HttpClient client = HttpClient.newHttpClient();

        // 创建一个HttpRequest对象，设置请求方法为GET，请求地址为url加上params
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                //添加Key

                .header("x-apikey",key)
                .build();

        // 发送请求，获取响应对象
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 返回响应体的字符串内容
        return response.body();

    }
}
