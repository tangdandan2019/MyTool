package com.vcredit.tools.controller;


import com.vcredit.tools.excel.data.RespDto;
import com.vcredit.tools.service.CompareToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.result.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * @Author: tangdandan
 * @Date: 2020/4/9 14:06
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CompareToolController {
    private final CompareToolService compareToolService;

    /**
     * 数据比较
     * @param fileOne
     * @param fileTwo
     * @return
     */
    @PostMapping("/compare/tool")
    public R<Object> compareData(@RequestParam("file1") MultipartFile fileOne, @RequestParam("file2") MultipartFile fileTwo){
        Optional<RespDto> respDto = compareToolService.compareData(fileOne, fileTwo);
        return R.success(respDto.get());
    }
}
