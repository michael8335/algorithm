package com.fly.algorithm.designpattern.filter;

public class AgeFilter extends SuperFilter
{

    public void filt(Person p)
    {
        this.result= p.getAge()>=18;
        if(result&&this.nextFilter!=null)
        {
            nextFilter.filt(p); 
        }
    }
}
