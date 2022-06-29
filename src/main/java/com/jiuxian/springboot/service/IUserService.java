package com.jiuxian.springboot.service;

import com.jiuxian.springboot.controller.dto.UserDTO;
import com.jiuxian.springboot.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jiuxian
 * @since 2022-05-16
 */
public interface IUserService extends IService<User> {

    UserDTO login(UserDTO userDTO);

    User register(UserDTO userDTO);

}
