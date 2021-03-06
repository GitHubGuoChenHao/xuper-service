package com.baidu.xuperunion.api;

import com.baidu.xuperunion.pb.XchainOuterClass;
import org.bouncycastle.util.encoders.Hex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNoException;

public class XuperClientTest {
    private Account account;
    private XuperClient client;

    @Before
    public void setUp() {
        try {
            client = new XuperClient("127.0.0.1:37101");
            // test connection
            client.getBlockingClient().getSystemStatus(XchainOuterClass.CommonIn.newBuilder().build());
            String keyPath = Paths.get("/Users/guo/Desktop/baidu/uap/xuper-service/src/test/resources/com/baidu/xuperunion/api/keys").toString();
            account = Account.create(keyPath);
        } catch (Exception e) {
            assumeNoException(e);
        }
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void transfer() throws Exception {
        String txid = client.transfer(account, "XC1111111111111111@xuper", BigInteger.valueOf(1)).getTxid();
        System.out.println("transfer " + txid);

        //XchainOuterClass.InternalBlock block = client.queryBlock();
    }

    @Test
    public void getBalance() throws Exception {
        Account bob = Account.create();
        BigInteger amount = BigInteger.valueOf(100);
        Transaction tx = client.transfer(account, bob.getAddress(), amount);
        System.out.println("transfer to bob " + tx.getTxid());

        BigInteger result = client.getBalance(bob.getAddress());
        System.out.println("@@@"+result);
        assertEquals(amount.longValue(), result.longValue());
    }

    @Test
    public void invokeContract() throws Exception {
        Map<String, byte[]> args = new HashMap<>();
        args.put("owner", "ww".getBytes());
        args.put("filehash", "333".getBytes());
        args.put("timestamp", "333".getBytes());
        args.put("filehash", "333".getBytes());
        args.put("timestamp", "333".getBytes());
        Transaction tx = client.invokeContract(account, "wasm", "eleccert", "Save", args);
        System.out.println("invoke txid: " + tx.getTxid());
        System.out.println("response: " + tx.getContractResponse().getBodyStr());
        System.out.println("gas: " + tx.getGasUsed());
    }

    @Test
    public void queryContract() throws Exception {
        Map<String, byte[]> args = new HashMap<>();
        args.put("owner", "gch".getBytes());
        args.put("filehash", "123".getBytes());
        System.out.println("@@@"+args);
//        Transaction tx = client.queryContract(account, "wasm", "eleccert", "Query", args);
//        System.out.println("response: " + tx.getContractResponse().getBodyStr());
//        System.out.println("gas: " + tx.getGasUsed());
    }

    @Test
    public void deployWasmContract() throws Exception {
        account.setContractAccount("XC1111111111111114@xuper");
        Map<String, byte[]> args = new HashMap<>();
        args.put("creator", "gch".getBytes());
//        String codePath = getClass().getResource("counter.wasm").getFile();
        byte[] code = Files.readAllBytes(Paths.get("/Users/guo/Desktop/uap/xuper-service/src/main/contract/counter.wasm"));
        Transaction tx = client.deployWasmContract(account, code, "counterss", "c", args);
        System.out.println("deploy contract " + tx);
    }

    @Test
    public void createContractAccount() throws Exception {
        Transaction tx = client.createContractAccount(account, "1111111111");
        System.out.println("create account " + tx.getTxid());

        // waiting create account tx confirmed
        sleep(4000);
    }

    @Test
    public void queryTx() throws Exception {
//        String txid = client.transfer(account, "XC1111111111111111@xuper", BigInteger.valueOf(10)).getTxid();
//        XchainOuterClass.Transaction tx = client.queryTx(txid);
//
//        assertEquals(txid, Hex.toHexString(tx.getTxid().toByteArray()));

        //String jsonMessage = '{"语文":"88","数学":"78","计算机":"99"}';

//        JSONObject myJson = JSONObject.fromObject(jsonMessage);
//
//        Map m = myJson;
    }

    @Test
    public void queryBlock() throws Exception {
        String txid = client.transfer(account, "XC1111111111111111@xuper", BigInteger.valueOf(10)).getTxid();
        // waiting for confirmed
        sleep(4000);
        XchainOuterClass.Transaction tx = client.queryTx(txid);
        assertEquals(txid, Hex.toHexString(tx.getTxid().toByteArray()));

        String blockid = Hex.toHexString(tx.getBlockid().toByteArray());
        XchainOuterClass.InternalBlock block = client.queryBlock(blockid);
        System.out.println(Hex.toHexString(block.getPreHash().toByteArray()));
        assertEquals(blockid, Hex.toHexString(block.getBlockid().toByteArray()));

    }

    @Test
    public void apiExample() throws Exception {
        createContractAccount();
        transfer();
        deployWasmContract();
        invokeContract();
        queryContract();
        queryTx();
        queryBlock();
        getBalance();
    }


}
