package com.googlecode.hellohbase.pages;

import com.google.inject.Inject;
import com.googlecode.hellohbase.dao.api.UserDao;
import com.googlecode.hellohbase.domain.User;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import java.io.IOException;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserPage extends WebPage {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private String text;
    private String name;
    private String mail;

    @Inject
    private UserDao userDao;

    /**
     * Constructor that is invoked when page is invoked without a em.
     *
     * @param parameters Page parameters
     */
    public UserPage(final PageParameters parameters) {

        final Label label = new Label("text", new PropertyModel<String>(this, "text"));
        label.setOutputMarkupId(true);

        add(label);

        Form<Void> form = new Form<Void>("form");
        form.add(new TextField<String>("mail", new PropertyModel<String>(this, "mail")));
        form.add(new TextField<String>("name", new PropertyModel<String>(this, "name")));

        add(form);

        add(new AjaxSubmitLink("helloSubmit", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                User newUser = new User();
                newUser.setEmail(mail);
                newUser.setName(name);

                try {
                    userDao.create(newUser);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                UserPage.this.text = "+Hello " + name + ", now you are registered";
                target.addComponent(label);
            }
        });

        add(new AjaxSubmitLink("goodbyeSubmit", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    userDao.delete(mail);
                    UserPage.this.text = "-User deleted: " + mail;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                target.addComponent(label);
            }
        });

    }
}
