package com.toolrecommend.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.toolrecommend.common.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper接口
 *
 * @author Tool Recommend Team
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
