package com.visualspider.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper {

    @Select("""
            select id, source_url, title, author, published_at, summary, content, cover_image, created_at, updated_at
            from article
            where source_url = #{sourceUrl}
            """)
    @Results(id = "articleResult", value = {
            @Result(property = "sourceUrl", column = "source_url"),
            @Result(property = "publishedAt", column = "published_at"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Article findBySourceUrl(String sourceUrl);

    @Insert("""
            insert into article (source_url, title, author, published_at, summary, content, cover_image)
            values (#{sourceUrl}, #{title}, #{author}, #{publishedAt}, #{summary}, #{content}, #{coverImage})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Article article);

    @Update("""
            update article
            set title = #{title},
                author = #{author},
                published_at = #{publishedAt},
                summary = #{summary},
                content = #{content},
                cover_image = #{coverImage},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int update(Article article);
}

