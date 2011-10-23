package com.googlecode.hellohbase.dao.impl;

import com.googlecode.hellohbase.dao.api.TweetDao;
import com.googlecode.hellohbase.dao.api.UserDao;
import com.googlecode.hellohbase.domain.Tweet;
import com.googlecode.hellohbase.domain.User;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 12:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class TweetDaoHBaseImplTest extends TestCase {
    private UserDao userDao;
    private TweetDao tweetDao;
    private User tweetUser;

    @Override
    public void setUp() throws Exception {
        this.userDao = new UserDaoHBaseImpl();
        this.tweetDao = new TweetDaoHBaseImpl();

        this.tweetUser = new User();
        tweetUser.setEmail("tweetusertest@test.com");
        tweetUser.setName("tweetusertest");
        userDao.create(tweetUser);

    }

    @Override
    public void tearDown() throws Exception {
        this.userDao.delete(tweetUser.getEmail());
    }

    @Test
    public void testTweet() throws Exception {

        tweetDao.tweet(userDao.get("user1"), "testing tweet");
    }

    @Test
    public void testLoadTweets() throws IOException {
        List<Tweet> tweetList = tweetDao.loadTweets(userDao.get("user2"));


    }


}
