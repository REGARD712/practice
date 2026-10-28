package com.coding24h.itnews.mapper;

import java.util.List;

import com.coding24h.itnews.entity.Article;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper {
    @Select("SELECT * FROM article ORDER BY create_time DESC")
    @Results(id = "articleWithUserAndCategory", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "category_id", property = "categoryId"),
            @Result(column = "user_id", property = "user", one = @One(select = "com.coding24h.itnews.mapper.UserMapper.selectById")),
            @Result(column = "category_id", property = "category", one = @One(select = "com.coding24h.itnews.mapper.CategoryMapper.selectById"))
    })
    List<Article> selectAllWithUserAndCategory();

    @Select("SELECT * FROM article WHERE id = #{id}")
    @ResultMap("articleWithUserAndCategory")
    Article selectById(Long id);

    @Select("SELECT * FROM article WHERE category_id = #{categoryId} ORDER BY create_time DESC")
    @ResultMap("articleWithUserAndCategory")
    List<Article> selectByCategoryId(Long categoryId);

    @Insert("INSERT INTO article(title, content, user_id, category_id) VALUES(#{title}, #{content}, #{userId}, #{categoryId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Article article);

    @Update("UPDATE article SET title=#{title}, content=#{content}, category_id=#{categoryId} WHERE id=#{id}")
    int update(Article article);

    @Delete("DELETE FROM article WHERE id = #{id}")
    int deleteById(Long id);
}