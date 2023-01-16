package com.udream.es.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QaEsDataDTO {

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

   @ApiModelProperty("阅读量")
   private Integer viewCount;

   @JsonDeserialize(using = LocalDateTimeDeserializer.class)
   @JsonSerialize(using = LocalDateTimeSerializer.class)
   @ApiModelProperty("更新时间")
   private LocalDateTime time;
}
