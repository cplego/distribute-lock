package com.distributeredisson;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;

@SpringBootTest
class DistributeRedissonApplicationTests {

	@Test
	void contextLoads() {
	}
	@Test
	void testsss() {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://192.168.137.103:6379");

		RedissonClient redisson = Redisson.create(config);

		RLock rLock = redisson.getLock("order");

		try {
			rLock.lock(30, TimeUnit.SECONDS);
			System.out.println("我获得了锁！！！");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			System.out.println("我释放了锁！！");
			rLock.unlock();
		}

	}
	@Test
	public void redissonLock() throws BrokenBarrierException, InterruptedException {

		Config config = new Config();
/*		SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
		sentinelServersConfig.setMasterName("mymaster");
		sentinelServersConfig.setPassword("foobared");
		String h1 = "redis://192.168.137.103:26379";
		String h2 = "redis://192.168.137.104:26379";
		String h3 = "redis://192.168.137.105:26379";
		sentinelServersConfig.addSentinelAddress(new String[]{h1,h2,h3});
		sentinelServersConfig.setDatabase(0);
		sentinelServersConfig.setCheckSentinelsList(false);*/

		SingleServerConfig singleServerConfig  = config.useSingleServer();
		singleServerConfig.setAddress("redis://192.168.137.103:6379");
		singleServerConfig.setDatabase(0);
		singleServerConfig.setPassword("foobared");
		RedissonClient redissonClient = Redisson.create(config);
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		CountDownLatch countDownLatch = new CountDownLatch(5);
		CyclicBarrier cyclicBarrier = new CyclicBarrier(5);
		for(int i= 0 ;i<5 ;i++){

		executorService.submit(new Runnable() {

			@Override
			public void run() {

				try {
					System.out.println(Thread.currentThread().getId()+" wait");
					cyclicBarrier.await();
					System.out.println(Thread.currentThread().getId()+" in run");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
				RLock rLock = redissonClient.getLock("lock:redissonLock");
				try {
				rLock.lock();
				System.out.println(Thread.currentThread().getId()+" get lock");

					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}finally{
				rLock.unlock();
				System.out.println(Thread.currentThread().getId()+" release lock");
				}


				countDownLatch.countDown();
			}
		});
		}

		countDownLatch.await();
		System.out.println("thread end");

	}

}
