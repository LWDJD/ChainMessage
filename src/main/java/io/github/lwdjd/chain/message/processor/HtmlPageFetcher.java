package io.github.lwdjd.chain.message.processor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HtmlPageFetcher {
    public static String[] getHtml(String url) {
//        String url = "https://www.oklink.com/zh-hans/oktc-test/tx/C423FBD263C76B4D890F3DCD132118F3B22AEF2651771904819E36F5F7F5703D";
        String errorCode = "1";
        String[] r={errorCode};
        try {
            // 获取网页内容
            Document document = Jsoup.connect(url).get();
            String html = document.html();
            errorCode = "0";
            r = new String[]{errorCode,html};

        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;

    }

    public static String[] getTx(String url) {
//        String url = "https://www.oklink.com/zh-hans/oktc-test/tx/C423FBD263C76B4D890F3DCD132118F3B22AEF2651771904819E36F5F7F5703D";
        String errorCode = "1";
        String[] r={errorCode};
        try {
            // 获取网页内容
            Document document = Jsoup.connect(url).get();

            // 使用Jsoup选择器定位script元素
            // 注意：这里使用的是Jsoup选择器语法，不是XPath
            Elements scriptElements = document.select("body > div:nth-of-type(1) > div > script:first-of-type");

            // 检查是否有匹配的script元素，并输出其文本内容
            if (!scriptElements.isEmpty()) {
                Element firstScript = scriptElements.first();
                if (firstScript != null) {
                    String scriptText = firstScript.html();
//                    System.out.println(scriptText);
                    errorCode = "0";
                    r = new String[]{errorCode,scriptText};
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;

    }
    public static String[] getInput(String jsonString) {
        String errorCode = "1";
        String[] r={errorCode};
        try {
            // 解析JSON字符串为JSONObject
            JSONObject jsonObject = JSONObject.parse(jsonString);

            // 按照提供的顺序访问JSON对象
            JSONObject appContext = jsonObject.getJSONObject("appContext");
            JSONObject initialProps = appContext.getJSONObject("initialProps");
            JSONObject store = initialProps.getJSONObject("store");
            JSONObject pageState = store.getJSONObject("pageState");
            JSONObject tradeInfoStore = pageState.getJSONObject("tradeInfoStore");
            JSONObject tradeInfo = tradeInfoStore.getJSONObject("tradeInfo");

            // 提取input和inputData字段
            String input = tradeInfo.getString("input");
            String inputData = tradeInfo.getString("inputData");
            errorCode = "0";
            // 输出结果
            r = new String[]{errorCode, input, inputData};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;

    }
}
