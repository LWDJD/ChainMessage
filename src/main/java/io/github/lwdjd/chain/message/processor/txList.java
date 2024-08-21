package io.github.lwdjd.chain.message.processor;

import com.alibaba.fastjson2.JSON;
import io.github.lwdjd.chain.message.Network.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class txList {
    public static String[] getAddressTx(String key,String addresses,int offset,int limit,String chain){
        String data = "";
        String[] r = {"1"};
        try {
            /*
            from :发送方的地址
            to ：接收方的地址
            offset ：从第N条之后开始（最多9999）
            limit ：每次查询的数量（最多100条）
            address ：地址
            tokenAddress ：与address填相同
            nonzeroValue ：未知（建议false)
            t :unix时间戳（毫秒级）
             */
            data = Network.getN("https://www.oklink.com/api/explorer/v1/"+chain+"/addresses/"+addresses+"/transactions/condition?offset="+offset+"&limit="+limit+"&address="+addresses+"&tokenAddress="+addresses+"&nonzeroValue=false",key);
            r = new String[]{"0", data};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return r;
    }

    public static List<Map<String, Object>> parseHitsList(String jsonData) {
        // 使用 Fastjson 解析 JSON 数据到 Map
        Map<String, Object> dataMap = JSON.parseObject(jsonData, Map.class);

        // 从 Map 中获取 "data" 对象
        Map<String, Object> data = (Map<String, Object>) dataMap.get("data");

        // 从 "data" 对象中获取 "hits" 列表
        if(data.size()==0){
            return new ArrayList<>();
        }else if(data.get("hits")==null){
            return new ArrayList<>();
        }


        return (List<Map<String, Object>>) data.get("hits");
    }
//    public static void main(String[] args) {
//        String[] data = getAddressTx("LWIzMWUtNDU0Ny05Mjk5LWI2ZDA3Yjc2MzFhYmEyYzkwM2NjfDI4MzUzNTIzODg5MDUwNDM=","0x8d86bc475bedcb08179c5e6a4d494ebd3b44ea8b",0,100,"okexchain_test");
//        if(data[0].equals("0")){
//            System.out.println("原始数据："+data[1]);
//            List<Map<String, Object>> b =parseHitsList(data[1]);
//            System.out.println("处理后的数据:"+b.get(0));
//        }else{
//            System.out.println("error");
//        }
//    }
}
