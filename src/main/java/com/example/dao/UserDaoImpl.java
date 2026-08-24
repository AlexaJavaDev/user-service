package com.example.dao;

import com.example.entity.User;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User create(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            logger.info("Создан пользователь: {}", user);
            return user;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            logger.error("Ошибка при создании пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        } catch (RuntimeException e) {
            logger.error("Ошибка при поиске пользователя по id {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        } catch (RuntimeException e) {
            logger.error("Ошибка при поиске пользователя по email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();
        } catch (RuntimeException e) {
            logger.error("Ошибка при получении всех пользователей: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public User update(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
            logger.info("Обновлен пользователь: {}", user);
            return user;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            logger.error("Ошибка при обновлении пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.info("Удален пользователь с id {}", id);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            logger.error("Ошибка при удалении пользователя с id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}