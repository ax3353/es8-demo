package com.udream.es.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QaData {

   @ApiModelProperty("id")
   private Long id;

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

   @ApiModelProperty("创建时间")
   private LocalDateTime createTime;

   @ApiModelProperty("更新时间")
   private LocalDateTime updateTime;
}
