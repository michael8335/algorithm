package com.fly.practice.concurrent.countlist;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 使用ExecutorService的invokeAll方法计算
 * @author 飞雪无情
 *
 */
public class CountSumWithCallable {

    /**
     * @param args
     * @throws InterruptedException 
     * @throws ExecutionException 
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int threadCounts =19;//使用的线程数
        long sum=0;
        ExecutorService exec=Executors.newFixedThreadPool(threadCounts);
        List<Callable<Long>> callList=new ArrayList<Callable<Long>>();
        //生成很大的List
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i <= 1000000; i++) {
            list.add(i);
        }
        int len=list.size()/threadCounts;//平均分割List
        //List中的数量没有线程数多（很少存在）
        if(len==0){
            threadCounts=list.size();//采用一个线程处理List中的一个元素
            len=list.size()/threadCounts;//重新平均分割List
        }
        for(int i=0;i<threadCounts;i++){
            final List<Integer> subList;
            if(i==threadCounts-1){
                subList=list.subList(i*len,list.size());
            }else{
                subList=list.subList(i*len, len*(i+1)>list.size()?list.size():len*(i+1));
            }
            //采用匿名内部类实现
            callList.add(new Callable<Long>(){
                public Long call() throws Exception {
                    long subSum=0L;
                    for(Integer i:subList){
                        subSum+=i;
                    }
                    System.out.println("分配给线程："+Thread.currentThread().getName()+"那一部分List的整数和为：\tSubSum:"+subSum);
                    return subSum;
                }
            });
        }
        List<Future<Long>> futureList = exec.invokeAll(callList);
        for(Future<Long> future:futureList){
            sum+=future.get();
        }
        exec.shutdown();
        System.out.println(sum);
    }

}
