package com.udream.es.serivce;

import co.elastic.clients.elasticsearch._types.Result;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.udream.es.dto.QaEsDataDTO;
import com.udream.es.mapper.QaDataMapper;
import com.udream.es.po.QaData;
import com.udream.es.query.QsDataQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
// @RequiredArgsConstructor只会对当前类里面加了final或者@NonNull的属性进行注入
@RequiredArgsConstructor
@Slf4j
public class QaDataServiceImpl {

    final private QaDataMapper qaDataMapper;
    final private QaEsManager qaEsManager;

    /**
     * 描述: 保存数据到es
     *
     * @author kun.zhu
     * @date 2022/11/18 15:35
     */
    public boolean saveEsData(QsDataQuery query) {
        String content = query.getContent();
        String title = query.getTitle();
        LocalDateTime now = LocalDateTime.now();

        // db存储参数
        QaData qaData = new QaData();
        qaData.setId(IdWorker.getId());
        qaData.setKeyword(query.getKeyword());
        qaData.setTitle(title);
        qaData.setContent(content);
        qaData.setSourceType(query.getSourceType());
        qaData.setSource(query.getSource());
        qaData.setViewCount(0);
        qaData.setCreateTime(now);
        qaData.setUpdateTime(now);

        // es存储参数
        QaEsDataDTO dto = new QaEsDataDTO();
        dto.setId(qaData.getId());
        dto.setKeyword(query.getKeyword());
        dto.setTitle(title);
        dto.setContent(content);
        dto.setSourceType(qaData.getSourceType());
        dto.setViewCount(0);
        dto.setTime(now);

        try {
            Result result = qaEsManager.addDoc(dto);
            // 先写入es
            if (!Result.Created.equals(result)) {
                return false;
            }

            // 再写入db
            int effect = qaDataMapper.insert(qaData);
            return effect > 0;
        } catch (IOException e) {
            log.error("保存数据到es失败", e);
            return false;
        }
    }

    /**
     * 描述: 从es搜索
     *
     * @author kun.zhu
     * @date 2022/11/21 17:29
     */
    public List<QaEsDataDTO> searchFromEs(String keyword) {
        try {
            return qaEsManager.searchMultiMatch(keyword);
        } catch (IOException e) {
            log.error("从es搜索数据失败", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * 描述: 从es搜索
     *
     * @author kun.zhu
     * @date 2022/11/21 19:29
     */
    public List<QaEsDataDTO> highLightSearchFromEs(String keyword) {
        try {
            return qaEsManager.highlightSearchMultiMatch(keyword);
        } catch (IOException e) {
            log.error("从es搜索数据失败", e);
        }
        return new ArrayList<>(0);
    }
}
