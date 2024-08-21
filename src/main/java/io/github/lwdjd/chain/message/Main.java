package io.github.lwdjd.chain.message;

import com.alibaba.fastjson2.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

import static io.github.lwdjd.chain.message.processor.HtmlPageFetcher.getInput;
import static io.github.lwdjd.chain.message.processor.HtmlPageFetcher.getTx;
import static io.github.lwdjd.chain.message.processor.txList.getAddressTx;
import static io.github.lwdjd.chain.message.processor.txList.parseHitsList;

public class Main {
    public static void main(String[] args) {
        // 创建Scanner对象，从标准输入（键盘）读取数据
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("1.获取地址的交易列表(最近的10000个交易)\n2.获取地址的交易列表并获取输入数据\n3.获取指定交易的输入数据\n\n请输入需要使用的模式(数字)：");
            switch (scanner.nextLine()){
                case "1":
                    String address = "";
                    while (true) {
                        System.out.print("\n请输入需要获取的地址：");
                        address = scanner.nextLine();
                        if(!address.equals("")){
                            break;
                        }
                        System.out.println("地址不能为空，请重新输入！");
                    }
                    System.out.print("\n本软件目前仅支持OKTC test\n请输入需要获取地址所在的链：");
                    String chain = "";
                    String c = scanner.nextLine();
                    if  (!c.equals("OKTC test")){
                        System.out.println("\n本软件目前仅支持OKTC test，请重头开始!");
                        break;
                    }else if (c.equals("OKTC test")){
                        chain = "okexchain_test";
                    }
                    System.out.print("\n请输入Key：");
                    String key = scanner.nextLine();
                    System.out.println("\n正在获取中，请稍后.......");
                    List<Map<String, Object>> txList = new ArrayList<>();

                    int offset = 0;
                    while (true) {
                        String[] tx = getAddressTx(key, address, offset, 100, chain);
                        if (!tx[0].equals("0")) {
                            System.out.println("获取失败，请检查网络是否正常！");
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
                        txList.addAll(t);
                        offset = offset+t.size();
                        System.out.println("已获取 " + offset + " 个交易！");
                    }
                    if (txList.size() == 0) {
                        System.out.println("获取失败，请检查Key是否正确，网络是否正常！");
                    }
                    System.out.println("获取成功，共" + txList.size() + "条交易记录！");
                    System.out.print("\n是否需要输出交易列表？\n请输入yes或no:");
                    switch (scanner.nextLine()) {
                        case "yes":
                            for (int i = txList.size()-1 ;i>=0;i--) {
                                // 将字符串转换为 long 类型
                                long timestamp = Long.parseLong(txList.get(i).get("blocktime").toString());

                                // 使用 long 值创建 Date 对象
                                Date date = new Date(timestamp);

                                // 创建 SimpleDateFormat 对象，并定义日期时间格式
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                // 设置时区为 UTC+8
                                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+16:00"));

                                // 将 Date 对象格式化为字符串
                                String formattedDate = dateFormat.format(date);

                                System.out.println(formattedDate+": 交易"+txList.get(i).get("hash"));
                                System.out.println("                     发送方地址: " + txList.get(i).get("from"));
                                System.out.println("                     接收方地址: " + txList.get(i).get("to"));

                            }
                            System.out.println();
                            break;
                        case "no":
                            System.out.println("已取消输出！\n");
                            break;
                        default:
                            System.out.println("\n输入有误，请重新输入！");
                            System.out.print("\n是否需要输出交易列表？\n请输入yes或no:");
                            continue;
                    }
                    break;
                case "2":
                    String address_2 = "";
                    while (true) {
                        System.out.print("\n请输入需要获取的地址：");
                        address_2 = scanner.nextLine();
                        if(!address_2.equals("")){
                            break;
                        }
                        System.out.println("地址不能为空，请重新输入！");
                    }
                    System.out.print("\n本软件目前仅支持OKTC test\n请输入需要获取地址所在的链：");
                    String chain_2 = "";
                    String c_2 = scanner.nextLine();
                    if  (!c_2.equals("OKTC test")){
                        System.out.println("\n本软件目前仅支持OKTC test，请重头开始!");
                        break;
                    }else if (c_2.equals("OKTC test")){
                        chain_2 = "okexchain_test";
                    }
                    System.out.print("\n请输入Key：");
                    String key_2 = scanner.nextLine();
                    System.out.println("\n正在获取中，请稍后.......");
                    List<Map<String, Object>> txList_2 = new ArrayList<>();

                    int offset_2 = 0;
                    while (true) {
                        String[] tx = getAddressTx(key_2, address_2, offset_2, 100, chain_2);
                        if (!tx[0].equals("0")) {
                            System.out.println("获取失败，请检查网络是否正常！");
                            break;
                        }
                        // 解析JSON字符串为JSONObject
                        JSONObject jsonObject = JSONObject.parse(tx[1]);
                        // 检查错误代码是否为0，如果不是0则退出循环
                        if(!jsonObject.getString("code").equals("0")){
                            System.out.println("错误代码"+jsonObject.getString("code"));
                            break;
                        }
                        List<Map<String, Object>> t = parseHitsList(tx[1]);
                        if(t.size()==0){
                            break;
                        }
                        txList_2.addAll(t);
                        offset_2 = offset_2+t.size();
                        System.out.println("已获取 " + offset_2 + " 个交易！");
                    }
                    if (txList_2.size() == 0) {
                        System.out.println("获取失败，请检查Key是否正确，网络是否正常！");
                    }
                    System.out.println("获取成功，共" + txList_2.size() + "条交易记录！");
                    System.out.print("\n是否需要输出交易列表？\n请输入yes或no:");
                    switch (scanner.nextLine()) {
                        case "yes":
                            for (int i = txList_2.size()-1 ;i>=0;i--) {
                                // 将字符串转换为 long 类型
                                long timestamp = Long.parseLong(txList_2.get(i).get("blocktime").toString());

                                // 使用 long 值创建 Date 对象
                                Date date = new Date(timestamp);

                                // 创建 SimpleDateFormat 对象，并定义日期时间格式
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                // 设置时区为 UTC+8
                                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+16:00"));

                                // 将 Date 对象格式化为字符串
                                String formattedDate = dateFormat.format(date);

                                System.out.println(formattedDate+": 交易"+txList_2.get(i).get("hash"));
                                System.out.println("                     发送方地址: " + txList_2.get(i).get("from"));
                                System.out.println("                     接收方地址: " + txList_2.get(i).get("to"));
                                String[] Tx;
                                try {
                                    Tx = getTx("https://www.oklink.com/zh-hans/oktc-test/tx/"+txList_2.get(i).get("hash"));
                                }catch (Exception e){
                                    System.out.println("检索失败,地址错误或无法连接到服务器！\n");
                                    continue;
                                }
                                if (Tx[0].equals("0")){

                                    String[] txI =getInput(Tx[1]);
                                    if (txI[0].equals("0")){
                                        System.out.println("                     原始16进制数据："+txI[1]);
                                        System.out.println("                     UTF-8数据："+txI[2]);
                                    }
                                }else {
                                    System.out.println("检索失败，地址可能不存在或页面格式已更新！");
                                }

                            }
                            System.out.println();
                            break;
                        case "no":
                            System.out.println("已取消输出！\n");
                            break;
                        default:
                            System.out.println("\n输入有误，请重新输入！");
                            System.out.print("\n是否需要输出交易列表？\n请输入yes或no:");
                            continue;
                    }
                    break;
                case "3":
                    System.out.print("\n请输入需要检索的交易(OKTC test)：");
                    String address_3 = scanner.nextLine();
                    String[] Tx;
                    try {
                        Tx = getTx("https://www.oklink.com/zh-hans/oktc-test/tx/"+address_3);
                    }catch (Exception e){
                        System.out.println("检索失败,地址错误或无法连接到服务器！\n");
                        continue;
                    }
                    if (Tx[0].equals("0")){

                        String[] txI =getInput(Tx[1]);
                        if (txI[0].equals("0")){
                            System.out.println("检索成功！");
                            System.out.println("原始16进制数据："+txI[1]);
                            System.out.println("UTF-8数据："+txI[2]);
                            System.out.println();
                        }
                    }else {
                        System.out.println("检索失败，地址可能不存在或页面格式已更新！");
                    }
                    break;
                default:
                    System.out.println("\n输入错误，请重新输入！\n");
            }
        }
    }
}