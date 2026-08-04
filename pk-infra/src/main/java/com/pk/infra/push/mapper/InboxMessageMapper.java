package com.pk.infra.push.mapper;

import com.pk.infra.push.repository.InboxMessageRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InboxMessageMapper {
    long countUnread(@Param("userId") long userId);

    List<InboxMessageRow> findPage(
            @Param("userId") long userId,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );

    InboxMessageRow findByUserIdAndMessageId(
            @Param("userId") long userId,
            @Param("messageId") long messageId
    );

    int markRead(@Param("userId") long userId, @Param("messageId") long messageId);
}
