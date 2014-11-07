package com.fly.practice.concurrent;


import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * CyclicBarrier 表示大家彼此等待，大家集合好后才开始出发，分散活动后又在i指定地点集合碰面， 这就好比整个公司的人员利用周末时间集体郊游一样，先各自从家出发到公司集合后，
 * 再同时出发到公园游玩，在指定地点集合后再同时开始就餐……
 * 
 * @author fly
 */
public class CyclicBarrierDemo
{
    public static void main(String[] args)
    {
        ExecutorService service = Executors.newCachedThreadPool();
        final CyclicBarrier cb = new CyclicBarrier(3); // 三个线程同时到达
        for (int i = 0; i < 3; i++ )
        {
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep((long)(Math.random() * 10000));
                        System.out.println("线程" + Thread.currentThread().getName()
                                           + "即将到达集合地点1，当前已有" + (cb.getNumberWaiting() + 1)
                                           + "个已到达"
                                           + (cb.getNumberWaiting() == 2 ? "都到齐了，继续走啊" : "正在等候"));
                        try
                        {
                            cb.await();
                        }
                        catch (BrokenBarrierException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Thread.sleep((long)(Math.random() * 10000));
                        System.out.println("线程" + Thread.currentThread().getName()
                                           + "即将到达集合地点2，当前已有" + (cb.getNumberWaiting() + 1)
                                           + "个已到达"
                                           + (cb.getNumberWaiting() == 2 ? "都到齐了，继续走啊" : "正在等候"));
                        try
                        {
                            cb.await();
                        }
                        catch (BrokenBarrierException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Thread.sleep((long)(Math.random() * 10000));
                        System.out.println("线程" + Thread.currentThread().getName()
                                           + "即将到达集合地点3，当前已有" + (cb.getNumberWaiting() + 1)
                                           + "个已到达"
                                           + (cb.getNumberWaiting() == 2 ? "都到齐了，继续走啊" : "正在等候"));
                        try
                        {
                            cb.await();
                        }
                        catch (BrokenBarrierException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };
            service.execute(runnable);
        }
        service.shutdown();
    }
}
