package com.frozen.base;

import io.etcd.jetcd.Client;

import java.util.concurrent.TimeUnit;

/**
 * @author: Frozen
 * @create: 2019-11-29 13:56
 * @description:
 **/
public class BaseMain {
	public static void main(String[] args){
		try {
			Client client = Client.builder().endpoints(
					"http://101.200.38.80:2379"
			).build();
			KeyValue keyValue = new KeyValue(client);
			String key="name";
			String value="frozen";
			Watch watch = new Watch(client);
			Thread thread = new Thread(()->{
				try {
					watch.watchEtcdKey(key);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			thread.start();
			TimeUnit.SECONDS.sleep(1);
			keyValue.put(key,value);
			keyValue.put(key,"fenfen");
			keyValue.delete(key);
		}catch (Exception e){
			e.printStackTrace();
		}

	}
}
