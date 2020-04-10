package com.vcredit.tools.service.impl;

import com.alibaba.excel.EasyExcel;
import com.vcredit.tools.excel.data.FileData;
import com.vcredit.tools.excel.listener.FileCompareListener;
import com.vcredit.tools.excel.listener.FileImportDataListener;
import com.vcredit.tools.service.CompareToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: tangdandan
 * @Date: 2020/4/9 15:10
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CompareToolServiceImpl implements CompareToolService {
    private StringRedisTemplate redisTemplate;

    /**
     * 比较数据
     * @param fileOne
     * @param fileTwo
     * @return
     */
    @Override
    public List compareData(MultipartFile fileOne, MultipartFile fileTwo) {
        List<FileData> data = new ArrayList<>();
        try {
            String firstLabel = "file1";
            InputStream inputStreamOne = fileOne.getInputStream();
            String secondLabel = "file2";
            InputStream inputStreamTwo = fileTwo.getInputStream();

            /**
             * 文件2跟文件1比较
             */
            FileImportDataListener dataListener = new FileImportDataListener(firstLabel);
            EasyExcel.read(inputStreamOne, FileData.class,dataListener).sheet().doRead();
            FileCompareListener compareListener = new FileCompareListener(firstLabel,data);
            EasyExcel.read(inputStreamTwo, FileData.class,compareListener).sheet().doRead();

            /**
             * 文件1跟文件2比较
             */

            InputStream inputStream1 = fileOne.getInputStream();
            InputStream inputStream2 = fileTwo.getInputStream();
            FileImportDataListener dataListener2 = new FileImportDataListener(secondLabel);
            EasyExcel.read(inputStream2, FileData.class,dataListener2).sheet().doRead();
            FileCompareListener compareListener2 = new FileCompareListener(secondLabel,data);
            EasyExcel.read(inputStream1, FileData.class,compareListener2).sheet().doRead();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
