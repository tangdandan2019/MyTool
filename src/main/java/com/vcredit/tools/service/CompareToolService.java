package com.vcredit.tools.service;

import com.vcredit.tools.excel.data.RespDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * @Author: tangdandan
 * @Date: 2020/4/9 15:08
 */
public interface CompareToolService {

    /**
     * 比较数据
     * @param file1
     * @param file2
     * @return
     */
    Optional<RespDto> compareData(MultipartFile file1, MultipartFile file2);
}
