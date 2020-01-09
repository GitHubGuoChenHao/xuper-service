package com.baidu.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Auther: gch
 * @Date: 2019/12/26 11:56
 * @Description:智能合约参数对象化
 */

@Data
public class Contract implements Serializable {

    private static final long serialVersionUID = 2191738960325446405L;

    @ApiModelProperty(value = "模块合约,通常是wasm", required = true)
    private String module;

    @ApiModelProperty(value = "合约名称", required = true)
    private String contract;

    @ApiModelProperty(value = "合约方法", required = true)
    private String method;

    @ApiModelProperty(value = "合约方法参数", required = true)
    private Map<String, Object> argss;

}
