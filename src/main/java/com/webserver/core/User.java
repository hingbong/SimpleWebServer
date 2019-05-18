package com.webserver.core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Hingbong
 */
public class User {

    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static User postUser(String message) {
        // 传送用户
        String username = "username=";
        String password = "password=";
        if (message.contains(username) && message.contains(password)) {
            message = URLDecoder.decode(message, StandardCharsets.UTF_8);
            String name =
                message.substring(message.indexOf("username=") + 9, message.indexOf("&password="));
            String passwd;
            String confirmPassword = "&confirm_password=";
            String newPassword = "&new_password=";
            if (message.contains(confirmPassword)) {
                passwd =
                    message.substring(
                        message.indexOf("&password=") + 10, message.indexOf("&confirm_password="));
            } else if (message.contains(newPassword)) {
                passwd =
                    message.substring(
                        message.indexOf("&password=") + 10, message.indexOf("&new_password="));

            } else {
                passwd = message.substring(message.indexOf("&password=") + 10);
            }
            return new User(name, passwd);
        }
        return null;
    }

    public static boolean newUser(User user) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("user.dat", "rw");
        byte[] data = new byte[32];
        while (raf.read(data) != -1) {
            if (new String(data, StandardCharsets.UTF_8).trim().equals(user.username)) {
                return false;
            }
            raf.skipBytes(32);
        }
        raf.seek(raf.length());
        byte[] usernameData = user.username.getBytes(StandardCharsets.UTF_8);
        usernameData = Arrays.copyOf(usernameData, 32);
        raf.write(usernameData);
        byte[] passwordData = user.password.getBytes(StandardCharsets.UTF_8);
        passwordData = Arrays.copyOf(passwordData, 32);
        raf.write(passwordData);
        return true;
    }

    public static boolean verifyUser(User user) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("user.dat", "r");
        byte[] data = new byte[32];
        while (raf.read(data) != -1) {
            if (new String(data, StandardCharsets.UTF_8).trim().equals(user.username)) {
                raf.read(data);
                if (new String(data, StandardCharsets.UTF_8).trim().equals(user.password)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean modifyPasswd(User user, String newPasswd) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("user.dat", "rw");
        byte[] data = new byte[32];
        while (raf.read(data) != -1) {
            if (new String(data, StandardCharsets.UTF_8).trim().equals(user.username)) {
                raf.read(data);
                if (new String(data, StandardCharsets.UTF_8).trim().equals(user.password)) {
                    byte[] newPasswdData = newPasswd.trim().getBytes(StandardCharsets.UTF_8);
                    newPasswdData = Arrays.copyOf(newPasswdData, 32);
                    raf.seek(raf.getFilePointer() - 32);
                    raf.write(newPasswdData);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "User{" + "username='" + username + '\'' + ", password='" + password + '\'' + '}';
    }
}
