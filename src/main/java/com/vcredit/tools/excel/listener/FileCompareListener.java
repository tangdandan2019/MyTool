package com.vcredit.tools.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.vcredit.tools.excel.data.FileData;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.config.SpringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: tangdandan
 * @Date: 2020/4/9 15:53
 */
@Slf4j
public class FileCompareListener extends AnalysisEventListener<FileData> {

   private StringRedisTemplate redisTemplate;
   private String randomLabel;
   List<FileData> res = null;
    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private int number = 0;
    private static final int BATCH_COUNT = 1000;
    List<FileData> list = new ArrayList<FileData>();

    public FileCompareListener(String label,List<FileData> res) {
        redisTemplate = SpringUtils.getBean(StringRedisTemplate.class);
        this.randomLabel = label;
        this.res = res;
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
            compareData();
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
        compareData();
        log.info("所有数据比较完成！");
        redisTemplate.delete(randomLabel);
    }
    /**
     * 加上存储数据库
     */
    private void compareData() {
        log.info("{}条数据，开始比较数据！", list.size());
        list.stream().forEach(data->{
            Boolean hasKey = redisTemplate.opsForHash().hasKey(randomLabel,data.getData());
            if(!hasKey){
                res.add(data);
            }
        });
    }

    /**
     * 获取数据总数量
     * @return
     */
    public int getTotal() {
        return number;
    }

}
