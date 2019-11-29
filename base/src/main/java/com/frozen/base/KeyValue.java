package com.frozen.base;

import static com.google.common.base.Charsets.UTF_8;

import java.util.concurrent.ExecutionException;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;

/**
 * @author: Frozen
 * @create: 2019-11-29 13:56
 * @description:
 **/
public class KeyValue {
	private Client client;

	public KeyValue(Client client) {
		this.client = client;
	}

	public void put(String key, String value) throws Exception {
		client.getKVClient().put(ByteSequence.from(key, UTF_8), ByteSequence.from(value, UTF_8)).get();
	}


	public String get(String key) throws Exception {
		GetResponse getResponse = client.getKVClient()
				.get(ByteSequence.from(key, UTF_8), GetOption.newBuilder().build()).get();
		if (getResponse.getKvs().isEmpty()) {
			return null;
		}
		return getResponse.getKvs().get(0).getValue().toString(UTF_8);
	}


	public void delete(String key) throws InterruptedException, ExecutionException {
		client.getKVClient().delete(ByteSequence.from(key, UTF_8)).get();
	}
}
