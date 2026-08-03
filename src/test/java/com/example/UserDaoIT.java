package com.example;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private UserDao userDao;

    @BeforeAll
    static void setUpContainer() {
        // Настройка Hibernate для использования контейнера
        Configuration configuration = new Configuration();
        System.setProperty("docker.host", "tcp://localhost:2375");
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.addAnnotatedClass(User.class);
        sessionFactory = configuration.buildSessionFactory();
    }

    @AfterAll
    static void tearDownContainer() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImplem(sessionFactory);
        // Очистка таблицы перед каждым тестом, для изоляции
        try (var session = sessionFactory.openSession()) {
            session.createNativeQuery("DELETE FROM users").executeUpdate();
        }
    }

    @Test
    void create_shouldSaveUser() {
        User user = new User("John", "john@example.com", 30);
        User saved = userDao.create(user);

        assertNotNull(saved.getId());
        assertEquals("john@example.com", saved.getEmail());
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        User user = new User("Jane", "jane@example.com", 25);
        User saved = userDao.create(user);

        Optional<User> found = userDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getEmail(), found.get().getEmail());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userDao.create(new User("User1", "u1@example.com", 20));
        userDao.create(new User("User2", "u2@example.com", 22));

        List<User> users = userDao.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void update_shouldModifyUser() {
        User user = new User("Old", "old@example.com", 40);
        User saved = userDao.create(user);

        saved.setName("New");
        saved.setAge(41);
        userDao.update(saved);

        Optional<User> updated = userDao.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("New", updated.get().getName());
        assertEquals(41, updated.get().getAge());
    }

    @Test
    void delete_shouldRemoveUser() {
        User user = new User("ToDelete", "delete@example.com", 18);
        User saved = userDao.create(user);

        userDao.delete(saved.getId());

        Optional<User> found = userDao.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        User user = new User("EmailTest", "email@example.com", 28);
        userDao.create(user);

        Optional<User> found = userDao.findByEmail("email@example.com");

        assertTrue(found.isPresent());
        assertEquals("EmailTest", found.get().getName());
    }
}
