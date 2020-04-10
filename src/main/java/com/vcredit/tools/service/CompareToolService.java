package com.vcredit.tools.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    List compareData(MultipartFile file1, MultipartFile file2);
}
