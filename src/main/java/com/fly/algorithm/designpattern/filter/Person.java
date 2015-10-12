package com.fly.algorithm.designpattern.filter;

public class Person
{
    private int age;

    private int sex;

    private int score;
    
    private String name;
    
    private boolean target;

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    public int getSex()
    {
        return sex;
    }

    public void setSex(int sex)
    {
        this.sex = sex;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isTarget()
    {
        return target;
    }

    public void setTarget(boolean target)
    {
        this.target = target;
    }
}
