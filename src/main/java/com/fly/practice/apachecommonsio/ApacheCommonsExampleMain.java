package com.fly.practice.apachecommonsio;


import java.io.IOException;


/**
 * http://www.javacodegeeks.com/2014/10/apache-commons-io-tutorial.html
 * 
 * @author fly
 */
public class ApacheCommonsExampleMain
{

    public static void main(String[] args)
        throws IOException
    {
        UtilityExample.runExample();
        FileMonitorExample.runExample();
        FiltersExample.runExample();
        InputExample.runExample();
        OutputExample.runExample();
        ComparatorExample.runExample();
    }
}
