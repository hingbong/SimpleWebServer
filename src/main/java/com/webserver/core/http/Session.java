package com.webserver.core.http;

import java.nio.channels.SocketChannel;

/**
 * @author Hingbong
 */
public class Session implements Runnable {

  private SocketChannel socketChannel;

  public Session() {
  }

  public Session(SocketChannel socketChannel) {
    this.socketChannel = socketChannel;
  }

  @Override
  public void run() {
    Request request = new Request(socketChannel);
    request.start();
    Response response = new Response(socketChannel, request);
    response.sendData();
  }
}
