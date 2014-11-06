package com.fly.algorithm.designpattern.filter;

public class NameFilter extends SuperFilter
{

    public boolean filt(Person p)
    {
        return "Fly".equals(p.getName());
    }

}
