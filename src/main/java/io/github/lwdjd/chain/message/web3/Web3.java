package io.github.lwdjd.chain.message.web3;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static io.github.lwdjd.chain.message.processor.Message.hexToUtf8;
import static io.github.lwdjd.chain.message.processor.Message.utf8StringToHex;

public class Web3 {
    // 1 Ether等于10^18 Wei
    private static final BigDecimal WEI_TO_ETHER = new BigDecimal("1000000000000000000");

    /**
     * 将Wei转换为Ether。
     *
     * @param weiAmount 以Wei为单位的金额
     * @return 以Ether为单位的金额
     */
    public static BigDecimal convertWeiToEther(BigInteger weiAmount) {
        // 创建一个MathContext，指定小数点后18位，并使用ROUND_HALF_DOWN舍入模式
        MathContext mathContext = new MathContext(18, RoundingMode.HALF_DOWN);
        // 使用MathContext进行除法运算
        return new BigDecimal(weiAmount).divide(WEI_TO_ETHER, mathContext);
    }

    public static Web3j web3j_OKTC_test = Web3j.build(new HttpService("https://exchaintestrpc.okex.org"));
//    public static Web3j web3j_OKTC_test = Web3j.build(new WebSocketService("https://exchaintestws.okex.org:8443",false));

    public static BigDecimal getBalance(String address) throws ExecutionException, InterruptedException {
        EthGetBalance ethGetBalance = web3j_OKTC_test.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get(); // 使用sendAsync()来异步发送请求
        try {
            if (ethGetBalance != null) {
                return convertWeiToEther(ethGetBalance.getBalance());
            }
        }catch (Exception e){
            throw new RuntimeException(e+"\nError fetching balance: " + ethGetBalance.getError().getMessage());
        }
        return null;
    }

    public static BigInteger getTransactionCount(String address) {

        // 获取最新的区块号
        try {
            EthGetTransactionCount ethGetTransactionCount = web3j_OKTC_test.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
            return ethGetTransactionCount.getTransactionCount();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public static Transaction getTransaction(String transactionHash){
        try {
            Optional<Transaction> transaction =web3j_OKTC_test.ethGetTransactionByHash(transactionHash).sendAsync().get().getTransaction();
            return transaction.orElse(null);

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getTransactionUtf8InputData(Transaction transaction){

        return hexToUtf8(transaction.getInput());
    }
    public static EthBlock getBlock(String blockHash) throws ExecutionException, InterruptedException {
        // 使用区块哈希获取区块信息
        EthBlock ethBlock = web3j_OKTC_test
                .ethGetBlockByHash(blockHash, true)
                .sendAsync()
                .get();
        return ethBlock;
    }

    public static void main(String[] args) throws Exception {
//        String transactionHash = "0x8b17cb96fcaff42185875807e87efe7f072a11265ed3de016e0073c3c4f79054";

//        Optional<Transaction> ethGetTransactionByHash = web3j_OKTC_test.ethGetTransactionByHash(transactionHash).sendAsync().get().getTransaction();
//        if (ethGetTransactionByHash.isPresent()) {
//            // 获取from地址
//            String from = ethGetTransactionByHash.get().getFrom();
//            // 获取to地址，如果交易是合约创建交易，则此字段可能为空
//            String to = ethGetTransactionByHash.get().getTo();
//            // 获取输入数据
//            String input = ethGetTransactionByHash.get().getInput();
//
//            // 打印交易详细信息
//            System.out.println("From: " + from);
//            System.out.println("To: " + to);
////            System.out.println("Input Data: " + input);
//            System.out.println("字符串："+hexToUtf8(input));
//        }else {
//            System.out.println("Transaction not found");
//        }

        System.out.println(utf8StringToHex(""));
        try{
            System.out.println(sendTransactionWithMessage(
                    "0x58b986be5a5d4f850ff411c1b43a32fca1fd9c17cb962b0ce06e38de1de27fd5",
                    "0xf8d4697231165be7068fad392150ba988a2c8105",
                    ""
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to send transaction: " + e.getMessage());

        }
    }
//    // 发送带消息的以太坊交易的方法
//    public static String sendTransactionWithMessage(String fromPrivateKey, String toAddress, String message) throws Exception {
//        Credentials credentials = Credentials.create(fromPrivateKey);
//        BigInteger nonce = web3j_OKTC_test.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
//                .sendAsync()
//                .get()
//                .getTransactionCount();
//
//
//        // 将消息转换为十六进制
//        String hexMessage = "0x"+utf8StringToHex(message);
//
//        // 创建原始交易
//        RawTransaction rawTransaction = RawTransaction.createTransaction(
//                nonce,
//                BigInteger.valueOf(1000), // 这里使用0 gas price，因为消息不执行任何操作
//                BigInteger.valueOf(99999999), // 一个基本交易的gas limit
//                toAddress, // 接收者的地址
//                BigInteger.valueOf(0),//发送的数量
//                hexMessage // 消息作为input data
//        );
//
//// 6. 签名交易
//        // 签名交易
//        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
//
//
//        // 发送交易
//        EthSendTransaction ethSendTransaction = web3j_OKTC_test.ethSendRawTransaction(Numeric.toHexString(signedMessage))
//                .sendAsync()
//                .get();
//
//        // 返回交易哈希
//        return ethSendTransaction.getTransactionHash();
//    }
public static String sendTransactionWithMessage(String fromPrivateKey, String toAddress, String message) throws Exception {
    try {
        Credentials credentials = Credentials.create(fromPrivateKey);
        BigInteger nonce = web3j_OKTC_test.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get()
                .getTransactionCount();

        String hexMessage = "0x" + utf8StringToHex(message);

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                BigInteger.valueOf(1000), // 合理设置 gas price
                BigInteger.valueOf(21000),      // 合理设置 gas limit
                toAddress,
                BigInteger.ZERO,                   // 不发送以太币
                hexMessage);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);

        EthSendTransaction ethSendTransaction = web3j_OKTC_test.ethSendRawTransaction(Numeric.toHexString(signedMessage))
                .sendAsync()
                .get();

        if (ethSendTransaction == null || ethSendTransaction.getTransactionHash() == null) {
            // 处理错误情况
            throw new Exception("Transaction was not sent successfully");
        }

        return ethSendTransaction.getTransactionHash();
    } catch (InterruptedException | ExecutionException e) {
        // 处理可能的中断或执行异常
        throw new Exception("Error sending transaction", e);
    }
}
//    public static String sendTransaction(String fromPrivateKey, String toAddress, String message) throws Exception {
//        Credentials credentials = Credentials.create(fromPrivateKey);
//        TransactionReceipt transactionReceipt = Transfer.sendFunds(
//                web3j_OKTC_test, credentials, toAddress,
//                BigDecimal.valueOf(0.001), Convert.Unit.ETHER,new BigInteger("65")).send();
//        return transactionReceipt.getTransactionHash();
//    }
}
