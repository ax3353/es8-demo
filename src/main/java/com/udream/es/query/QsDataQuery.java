package com.udream.es.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 描述: QA数据入参
 * @author kun.zhu
 * @date 2022/11/21 17:45
 */
@Data
public class QsDataQuery {

    @ApiModelProperty("关键词")
    private String keyword;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("来源类型")
    private Integer sourceType;

    @ApiModelProperty("来源(按类型区分，可以是表id，也可以是链接)")
    private String source;

    @ApiModelProperty("阅读量")
    private Integer viewCount;
}
