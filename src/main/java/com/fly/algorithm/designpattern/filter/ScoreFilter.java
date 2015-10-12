package com.fly.algorithm.designpattern.filter;

public class ScoreFilter extends SuperFilter
{

    public void filt(Person p)
    {
        this.result= p.getScore()>=60;
    }
}
