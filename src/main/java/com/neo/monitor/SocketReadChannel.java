package com.neo.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

public class SocketReadChannel implements Callable<Boolean>{
	private SocketChannel socketChannel;

	public SocketReadChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public Boolean call() throws InterruptedException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(socketChannel.socket().getInputStream()));
			Thread.sleep(35);
			while (in.ready()) {
				@SuppressWarnings("unused")
				char c = (char) in.read();
				if (!in.ready()) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return true;
	}
}
