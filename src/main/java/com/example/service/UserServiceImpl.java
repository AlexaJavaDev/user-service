package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;
import com.example.exception.UserAlreadyExistsException;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User create(User user) {
        if (userDao.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email " + user.getEmail() + " уже занят");
        }
        return userDao.create(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public User update(User user) {
        Optional<User> existing = userDao.findByEmail(user.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
            throw new UserAlreadyExistsException("Email " + user.getEmail() + " занят другим пользователем");
        }
        return userDao.update(user);
    }

    @Override
    public void delete(Long id) {
        userDao.delete(id);
    }
}
