package com.vcredit.tools.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.vcredit.tools.excel.data.FileData;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.config.SpringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: tangdandan
 * @Date: 2020/4/9 15:53
 */
@Slf4j
public class FileImportDataListener extends AnalysisEventListener<FileData> {

   private Jedis jedis;
   private String randomLabel;
   private ArrayList<String> repeatValues;
   private  Pipeline pipeline;
    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private int number = 0;
    private static final int BATCH_COUNT = 1000;
    List<FileData> list = new ArrayList<FileData>();


    public FileImportDataListener(String label, ArrayList<String> data) {
        jedis = new Jedis("127.0.0.1", 6379);
        //使用通道，加速处理数据
        pipeline= jedis.pipelined();
        this.randomLabel=label;
        this.repeatValues =data;
    }

    /**
     * 这个每一条数据解析都会来调用
     */
   @Override
    public void invoke( FileData data, AnalysisContext context) {
        number++;
        list.add(data);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            list.clear();
        }
    }
    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        if(!list.isEmpty()) {
            saveData();
        }
        log.info("所有数据解析完成！");
    }
    /**
     * 加上存储数据库
     */
    private void saveData() {
        log.info("{}条数据，开始存储redis！", list.size());

        //list转换为String类型
        List<String> transfer = this.list.stream().map(da -> da.getData()).collect(Collectors.toList());
        //判断是否有重复元素
        findRepeatValues(transfer,repeatValues);
        String[] strings = new String[transfer.size()];
        //转换成数组
        transfer.toArray(strings);
        pipeline.sadd(randomLabel,strings);
        pipeline.sync();
        log.info("存储进入redis成功！");
    }

    public void findRepeatValues(List<String> list, ArrayList res) {
        ArrayList repeat = new ArrayList<String>();
        list.stream().forEach(d->{
            if(repeat.contains(d)){
                res.add(d);
            }else {
                repeat.add(d);
            }
        });
        repeat.clear();
    }

    /**
     * 获取数据总数量
     * @return
     */
    public int getTotal() {
        return number;
    }
}
