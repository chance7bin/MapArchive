package com.opengms.maparchivebackendprj.dao.impl;

import com.mongodb.client.result.DeleteResult;
import com.opengms.maparchivebackendprj.dao.IUserDao;
import com.opengms.maparchivebackendprj.entity.enums.UserRoleEnum;
import com.opengms.maparchivebackendprj.entity.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Repository
public class UserDaoImpl implements IUserDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public User findByName(String name){

        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(name));

        return mongoTemplate.findOne(query,User.class);

    }

    @Override
    public User findById(String userId) {
        return mongoTemplate.findById(userId,User.class);
    }

    @Override
    public User insert(User user) {
        return mongoTemplate.insert(user);
    }

    @Override
    public List<User> findAll(Pageable pageable) {
        Query query = new Query();
        return mongoTemplate.find(query.with(pageable),User.class);
    }

    @Override
    public List<User> findAllByUserRole(UserRoleEnum userRole, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userRole").is(userRole));
        return mongoTemplate.find(query.with(pageable),User.class);
    }

    @Override
    public User save(User user) {
        return mongoTemplate.save(user);
    }

    @Override
    public DeleteResult delete(User user) {
        return mongoTemplate.remove(user);
    }

    @Override
    public long countAll() {
        return mongoTemplate.estimatedCount(User.class);
    }

    @Override
    public long countAllByUserRole(UserRoleEnum userRole) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userRole").is(userRole));
        return mongoTemplate.count(query,User.class);
    }


}
