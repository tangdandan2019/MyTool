package com.vcredit.tools.excel.data;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @Author: tangdandan
 * @Date: 2020/4/16 17:49
 */
@Data
public class RespDto {
    private ArrayList<String> diffData;
    private ArrayList<String> repeatData;
}
