package com.baidu.controller;

import com.baidu.common.AjaxResult;
import com.baidu.entity.Contract;
import com.baidu.xuperunion.api.Account;
import com.baidu.xuperunion.api.Transaction;
import com.baidu.xuperunion.api.XuperClient;
import com.baidu.xuperunion.pb.XchainOuterClass;
import io.swagger.annotations.*;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;


/**
 * @Auther: gch
 * @Date: 2019/12/25 09:54
 * @Description: 链账户操作
 */

@Api(value = "链账户操作", description = "链账户操作")
@RestController
@RequestMapping("/account")
public class AccountController {

    private Logger logger = LoggerFactory.getLogger(AccountController.class);

    private Account account;
    private XuperClient client;

    private Account setUp(){
        try {
            client = new XuperClient("192.168.45.204:37101");
            // connection
            client.getBlockingClient().getSystemStatus(XchainOuterClass.CommonIn.newBuilder().build());
            String keyPath = Paths.get("/Users/guo/Desktop/uap/xuper-service/src/test/resources/com/baidu/xuperunion/api/keys").toString();
            account = Account.create(keyPath);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return account;
    }

    private void tearDown() {
        client.close();
    }

    private  Map<String, byte[]> contract(Map<String, Object> argss){
        Map<String, byte[]> args = new HashMap<>();
        for(String key : argss.keySet()){
            args.put(key,argss.get(key).toString().getBytes());
        }
        return args;
    }


    @ApiOperation(value = "创建账户",httpMethod = "GET",produces = "application/xml,application/json")
    @GetMapping("/createAccount")
    @ResponseBody
    public AjaxResult createAccount() {

        try {
            setUp();
            account = Account.create();
            tearDown();
        } catch (Exception e) {
            return AjaxResult.error(e.toString());
        }

        return AjaxResult.success("Address:"+account.getAddress());
    }


    @ApiOperation(value="创建合约账户",httpMethod = "GET",produces = "application/xml,application/json")
    @ApiImplicitParam(name = "contractAccountName",value = "合约账号名称（长度预期为16）",required = true)
    @GetMapping("/createContractAccount")
    @ResponseBody
    public AjaxResult createContractAccount(String contractAccountName){

        Transaction tx;
        try {
            setUp();
            tx= client.createContractAccount(account, contractAccountName);
            sleep(4000);
            tearDown();
        } catch (Exception e) {
            return AjaxResult.error(e.toString());
        }

        return AjaxResult.success("Txid:"+tx.getTxid());
    }


    @ApiOperation(value="转账",httpMethod = "GET",produces = "application/xml,application/json")
    @ApiImplicitParams({@ApiImplicitParam(name = "accountOrContractAccountName",value = "账号（合约账号）名称",required = true),
                        @ApiImplicitParam(name = "money",value = "钱",required = true,dataType = "Long")})
    @GetMapping("/transfer")
    @ResponseBody
    public AjaxResult transfer(String accountOrContractAccountName,Long money){

        String txid;
        try {
            setUp();
            txid = client.transfer(account, accountOrContractAccountName, BigInteger.valueOf(money)).getTxid();
            tearDown();
        } catch (Exception e) {
            return AjaxResult.error(e.toString());
        }

        return AjaxResult.success("Txid:"+txid);
    }


    @ApiOperation(value="账户余额",httpMethod = "GET",produces = "application/xml,application/json")
    @ApiImplicitParams({@ApiImplicitParam(name = "address",value = "账号名称",required = true)})
    @GetMapping("/getBalance")
    @ResponseBody
    public AjaxResult getBalance(String address) {

        BigInteger result;
        try {
            setUp();
            result = client.getBalance(address);
            tearDown();
        } catch (Exception e) {
            return AjaxResult.error(e.toString());
        }

        return AjaxResult.success("balance:"+result);
    }


    @ApiOperation(value="部署合约",httpMethod = "GET",produces = "application/xml,application/json")
    @ApiImplicitParams({@ApiImplicitParam(name = "ContractAccountName",value = "合约账号名称",required = true),
                        @ApiImplicitParam(name = "creator",value = "创建者",required = true),
                        @ApiImplicitParam(name = "wasmFile",value = "wasm文件地址",required = true),
                        @ApiImplicitParam(name = "contract",value = "合约名字",required = true),
                        @ApiImplicitParam(name = "runtime",value = "运行语言",required = true)})
    @GetMapping("/deployWasmContract")
    @ResponseBody
    public AjaxResult deployWasmContract(String ContractAccountName,String creator,String wasmFile,String contract,String runtime) {

        Transaction tx;
        try {
            setUp();
            account.setContractAccount(ContractAccountName);
            Map<String, byte[]> args = new HashMap<>();
            args.put("creator", creator.getBytes());
            byte[] code = Files.readAllBytes(Paths.get(wasmFile));
            tx = client.deployWasmContract(account, code, contract, runtime, args);
            tearDown();
        } catch (Exception e) {
            return AjaxResult.error(e.toString());
        }

        return AjaxResult.success("Txid:"+tx.getTxid());
    }


    @ApiOperation(value="调用合约",httpMethod = "POST",produces = "application/xml,application/json")
    @ApiImplicitParam(name = "contract", value = "contract信息", required = true, dataType = "Contract")
    @PostMapping("/invokeContract")
    @ResponseBody
    public AjaxResult invokeContract(@RequestBody Contract contract) {

        Transaction tx;
        try {
            setUp();
            Map<String, byte[]> args = contract(contract.getArgss());
            tx = client.invokeContract(account,contract.getModule(), contract.getContract(), contract.getMethod(), args);
            tearDown();
        } catch (Exception e) {
            return AjaxResult.error(e.toString());
        }
        return AjaxResult.success("Txid:"+tx.getTxid()+";Gas:"+tx.getGasUsed());
    }


    @ApiOperation(value="查询合约",httpMethod = "POST",produces = "application/xml,application/json")
    @ApiImplicitParam(name = "contract", value = "contract信息", required = true, dataType = "Contract")
    @PostMapping("/queryContract")
    @ResponseBody
    public AjaxResult queryContract(@RequestBody Contract contract) {

        Transaction tx;
        try {
            setUp();
            Map<String, byte[]> args = contract(contract.getArgss());
            tx = client.queryContract(account, contract.getModule(), contract.getContract(), contract.getMethod(), args);
            tearDown();
        } catch (Exception e) {
            return AjaxResult.error(e.toString());
        }

        return AjaxResult.success("response:"+tx.getContractResponse().getBodyStr()+";Gas:"+tx.getGasUsed());
    }


    @ApiOperation(value="查询Tx信息",httpMethod = "GET",produces = "application/xml,application/json")
    @ApiImplicitParams({@ApiImplicitParam(name = "txid",value = "查询的TxId",required = true)})
    @GetMapping("/queryTx")
    @ResponseBody
    public AjaxResult queryTx(String txid) {

        XchainOuterClass.Transaction tx;
        try {
            setUp();
            tx = client.queryTx(txid);
            tearDown();
        } catch (Exception e) {
            return AjaxResult.error(e.toString());
        }

        return AjaxResult.success("tx:"+tx.toString()+";blockid:"+Hex.toHexString(tx.getBlockid().toByteArray()));
    }


    @ApiOperation(value="查询Block信息",httpMethod = "GET",produces = "application/xml,application/json")
    @ApiImplicitParams({@ApiImplicitParam(name = "blockid",value = "查询的BlockId",required = true)})
    @GetMapping("/queryBlock")
    @ResponseBody
    public AjaxResult queryBlock(String blockid) {

        XchainOuterClass.InternalBlock block;
        try {
            setUp();
            block = client.queryBlock(blockid);
            tearDown();
        } catch (Exception e) {
            return AjaxResult.error(e.toString());
        }

        return AjaxResult.success("block:"+block);
    }




}
