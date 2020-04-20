package com.vcredit.tools;

import com.alibaba.excel.EasyExcel;
import com.vcredit.tools.excel.data.FileData;
import com.vcredit.tools.excel.listener.FileImportDataListener;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;

/**
 * @Author: tangdandan
 * @Date: 2020/4/17 11:12
 */
@Slf4j
public class Tool {
    public static void main(String[] args) {
        //获取jedis
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        //使用通道，加速处理数据
        Pipeline pipeline = jedis.pipelined();

        ArrayList<String> data = new ArrayList<>();
        ArrayList<String> repeat = new ArrayList<>();
        try {
            String firstLabel = "file1";
            File fileOne= new File("D://workspace/mytool/src/main/java/com/vcredit/tools/file/test1.xlsx");
            InputStream inputStreamOne = new FileInputStream(fileOne);
            String secondLabel = "file2";
            File fileTwo= new File("D://workspace/mytool/src/main/java/com/vcredit/tools/file/test2.xlsx");
            InputStream inputStreamTwo = new FileInputStream(fileTwo);

            FileImportDataListener dataListener = new FileImportDataListener(firstLabel,repeat);
            EasyExcel.read(inputStreamOne, FileData.class,dataListener).sheet().doRead();
            log.info(fileOne.getName()+"总共解析存储了"+dataListener.getTotal()+"条数据入库~~~~~");
            FileImportDataListener dataListener2 = new FileImportDataListener(secondLabel,repeat);
            EasyExcel.read(inputStreamTwo, FileData.class,dataListener2).sheet().doRead();
            log.info(fileTwo.getName()+"总共解析存储了"+dataListener2.getTotal()+"条数据入库~~~~~");

            Response<Set<String>> sDiff1 = pipeline.sdiff(firstLabel, secondLabel);
            Response<Set<String>> sDiff2 = pipeline.sdiff(secondLabel, firstLabel);

            pipeline.del(firstLabel);
            pipeline.del(secondLabel);
            pipeline.clear();
            pipeline.close();
            sDiff1.get().stream().forEach(s->{
                data.add(s);
            });
            sDiff2.get().stream().forEach(d->{
                data.add(d);
            });
            log.info("两个文件包含的不同数据有："+data.toString());
            if (repeat.isEmpty()) {
                log.info("不包含重复数据");
            } else {
                log.info("包含的重复数据有：" + repeat.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
