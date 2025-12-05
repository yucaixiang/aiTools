package com.toolrecommend.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.toolrecommend.common.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对话Mapper接口
 *
 * @author Tool Recommend Team
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 根据会话ID查询对话历史
     */
    List<Conversation> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 查询用户最近的会话
     */
    List<Conversation> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 删除会话历史
     */
    int deleteBySessionId(@Param("sessionId") String sessionId);
}
