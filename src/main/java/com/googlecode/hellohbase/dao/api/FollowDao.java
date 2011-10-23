package com.googlecode.hellohbase.dao.api;

import com.googlecode.hellohbase.domain.User;

import java.io.IOException;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 10:45 AM
 * To change this template use File | Settings | File Templates.
 */
public interface FollowDao {

    public void follow(User follower, User followed) throws IOException;

    public void unFollow(User follower, User followed) throws IOException;
}
