package io.github.lwdjd.chain.message.web3;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static io.github.lwdjd.chain.message.processor.Message.hexToUtf8;

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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String transactionHash = "0x8b17cb96fcaff42185875807e87efe7f072a11265ed3de016e0073c3c4f79054";

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
        System.out.println(getTransaction(transactionHash).getFrom());
    }

}
