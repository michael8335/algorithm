package com.fly.algorithm.designpattern.filter;

public class FilterTest
{

    public static void main(String[] args)
    {
        AgeFilter ageFilter=new AgeFilter();
        NameFilter nameFilter=new NameFilter();
        ScoreFilter scoreFilter=new ScoreFilter();
        ageFilter.nextFilter=nameFilter;
        nameFilter.nextFilter=scoreFilter;
        Person p=new Person();
        p.setAge(18);
        p.setName("Name");
        p.setScore(87);
        ageFilter.filt(p);
        System.out.println(p.isTarget());
    }

}
