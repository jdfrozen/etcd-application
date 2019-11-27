package com.frozen.select.master.server;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: Frozen
 * @create: 2019-11-26 14:24
 * @description: 应用一
 **/
public class AppServer2 {
	private Client client;
	private Lock lock;
	private Lease lease;
	//单位：秒
	private long lockTTl = 1;
	private ByteSequence lockKey = ByteSequence.from("/root/lock", StandardCharsets.UTF_8);
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);

	public AppServer2() {
		client = Client.builder().endpoints(
				"http://101.200.38.80:2379"
		).build();
		lock = client.getLockClient();
		lease = client.getLeaseClient();
	}

	public static void main(String[] args)throws Exception{
		AppServer2 server = new AppServer2();
		server.lockTest1toMaster();
	}

	public void lockTest1toMaster() throws InterruptedException, ExecutionException {
		long leaseId = lease.grant(lockTTl).get().getID();
		lease.keepAlive(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {
			@Override
			public void onNext(LeaseKeepAliveResponse value) {
				System.err.println("LeaseKeepAliveResponse value:"+ value.getTTL());
			}
			@Override
			public void onError(Throwable t) {
				scheduledThreadPool.shutdownNow();
				t.printStackTrace();

			}
			@Override
			public void onCompleted() {
				scheduledThreadPool.shutdownNow();
			}
		});
		lock.lock(lockKey, leaseId).get().getKey();
		scheduledThreadPool.submit(() -> {
			while (true) {
				System.err.println("我是AppServer2服务开始工作了");
				TimeUnit.SECONDS.sleep(1);
			}
		});
		TimeUnit.DAYS.sleep(1);
	}

}
