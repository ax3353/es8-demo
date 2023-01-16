package com.udream.es.controller;

import com.udream.es.query.QsDataQuery;
import com.udream.es.serivce.QaDataServiceImpl;
import com.udream.es.vo.QaDataVO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
// @RequiredArgsConstructor只会对当前类里面加了final或者@NonNull的属性进行注入
@RequiredArgsConstructor
@RequestMapping("/es/dataCollect")
public class QaDataController {

    @NonNull
    private QaDataServiceImpl qaDataService;

    /**
     * 描述: 保存数据到es
     *
     * @author kun.zhu
     * @date 2022/11/18 15:35
     */
    @PostMapping("/saveEsData")
    public boolean saveEsData(@RequestBody QsDataQuery qsDataQuery) {
        return qaDataService.saveEsData(qsDataQuery);
    }

    @GetMapping("/search")
    public List<QaDataVO> search(@RequestParam String keyword) {
        return qaDataService.highLightSearchFromEs(keyword).stream().map(e -> {
            QaDataVO vo = new QaDataVO();
            BeanUtils.copyProperties(e, vo, "keyword, summary");
            return vo;
        }).collect(Collectors.toList());
    }
}
