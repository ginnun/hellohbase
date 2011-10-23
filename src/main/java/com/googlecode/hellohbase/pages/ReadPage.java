package com.googlecode.hellohbase.pages;

import com.google.inject.Inject;
import com.googlecode.hellohbase.dao.api.TweetDao;
import com.googlecode.hellohbase.dao.api.UserDao;
import com.googlecode.hellohbase.domain.Tweet;
import com.googlecode.hellohbase.domain.User;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;

import java.io.IOException;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReadPage extends WebPage {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private String text;
    private String mail;

    @Inject
    private TweetDao tweetDao;
    @Inject
    private UserDao userDao;


    /**
     * Constructor that is invoked when page is invoked without a em.
     *
     * @param parameters Page parameters
     */
    public ReadPage(final PageParameters parameters) {

        final Label label = new Label("text", new PropertyModel<String>(this, "text"));
        label.setOutputMarkupId(true);
        final RepeatingView view = new RepeatingView("repeater");
        final WebMarkupContainer container = new WebMarkupContainer("container");
        view.setOutputMarkupId(true);
        container.setOutputMarkupId(true);

        add(label);

        Form<Void> form = new Form<Void>("form");
        form.add(new TextField<String>("mail", new PropertyModel<String>(this, "mail")));


        add(form);

        add(new AjaxSubmitLink("helloSubmit", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {


                try {
                    User user = userDao.get(mail);

                    view.removeAll();

                    Integer i = 1;
                    for (Tweet tweet : tweetDao.loadTweets(user)) {
                        view.add(new Label(i.toString(), tweet.getMsg() + " - " + tweet.getUserName() + " at " + tweet.getTime()));
                        i++;
                    }


                } catch (IOException e) {
                    e.printStackTrace();  // @TODO log and handle properly.
                }
                ReadPage.this.text = "Home page of " + mail;
                target.addComponent(label);
                target.addComponent(container);
            }
        });

        container.add(view);
        add(container);


    }
}
