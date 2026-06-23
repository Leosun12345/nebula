package io.nebula.storage.mysql.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CommonService<T> extends IService<T> {
    Boolean insertIgnoreBatch(List<T> list);
    Boolean replaceBatch(List<T> list);
    Boolean insertOnDuplicateBatch(List<T> list);
}
