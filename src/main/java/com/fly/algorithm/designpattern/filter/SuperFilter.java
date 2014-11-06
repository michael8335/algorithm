package com.fly.algorithm.designpattern.filter;

public abstract class SuperFilter implements Filter
{
    protected Filter nextFilter;
    protected boolean result;
    
}
