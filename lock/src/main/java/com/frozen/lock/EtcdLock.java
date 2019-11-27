package com.frozen.lock;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.lease.LeaseTimeToLiveResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.options.LeaseOption;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author: Frozen
 * @create: 2019-11-27 19:48
 * @description:
 **/
public class EtcdLock {
	private Long leaseId;
	private ByteSequence byteSequence;
	private Client client;

	public EtcdLock(Client client) {
		this.client = client;
	}

	/**
	 * @param lockName
	 * @param lockTime
	 * @param waitTime
	 * @return
	 */
	public Boolean tryLock(String lockName,long lockTime, Long waitTime) {
		try {
			leaseId = client.getLeaseClient().grant(lockTime).get(1, TimeUnit.SECONDS).getID();
			ByteSequence req = ByteSequence.from(lockName, Charset.defaultCharset());
			CompletableFuture<LockResponse> lock = client.getLockClient().lock(req, leaseId);
			waitTime = Math.max(500, waitTime);
			byteSequence = lock.get(waitTime, TimeUnit.MILLISECONDS).getKey();
			CompletableFuture<LeaseTimeToLiveResponse> lease = client.getLeaseClient().timeToLive(leaseId, LeaseOption.DEFAULT);
			long ttl = lease.get(1, TimeUnit.SECONDS).getTTl();
			if (ttl > 0) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	public void unlockLease() {
		client.getLeaseClient().revoke(leaseId);
	}

	public void unlock() {
		client.getLockClient().unlock(byteSequence);
	}

	public void keepAliveOnce() {
		client.getLeaseClient().keepAliveOnce(leaseId);
	}
}
