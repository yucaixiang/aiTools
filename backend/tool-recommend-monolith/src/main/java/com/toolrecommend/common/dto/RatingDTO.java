package com.toolrecommend.common.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 评分DTO
 */
@Data
public class RatingDTO {

    /**
     * 工具ID
     */
    @NotNull(message = "工具ID不能为空")
    private Long toolId;

    /**
     * 评分(1-5)
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer score;
}
