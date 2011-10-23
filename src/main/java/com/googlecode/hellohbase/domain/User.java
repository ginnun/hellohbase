package com.googlecode.hellohbase.domain;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 9:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class User {
    private String email;
    private String name;

    private long userId;

    public User() {

        setUserId(System.currentTimeMillis());   // @TODO make this reliable and thread-safe.
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
