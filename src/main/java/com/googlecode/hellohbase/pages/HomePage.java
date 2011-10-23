package com.googlecode.hellohbase.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;


public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;


    /**
     * Constructor that is invoked when page is invoked without a em.
     *
     * @param parameters Page parameters
     */
    public HomePage(final PageParameters parameters) {

        final Label label = new Label("text", "Select a page");
       add(label);

    }
}
