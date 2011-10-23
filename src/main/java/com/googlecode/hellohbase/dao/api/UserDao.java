package com.googlecode.hellohbase.dao.api;

import com.googlecode.hellohbase.domain.User;

import java.io.IOException;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 9:09 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserDao {

    public void create(User user) throws IOException;

    public void delete(String email) throws IOException;

    public User get(String email) throws IOException;

}
