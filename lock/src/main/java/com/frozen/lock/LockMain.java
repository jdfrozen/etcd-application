package com.frozen.lock;

import io.etcd.jetcd.Client;
import java.util.concurrent.TimeUnit;

/**
 * @author: Frozen
 * @create: 2019-11-27 10:36
 * @description:
 **/
public class LockMain {
	public static void main(String[] args) throws Exception {
		Client client = Client.builder().endpoints(
				"http://101.200.38.80:2379"
		).build();
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(new App(client, i));
			thread.start();
		}
	}

	public static class App implements Runnable {
		private Client client;
		private int i;

		public App(Client client, int i) {
			this.client = client;
			this.i = i;
		}

		@Override
		public void run() {
			try {
				EtcdLock lock = new EtcdLock(client);
				if (lock.tryLock("frozen", 10, 10000L)) {
					System.out.println(i + "获得锁");
					TimeUnit.SECONDS.sleep(1);
					lock.unlock();
				} else {
					System.out.println(i + "获得锁失败");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
