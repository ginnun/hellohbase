package com.googlecode.hellohbase.dao.impl;

import com.googlecode.hellohbase.dao.api.FollowDao;
import com.googlecode.hellohbase.dao.api.UserDao;
import com.googlecode.hellohbase.domain.User;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 1:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class FollowDaoHBaseImplTest {

    private static FollowDao followDao;
    private static UserDao userDao;
    private User followerUser, followedUser;

    @BeforeClass
    public static void init() throws IOException {
        followDao = new FollowDaoHBaseImpl();

    }

    @Before
    public void setUp() throws Exception {


        userDao = new UserDaoHBaseImpl();

        this.followerUser = new User();
        followerUser.setEmail("followeRtest@test.com");
        followerUser.setName("followeRtestuser");
        userDao.create(followerUser);

        this.followedUser = new User();
        followedUser.setEmail("followeDtest@test.com");
        followedUser.setName("followeDtestuser");
        userDao.create(followedUser);

    }

    @Test
    public void testFollow() throws Exception {
        followDao.follow(followerUser, followedUser);
        followDao.unFollow(followerUser, followedUser);

    }
}
