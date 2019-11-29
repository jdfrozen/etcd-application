package com.frozen.base;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;

import static com.google.common.base.Charsets.UTF_8;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;

/**
 * @author: Frozen
 * @create: 2019-11-29 13:56
 * @description:
 **/
public class Watch {

	private Client client;

	public Watch(Client client) {
		this.client = client;
	}

	public void watchEtcdKey(String key) throws Exception {
		// 最大事件数量
		Integer maxEvents = Integer.MAX_VALUE;
		CountDownLatch latch = new CountDownLatch(maxEvents);
		io.etcd.jetcd.Watch.Watcher watcher = null;
		try {
			ByteSequence watchKey = ByteSequence.from(key, UTF_8);
			WatchOption watchOpts = WatchOption.newBuilder().build();
			watcher = this.client.getWatchClient().watch(watchKey, watchOpts, response -> {
				for (WatchEvent event : response.getEvents()) {
					System.out.println("watch type= \"" + event.getEventType().toString() + "\",  key= \""
							+ Optional.ofNullable(event.getKeyValue().getKey()).map(bs -> bs.toString(UTF_8)).orElse("")
							+ "\",  value= \"" + Optional.ofNullable(event.getKeyValue().getValue())
							.map(bs -> bs.toString(UTF_8)).orElse("")
							+ "\"");
				}
				latch.countDown();
			});
			latch.await();
		} catch (Exception e) {
			if (watcher != null) {
				watcher.close();
				client.close();
			}
			throw e;
		}
	}
}
