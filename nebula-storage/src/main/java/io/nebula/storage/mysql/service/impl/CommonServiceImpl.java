package io.nebula.storage.mysql.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.nebula.storage.mysql.mapper.CommonMapper;
import io.nebula.storage.mysql.service.CommonService;

import java.util.List;

public abstract class CommonServiceImpl<M extends CommonMapper<T>, T>
    extends ServiceImpl<M, T> implements CommonService<T> {

    private static final int BATCH_SIZE = 1000;

    @Override
    public Boolean insertIgnoreBatch(List<T> list) {
        return executeBatch(list, baseMapper::insertIgnoreBatch);
    }

    @Override
    public Boolean replaceBatch(List<T> list) {
        return executeBatch(list, baseMapper::replaceBatch);
    }

    @Override
    public Boolean insertOnDuplicateBatch(List<T> list) {
        return executeBatch(list, baseMapper::insertOnDuplicateBatch);
    }

    private Boolean executeBatch(List<T> list, BatchExecutor<T> executor) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }

        int batchSize = Math.min(BATCH_SIZE, list.size());
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            List<T> batch = list.subList(i, end);
            executor.execute(batch);
        }
        return true;
    }

    @FunctionalInterface
    private interface BatchExecutor<T> {
        void execute(List<T> batch);
    }
}
