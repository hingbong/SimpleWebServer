package com.webserver.core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class User implements Serializable {

  private static final long serialVersionUID = 7645096949777878404L;
  private String username;
  private String password;

  public User(String username, String password) {
    this.username = username;
    this.password = password;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User)) {
      return false;
    }
    User user = (User) o;
    return username.equals(user.username) &&
        password.equals(user.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password);
  }

  @Override
  public String toString() {
    return "User{" +
        "username='" + username + '\'' +
        ", password='" + password + '\'' +
        '}';
  }
}
