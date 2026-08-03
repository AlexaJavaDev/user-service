package com.example;

import com.example.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImplem userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Test", "test@example.com", 25);
        testUser.setId(1L);
    }

    @Test
    void createUser_shouldSaveUser_whenEmailIsUnique() {
        when(userDao.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(userDao.create(testUser)).thenReturn(testUser);

        User created = userService.create(testUser);

        assertNotNull(created);
        assertEquals(testUser, created);
        verify(userDao, times(1)).findByEmail(testUser.getEmail());
        verify(userDao, times(1)).create(testUser);
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        when(userDao.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.create(testUser));

        verify(userDao, never()).create(testUser);
    }

    @Test
    void updateUser_shouldUpdate_whenEmailIsUniqueOrSameUser() {
        User updatedUser = new User("Updated", "test@example.com", 30);
        updatedUser.setId(1L);

        // Если email не изменился – проверяем, что findByEmail вернул того же пользователя
        when(userDao.findByEmail(updatedUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userDao.update(updatedUser)).thenReturn(updatedUser);

        User result = userService.update(updatedUser);

        assertEquals(updatedUser, result);
        verify(userDao, times(1)).findByEmail(updatedUser.getEmail());
        verify(userDao, times(1)).update(updatedUser);
    }

    @Test
    void updateUser_shouldThrowException_whenEmailBelongsToAnotherUser() {
        User anotherUser = new User("Another", "other@example.com", 40);
        anotherUser.setId(2L);

        User updatedUser = new User("Test", "other@example.com", 26);
        updatedUser.setId(1L);

        when(userDao.findByEmail(updatedUser.getEmail())).thenReturn(Optional.of(anotherUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.update(updatedUser));

        verify(userDao, never()).update(updatedUser);
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> found = userService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals(testUser, found.get());
    }

    @Test
    void delete_shouldCallDaoDelete() {
        userService.delete(1L);
        verify(userDao, times(1)).delete(1L);
    }
}
