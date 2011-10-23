package com.googlecode.hellohbase.dao.api;

import com.googlecode.hellohbase.domain.Tweet;
import com.googlecode.hellohbase.domain.User;

import java.io.IOException;
import java.util.List;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 11:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TweetDao {
    public void tweet(User user, String content) throws IOException;

    public List<Tweet> loadTweets(User user) throws IOException;


}
