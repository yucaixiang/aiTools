package com.toolrecommend.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.toolrecommend.common.entity.UserAction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户行为Mapper接口
 *
 * @author Tool Recommend Team
 */
@Mapper
public interface UserActionMapper extends BaseMapper<UserAction> {
}
