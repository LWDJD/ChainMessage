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
import java.nio.charset.StandardCharsets;
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
//



//        message.append(String.valueOf(message).repeat(250000));
        for(int i =0;i<100;i++) {
            StringBuilder message = new StringBuilder("我测试一下"+i);
            byte[] bytes = message.toString().getBytes(StandardCharsets.UTF_8);
            long byteCount = bytes.length; // 获取字节数组的长度


//            System.out.println("调用sendTransaction方法");
            try {
                EthSendTransaction transaction = sendTransaction(
                        "58b986be5a5d4f850ff411c1b43a32fca1fd9c17cb962b0ce06e38de1de27fd5",
                        "0xf8d4697231165be7068fad392150ba988a2c8105",
                        message.toString(),
                        BigInteger.valueOf(3),
                        BigInteger.valueOf(byteCount).multiply(BigInteger.valueOf(68)).add(BigInteger.valueOf(21000)),
                        BigInteger.valueOf(i)
                );
                System.out.println("TxHash:   " +transaction.getTransactionHash());
                if (transaction.getError()!=null){
                    System.out.println("Error:   " + transaction.getError().getMessage());
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to send transaction: " + e.getMessage());


            }
        }
    }
    /**
     * 发送交易
     * @param fromPrivateKey 地址
     * @param toAddress 目标地址
     * @param message 附加信息
     * @param gasPrice gas价格
     * @param gasLimit gas限制量
     * @return 返回交易信息
     */
    public static EthSendTransaction sendTransaction(String fromPrivateKey, String toAddress, String message,BigInteger gasPrice,BigInteger gasLimit) throws Exception {
        // 使用私钥创建Credentials对象
        Credentials credentials = Credentials.create(fromPrivateKey);

        // 获取nonce
        EthGetTransactionCount ethGetTransactionCount = web3j_OKTC_test.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        // 获取当前链的chainId
        EthChainId ethChainId = web3j_OKTC_test.ethChainId().sendAsync().get();
        long chainId = ethChainId.getChainId().longValue();

        // 将消息转换为16进制
        String hexMessage = "0x"+utf8StringToHex(message);

        // 创建RawTransaction
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice, // gas price, 单价
                gasLimit, // gas limit, gas数量限制
                toAddress, // to address
                hexMessage // data
        );

        // 签名交易
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);


        String signedRawTx = Numeric.toHexString(signedMessage);
        // 发送原始交易
        System.out.println("开始发送交易");
        // 返回交易
        return web3j_OKTC_test.ethSendRawTransaction(signedRawTx).sendAsync().get();
    }

    /**
     * 发送交易
     * @param fromPrivateKey 地址
     * @param toAddress 目标地址
     * @param message 附加信息
     * @param gasPrice gas价格
     * @param gasLimit gas限制量
     * @param nonceAdd 需要添加的nonce数量
     * @return 返回交易信息
     */
    public static EthSendTransaction sendTransaction(String fromPrivateKey, String toAddress, String message,BigInteger gasPrice,BigInteger gasLimit,BigInteger nonceAdd) throws Exception {
        // 使用私钥创建Credentials对象
        Credentials credentials = Credentials.create(fromPrivateKey);

        // 获取nonce
        EthGetTransactionCount ethGetTransactionCount = web3j_OKTC_test.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        nonce = nonce.add(nonceAdd);
        // 获取当前链的chainId
        EthChainId ethChainId = web3j_OKTC_test.ethChainId().sendAsync().get();
        long chainId = ethChainId.getChainId().longValue();

        // 将消息转换为16进制
        String hexMessage = "0x"+utf8StringToHex(message);

        // 创建RawTransaction
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice, // gas price, 单价
                gasLimit, // gas limit, gas数量限制
                toAddress, // to address
                hexMessage // data
        );

        // 签名交易
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);


        String signedRawTx = Numeric.toHexString(signedMessage);
        // 发送原始交易
        System.out.println("开始发送交易");
        // 返回交易
        return web3j_OKTC_test.ethSendRawTransaction(signedRawTx).sendAsync().get();
    }
}
