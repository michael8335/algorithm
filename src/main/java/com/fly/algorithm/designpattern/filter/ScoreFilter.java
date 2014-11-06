package com.fly.algorithm.designpattern.filter;

public class ScoreFilter extends SuperFilter
{

    public boolean filt(Person p)
    {
        return p.getScore()>=60;
    }
}
