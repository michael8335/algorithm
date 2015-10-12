package com.fly.practice.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ForkJoinPoolDemo1 {
    
    public final static ForkJoinPool mainPool = new ForkJoinPool();
    
    public static void main(String[] args){
        int n = 26;
        int[] a = new int[n];
        for(int i=0; i<n; i++) {
            a[i] = i;
        }
        SubTask task = new SubTask(a, 0, n);
        mainPool.invoke(task);
        for(int i=0; i<n; i++) {
            System.out.print(a[i]+" ");
        }
    }
}

class SubTask extends RecursiveAction {

    private static final long serialVersionUID = 1L;
    
    private int[] a;
    private int beg;
    private int end;
    
    public SubTask(int[] a, int beg, int end) {
        super();
        this.a = a;
        this.beg = beg;
        this.end = end;
    }

    @Override
    protected void compute() {
        if(end-beg>10) {
            int mid = (beg+end) / 2;
            SubTask t1 = new SubTask(a, beg, mid);
            SubTask t2 = new SubTask(a, mid, end);
            invokeAll(t1, t2);
        }else {
            for(int i=beg; i<end; i++) {
                a[i] = a[i] + 1;
            }
        }
    }
}