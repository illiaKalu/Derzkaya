package com.dev.IDAP.derzkaya;

/**
 * Created by illiakaliuzhnyi on 7/9/15.
 */
public class TimeStamps {

    private String timeStampType;
    private long offsetInMs;

    TimeStamps(String timeStampType, long offsetInMs){

        this.setTimeStampType(timeStampType);
        this.setOffsetInMs(offsetInMs);

    }


    public String getTimeStampType() {
        return timeStampType;
    }

    public void setTimeStampType(String timeStampType) {
        this.timeStampType = timeStampType;
    }

    public long getOffsetInMs() {
        return offsetInMs;
    }

    public void setOffsetInMs(long offsetInMs) {
        this.offsetInMs = offsetInMs;
    }
}
