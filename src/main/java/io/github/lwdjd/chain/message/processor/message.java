package io.github.lwdjd.chain.message.processor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class message {
//    public static String hexToUtf8String(String hexStr) {
//        // 去除字符串开头的"0x"或"0X"
//        hexStr = hexStr.replaceAll("0x|0X", "").trim();
//        // 将字符串转换为小写以统一处理
//        hexStr = hexStr.toLowerCase();
//
//        // 处理中文字符，因为中文字符可能占用多个字节
//        int len = hexStr.length();
//        int start = 0;
//        StringBuilder sb = new StringBuilder();
//        while (start < len) {
//            if (hexStr.startsWith("00", start) && (start + 4 <= len)) {
//                // 跳过UTF-8编码中的00字节
//                start += 4;
//            } else {
//                // 每两个字符为一组，表示一个字节
//                String byteString = hexStr.substring(start, start + 2);
//                int byteValue = Integer.parseInt(byteString, 16);
//                sb.append((char) byteValue);
//                start += 2;
//            }
//        }
//        return sb.toString();
//    }
    /**
     * 将16进制字符串转换为UTF-8字符串。
     *
     * @param hexString 16进制字符串
     * @return UTF-8编码的字符串
     */
    public static String hexToUtf8(String hexString) {
        // 去除字符串中的空格
        hexString = hexString.replaceAll(" ", "");

        // 确保16进制字符串长度为偶数
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");
        }

        // 将16进制字符串转换为字节数组
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            // 将两个16进制字符转换为一个字节
            String byteString = hexString.substring(i, i + 2);
            bytes[i / 2] = (byte) (Integer.parseInt(byteString, 16) & 0xFF);
        }

        // 使用UTF-8编码创建字符串
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8是Java的标准编码，通常不会抛出此异常
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }
    public static String utf8StringToHex(String str) {
        StringBuilder hex = new StringBuilder();
        // 将字符串转换为字节数组
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        // 遍历字节数组，将每个字节转换为16进制表示
        for (byte b : strBytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

}
