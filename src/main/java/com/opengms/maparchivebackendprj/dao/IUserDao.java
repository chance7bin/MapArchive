package com.opengms.maparchivebackendprj.dao;

import com.mongodb.client.result.DeleteResult;
import com.opengms.maparchivebackendprj.entity.enums.UserRoleEnum;
import com.opengms.maparchivebackendprj.entity.po.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IUserDao {

    User findByName(String name);

    User findById(String userId);

    User insert(User user);

    List<User> findAll(Pageable pageable);

    List<User> findAllByUserRole(UserRoleEnum userRole, Pageable pageable);

    User save(User user);

    DeleteResult delete(User user);

    long countAll();

    long countAllByUserRole(UserRoleEnum userRole);
}
