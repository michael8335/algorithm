package com.fly.practice.concurrent.countlist;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 计算List中所有整数的和<br>
 * 采用多线程，分割List计算
 * @author 飞雪无情
 * @since 2010-7-12
 */
public class CountListIntegerSum {
    private long sum;//存放整数的和
    private CyclicBarrier barrier;//障栅集合点(同步器)
    private List<Integer> list;//整数集合List
    private int threadCounts;//使用的线程数
    public CountListIntegerSum(List<Integer> list,int threadCounts) {
        this.list=list;
        this.threadCounts=threadCounts;
    }
    /**
     * 获取List中所有整数的和
     * @return
     */
    public long getIntegerSum(){
        ExecutorService exec=Executors.newFixedThreadPool(threadCounts);
        int len=list.size()/threadCounts;//平均分割List
        //List中的数量没有线程数多（很少存在）
        if(len==0){
            threadCounts=list.size();//采用一个线程处理List中的一个元素
            len=list.size()/threadCounts;//重新平均分割List
        }
        barrier=new CyclicBarrier(threadCounts+1);
        for(int i=0;i<threadCounts;i++){
            //创建线程任务
            if(i==threadCounts-1){//最后一个线程承担剩下的所有元素的计算
                exec.execute(new SubIntegerSumTask(list.subList(i*len,list.size())));
            }else{
                exec.execute(new SubIntegerSumTask(list.subList(i*len, len*(i+1)>list.size()?list.size():len*(i+1))));
            }
        }
        try {
            barrier.await();//关键，使该线程在障栅处等待，直到所有的线程都到达障栅处
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName()+":Interrupted");
        } catch (BrokenBarrierException e) {
            System.out.println(Thread.currentThread().getName()+":BrokenBarrier");
        }
        exec.shutdown();
        return sum;
    }
    /**
     * 分割计算List整数和的线程任务
     * @author lishuai
     *
     */
    public class SubIntegerSumTask implements Runnable{
        private List<Integer> subList;
        public SubIntegerSumTask(List<Integer> subList) {
            this.subList=subList;
        }
        public void run() {
            long subSum=0L;
            for (Integer i : subList) {
                subSum += i;
            }  
            synchronized(CountListIntegerSum.this){//在CountListIntegerSum对象上同步
                sum+=subSum;
            }
            try {
                barrier.await();//关键，使该线程在障栅处等待，直到所有的线程都到达障栅处
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName()+":Interrupted");
            } catch (BrokenBarrierException e) {
                System.out.println(Thread.currentThread().getName()+":BrokenBarrier");
            }
            System.out.println("分配给线程："+Thread.currentThread().getName()+"那一部分List的整数和为：\tSubSum:"+subSum);
        }
        
    }
    
}
