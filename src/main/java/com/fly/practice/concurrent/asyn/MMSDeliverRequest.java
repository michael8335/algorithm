package com.fly.practice.concurrent.asyn;

import java.util.Date;

public class MMSDeliverRequest
{
    private Date timeStamp;

    private String expiry;

    public Date getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getExpiry()
    {
        return expiry;
    }

    public void setExpiry(String expiry)
    {
        this.expiry = expiry;
    }
}
