package com.googlecode.hellohbase;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.hellohbase.config.modules.Module;
import com.googlecode.hellohbase.pages.*;
import org.apache.wicket.Page;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.protocol.http.WebApplication;

public class WicketApplication extends WebApplication {
    /**
     * Constructor
     */
    public WicketApplication() {
    }

    @Override
    protected void init() {

        super.init();
        addComponentInstantiationListener(new GuiceComponentInjector(this, getGuiceInjector()));
        mountBookmarkablePage("/user", UserPage.class);
        mountBookmarkablePage("/tweet", TweetPage.class);
        mountBookmarkablePage("/follow", FollowPage.class);
        mountBookmarkablePage("/read", ReadPage.class);
        mountBookmarkablePage("/home", HomePage.class);
    }

    protected Injector getGuiceInjector() {
        return Guice.createInjector(new Module());
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

}
