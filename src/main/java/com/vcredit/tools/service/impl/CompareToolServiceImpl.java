package com.vcredit.tools.service.impl;

import com.alibaba.excel.EasyExcel;
import com.vcredit.tools.excel.data.FileData;
import com.vcredit.tools.excel.data.RespDto;
import com.vcredit.tools.excel.listener.FileImportDataListener;
import com.vcredit.tools.service.CompareToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @Author: tangdandan
 * @Date: 2020/4/9 15:10
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CompareToolServiceImpl implements CompareToolService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 比较数据
     * @param fileOne
     * @param fileTwo
     * @return
     */
    @Override
    public Optional<RespDto> compareData(MultipartFile fileOne, MultipartFile fileTwo) {
        RespDto dto = new RespDto();
        ArrayList<String> data = new ArrayList<>();
        ArrayList<String> repeat = new ArrayList<>();
        try {
            String firstLabel = "file1";
            InputStream inputStreamOne = fileOne.getInputStream();
            String secondLabel = "file2";
            InputStream inputStreamTwo = fileTwo.getInputStream();

            FileImportDataListener dataListener = new FileImportDataListener(firstLabel,repeat);
            EasyExcel.read(inputStreamOne, FileData.class,dataListener).sheet().doRead();
            log.info(fileOne.getName()+"总共解析存储了"+dataListener.getTotal()+"条数据入库~~~~~");
            FileImportDataListener dataListener2 = new FileImportDataListener(secondLabel,repeat);
            EasyExcel.read(inputStreamTwo, FileData.class,dataListener2).sheet().doRead();
            log.info(fileTwo.getName()+"总共解析存储了"+dataListener2.getTotal()+"条数据入库~~~~~");

            Set<String> res1 = redisTemplate.opsForSet().difference(firstLabel, secondLabel);
            Set<String> res2 = redisTemplate.opsForSet().difference(secondLabel, firstLabel);
            redisTemplate.delete(firstLabel);
            redisTemplate.delete(secondLabel);
             res1.stream().forEach(s->{
                 data.add(s);
             });
             res2.stream().forEach(d->{
                 data.add(d);
             });
             dto.setDiffData(data);
             dto.setRepeatData(repeat);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(dto);
    }

}
