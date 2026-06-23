package io.nebula.storage.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommonMapper<T> extends BaseMapper<T> {

    int insertIgnoreBatch(@Param("list") List<T> list);

    int replaceBatch(@Param("list") List<T> list);

    int insertOnDuplicateBatch(@Param("list") List<T> list);
}
