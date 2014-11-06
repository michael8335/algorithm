package com.fly.algorithm.designpattern.filter;

public class AgeFilter extends SuperFilter
{

    public boolean filt(Person p)
    {
        return p.getAge()>=18;
    }
}
