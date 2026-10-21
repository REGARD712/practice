package com.coding24h.imessage.mapper;

import com.coding24h.imessage.entity.Message;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MessageMapper {
    @Select("SELECT * FROM message WHERE id = #{id}")
    Message selectById(Long id);

    @Select("SELECT * FROM message ORDER BY create_time DESC")
    @Results(id = "messageWithUserAndCategory", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "category_id", property = "categoryId"),
            @Result(column = "user_id", property = "user",
                    one = @One(select = "com.coding24h.imessage.mapper.UserMapper.selectById")),
            @Result(column = "category_id", property = "category",
                    one = @One(select = "com.coding24h.imessage.mapper.CategoryMapper.selectById"))
    })
    List<Message> selectAllWithUserAndCategory();

    @Insert("INSERT INTO message(title, content, user_id, category_id) VALUES(#{title}, #{content}, #{userId}, #{categoryId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    @Update("UPDATE message SET title=#{title}, content=#{content}, user_id=#{userId}, category_id=#{categoryId} WHERE id=#{id}")
    int update(Message message);

    @Delete("DELETE FROM message WHERE id = #{id}")
    int deleteById(Long id);
}
