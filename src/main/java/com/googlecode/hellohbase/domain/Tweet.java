package com.googlecode.hellohbase.domain;

import java.util.Date;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class Tweet {
    String userName;
    String msg;
    Date time;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
