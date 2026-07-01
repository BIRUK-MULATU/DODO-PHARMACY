//package com.rxpharma.service;
//
//import com.rxpharma.entity.User;
//import com.rxpharma.exception.BadRequestException;
//import com.rxpharma.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @InjectMocks
//    private UserService userService;
//
//    private User user;
//
//    @BeforeEach
//    void setUp() {
//        user = new User();
//        user.setId(1L);
//        user.setFullName("Test User");
//        user.setEmail("test@mail.com");
//        user.setPassword("encoded-pass");
//        user.setRole(User.Role.USER);
//        user.setApproved(false);
//    }
//
//    @Test
//    void getAllUsers_ShouldReturnList() {
//        when(userRepository.findAll()).thenReturn(List.of(user));
//
//        List<User> result = userService.getAllUsers();
//
//        assertEquals(1, result.size());
//        verify(userRepository).findAll();
//    }
//
//    @Test
//    void getUserById_ShouldReturnUser() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//
//        User result = userService.getUserById(1L);
//
//        assertEquals("Test User", result.getFullName());
//    }
//
//    @Test
//    void getUserById_ShouldThrowException_WhenNotFound() {
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//
//        RuntimeException ex = assertThrows(RuntimeException.class,
//                () -> userService.getUserById(1L));
//
//        assertTrue(ex.getMessage().contains("User not found"));
//    }
//
//    @Test
//    void getUserByEmail_ShouldReturnUser() {
//        when(userRepository.findByEmail("test@mail.com"))
//                .thenReturn(Optional.of(user));
//
//        User result = userService.getUserByEmail("test@mail.com");
//
//        assertNotNull(result);
//    }
//
//    @Test
//    void updateUser_ShouldUpdateAndSave() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        User result = userService.updateUser(
//                1L, "New Name", "new@mail.com", User.Role.ADMIN);
//
//        assertEquals("New Name", result.getFullName());
//        assertEquals(User.Role.ADMIN, result.getRole());
//
//        verify(userRepository).save(any(User.class));
//    }
//
//    @Test
//    void deleteUser_ShouldDelete_WhenExists() {
//        when(userRepository.existsById(1L)).thenReturn(true);
//
//        userService.deleteUser(1L);
//
//        verify(userRepository).deleteById(1L);
//    }
//
//    @Test
//    void deleteUser_ShouldThrow_WhenNotFound() {
//        when(userRepository.existsById(1L)).thenReturn(false);
//
//        RuntimeException ex = assertThrows(RuntimeException.class,
//                () -> userService.deleteUser(1L));
//
//        assertTrue(ex.getMessage().contains("User not found"));
//    }
//
//    @Test
//    void changePassword_ShouldUpdatePassword() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches("old", "encoded-pass")).thenReturn(true);
//        when(passwordEncoder.matches("new", "encoded-pass")).thenReturn(false);
//        when(passwordEncoder.encode("new")).thenReturn("new-encoded");
//
//        userService.changePassword(1L, "old", "new", "new");
//
//        verify(userRepository).save(any(User.class));
//    }
//
//    @Test
//    void changePassword_ShouldFail_WhenCurrentPasswordWrong() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches("wrong", "encoded-pass")).thenReturn(false);
//
//        assertThrows(BadRequestException.class,
//                () -> userService.changePassword(1L, "wrong", "new", "new"));
//    }
//
//    @Test
//    void changePassword_ShouldFail_WhenPasswordsDoNotMatch() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches("old", "encoded-pass")).thenReturn(true);
//
//        assertThrows(BadRequestException.class,
//                () -> userService.changePassword(1L, "old", "new", "different"));
//    }
//
//    @Test
//    void adminResetPassword_ShouldSave() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
//
//        userService.adminResetPassword(1L, "newpass");
//
//        verify(userRepository).save(any(User.class));
//    }
//
//    @Test
//    void updateRole_ShouldUpdateRole() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        User result = userService.updateRole(1L, User.Role.ADMIN);
//
//        assertEquals(User.Role.ADMIN, result.getRole());
//    }
//
//    @Test
//    void approveUser_ShouldSetApprovedTrue() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        User result = userService.approveUser(1L);
//
//        assertTrue(result.isApproved());
//    }
//
//    @Test
//    void denyUser_ShouldDelete_WhenNotApproved() {
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//
//        userService.denyUser(1L);
//
//        verify(userRepository).delete(user);
//    }
//
//    @Test
//    void denyUser_ShouldThrow_WhenAlreadyApproved() {
//        user.setApproved(true);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//
//        assertThrows(BadRequestException.class,
//                () -> userService.denyUser(1L));
//    }
//
//    @Test
//    void getPendingUsers_ShouldReturnList() {
//        when(userRepository.findByApprovedFalse()).thenReturn(List.of(user));
//
//        List<User> result = userService.getPendingUsers();
//
//        assertEquals(1, result.size());
//    }
//}