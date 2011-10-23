package com.googlecode.hellohbase.config.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.googlecode.hellohbase.dao.api.FollowDao;
import com.googlecode.hellohbase.dao.api.TweetDao;
import com.googlecode.hellohbase.dao.api.UserDao;
import com.googlecode.hellohbase.dao.impl.FollowDaoHBaseImpl;
import com.googlecode.hellohbase.dao.impl.TweetDaoHBaseImpl;
import com.googlecode.hellohbase.dao.impl.UserDaoHBaseImpl;

public class Module extends AbstractModule {


    @Override
    protected void configure() {

        bind(UserDao.class).to(UserDaoHBaseImpl.class).in(Singleton.class);
        bind(TweetDao.class).to(TweetDaoHBaseImpl.class).in(Singleton.class);
        bind(FollowDao.class).to(FollowDaoHBaseImpl.class).in(Singleton.class);

    }
}