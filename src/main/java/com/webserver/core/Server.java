package com.webserver.core;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Server {

  static final File WEB_ROOT = new File("web_root");
  private static final Server SERVER = new Server();
  private ServerSocket serverSocket;

  private Server() {
    try {
      this.serverSocket = new ServerSocket(8888);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    SERVER.start();
  }

  private void start() {
    // 启动服务器
    try {
      //noinspection InfiniteLoopStatement
      while (true) {
        Socket socket = serverSocket.accept();
        Thread thread = new Thread(() -> {
//          System.out.println("connected");
          try {
            synchronized (socket) {
              Request request = new Request(socket.getInputStream());
              Response response = new Response(socket.getOutputStream(), request);
              response.sendData();
            }
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            try {
              socket.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
        thread.start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
