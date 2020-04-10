package com.vcredit.tools.excel.data;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @Author: tangdandan
 * @Date: 2020/4/9 16:00
 */
@Data
public class FileData {
    @ExcelProperty(index = 0)
    private String data;
}
