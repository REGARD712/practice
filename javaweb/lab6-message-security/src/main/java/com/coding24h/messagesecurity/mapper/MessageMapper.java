package com.coding24h.messagesecurity.mapper;

import com.coding24h.messagesecurity.entity.Message;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MessageMapper {
    @Select("SELECT m.*, u.username FROM message m JOIN `user` u ON m.user_id = u.id ORDER BY m.create_time DESC")
    @Results(id = "messageResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "content", column = "content"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "username", column = "username")
    })
    List<Message> findAllMessages();

    @Select("SELECT m.*, u.username FROM message m JOIN `user` u ON m.user_id = u.id WHERE m.user_id = #{userId} ORDER BY m.create_time DESC")
    List<Message> findMessagesByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO message(content, user_id) VALUES(#{content}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(Message message);

    @Delete("DELETE FROM message WHERE id = #{id}")
    int deleteMessageById(Long id);

    @Select("SELECT user_id FROM message WHERE id = #{id}")
    Long findUserIdByMessageId(@Param("id") Long id);
}
