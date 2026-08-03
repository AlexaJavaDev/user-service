package com.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserDaoImplem implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImplem.class);
    private final SessionFactory sessionFactory;

    public UserDaoImplem() {
        try {
            // Создаём сеансы из hibernate.cfg.xml
            this.sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Exception e) {
            logger.error("Не удалось инициализировать SessionFactory: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка инициализации Hibernate", e);
        }
    }

    @Override
    public User create(User user) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.save(user);
            tx.commit();
            logger.info("Создан пользователь: {}", user);
            return user;
        } catch (PersistenceException e) {
            if (tx != null) tx.rollback();
            logger.error("Ошибка при создании пользователя {}: {}", user, e.getMessage(), e);
            throw e; // дальше для обработки в вызывающем коде
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, id);
            logger.debug("Поиск пользователя по id {}: {}", id, user);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по id {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = builder.createQuery(User.class);
            query.from(User.class);
            List<User> list = session.createQuery(query).getResultList();
            logger.debug("Найдено {} пользователей", list.size());
            return list;
        } catch (Exception e) {
            logger.error("Ошибка при получении всех пользователей: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public User update(User user) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.update(user);
            tx.commit();
            logger.info("Обновлён пользователь: {}", user);
            return user;
        } catch (PersistenceException e) {
            if (tx != null) tx.rollback();
            logger.error("Ошибка при обновлении пользователя {}: {}", user, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
                logger.info("Удалён пользователь с id {}", id);
            } else {
                logger.warn("Попытка удалить несуществующего пользователя с id {}", id);
            }
            tx.commit();
        } catch (PersistenceException e) {
            if (tx != null) tx.rollback();
            logger.error("Ошибка при удалении пользователя с id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Закрытие сеансов при завершении приложения
    public void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
            logger.info("SessionFactory закрыта");
        }
    }
}
