package io.github.lwdjd.chain.message.processor;

import com.alibaba.fastjson2.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.lwdjd.chain.message.processor.txList.getAddressTx;
import static io.github.lwdjd.chain.message.processor.txList.parseHitsList;

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
    public static List<Map<String,Object>> getTxList(String key_2,String address_2 ,String chain_2) {
        List<Map<String, Object>> txList_2 = new ArrayList<>();

        int offset_2 = 0;
        try {
            while (true) {
                String[] tx = getAddressTx(key_2, address_2, offset_2, 100, chain_2);
                if (!tx[0].equals("0")) {
                    break;
                }
                // 解析JSON字符串为JSONObject
                JSONObject jsonObject = JSONObject.parse(tx[1]);
                // 检查错误代码是否为0，如果不是0则退出循环
                if(!jsonObject.getString("code").equals("0")){
                    break;
                }
                List<Map<String, Object>> t = parseHitsList(tx[1]);
                if(t.size()==0){
                    break;
                }
                txList_2.addAll(t);
                offset_2 = offset_2+t.size();
            }
        }catch (Exception e){
            return null;
        }
        return txList_2;
    }

    /**
     *
     * @param key_2 X-API-KEY
     * @param address_2 地址
     * @param chain_2 链
     * @param offset_2 起始位置
     * @param once_2 结束位置（最大9999）
     * @return 交易列表
     */
    public static List<Map<String,Object>> getArrayTxList(String key_2,String address_2,String chain_2,int offset_2,int once_2) {
        List<Map<String, Object>> txList_2 = new ArrayList<>();

        try {
            while (offset_2 <= once_2) {
                String[] tx = getAddressTx(key_2, address_2, offset_2, (once_2-offset_2)>100?100:offset_2, chain_2);
                if (!tx[0].equals("0")) {
                    break;
                }
                // 解析JSON字符串为JSONObject
                JSONObject jsonObject = JSONObject.parse(tx[1]);
                // 检查错误代码是否为0，如果不是0则退出循环
                if(!jsonObject.getString("code").equals("0")){
                    break;
                }
                List<Map<String, Object>> t = parseHitsList(tx[1]);
                if(t.size()==0){
                    break;
                }
                txList_2.addAll(t);
                offset_2 = offset_2+t.size();
            }
        }catch (Exception e){
            return null;
        }
        return txList_2;
    }


    public static void main(String[] args) {
//        System.out.println(getTxList(
//                "LWIzMWUtNDU0Ny05Mjk5LWI2ZDA3Yjc2MzFhYmEyYzkwM2NjfDI4MzUzNTIzODg5MDUwNDM=",
//                "0x2b622ab34d01a2d01e405225711595395caf404b",
//                "okexchain_test")
//        );
        System.out.println(getArrayTxList(
                "LWIzMWUtNDU0Ny05Mjk5LWI2ZDA3Yjc2MzFhYmEyYzkwM2NjfDI4MzUzNTIzODg5MDUwNDM=",
                "0x2b622ab34d01a2d01e405225711595395caf404b",
                "okexchain_test",
                14,
                100
                ).size()
        );
    }


}
