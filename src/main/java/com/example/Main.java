package com.example;

import com.example.console.UserConsoleApp;
import com.example.dao.UserDao;
import com.example.dao.UserDaoImpl;
import com.example.service.UserService;
import com.example.service.UserServiceImpl;

public class Main {
    public static void main(String[] args) {
        UserDao userDao = new UserDaoImpl();
        UserService userService = new UserServiceImpl(userDao);

        UserConsoleApp app = new UserConsoleApp(userService);
        app.run();
    }
}
