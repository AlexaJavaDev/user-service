package com.example;

import com.example.exception.UserAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserServiceImplem implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImplem.class);
    private final UserDao userDao;

    public UserServiceImplem(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User create(User user) {
        // Проверка уникальности email
        if (userDao.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь с email " + user.getEmail() + " уже существует");
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
        // При обновлении проверяем, что новый email не занят другим пользователем
        Optional<User> existing = userDao.findByEmail(user.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
            throw new UserAlreadyExistsException("Email " + user.getEmail() + " уже используется другим пользователем");
        }
        return userDao.update(user);
    }

    @Override
    public void delete(Long id) {
        userDao.delete(id);
    }
}
