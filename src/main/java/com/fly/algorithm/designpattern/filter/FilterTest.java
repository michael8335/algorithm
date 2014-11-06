package com.fly.algorithm.designpattern.filter;

import java.util.Arrays;

public class FilterTest
{

    public static void main(String[] args)
    {
        Filter[] filters={new AgeFilter(),new NameFilter(),new ScoreFilter()};
        Person p=new Person();
        p.setAge(18);
        p.setName("Name");
        
    }

}
