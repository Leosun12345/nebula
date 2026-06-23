package io.nebula.storage.mongodb.repository.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseMongoRepository<T, ID extends Serializable>
    extends MongoRepository<T, ID> {
    // 公共查询方法由子类实现
}
