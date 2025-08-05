package com.nhom5.healthtracking.data.repository;

import com.nhom5.healthtracking.data.local.dao.UserDao;
import com.nhom5.healthtracking.data.local.entity.User;
import java.util.List;

public class UserRepository {
    private UserDao userDao;

    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
    }
    
    // Get all users
    public List<User> getAll() {
        return userDao.getAllUsers();
    }
    
    // Insert new user
    public void insert(User user) {
        userDao.insertUser(user);
    }
    
    // Update existing user
    public void update(User user) {
        userDao.updateUser(user);
    }
    
    // Delete user
    public void delete(User user) {
        userDao.deleteUser(user);
    }
    
    // Get user by ID
    public User getById(int id) {
        return userDao.getUserById(id);
    }
    
    // Get user by email
    public User getByEmail(String email) {
        return userDao.getUserByEmail(email);
    }
    
    // Check if email already exists
    public boolean checkEmailExists(String email) {
        User user = userDao.getUserByEmail(email);
        return user != null;
    }
    
    // Authenticate user with email and password
    public User authenticate(String email, String password) {
        User user = userDao.getUserByEmail(email);
        if (user != null && user.password.equals(password)) {
            return user;
        }
        return null;
    }
    
    // Get user count
    public int getCount() {
        return userDao.getAllUsers().size();
    }
}
