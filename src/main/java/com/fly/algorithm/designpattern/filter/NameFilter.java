package com.fly.algorithm.designpattern.filter;

public class NameFilter extends SuperFilter
{

    public void filt(Person p)
    {
        this.result= "Fly".equals(p.getName());
    }

}
