package com.googlecode.hellohbase.dao.impl;

import com.googlecode.hellohbase.dao.api.UserDao;
import com.googlecode.hellohbase.domain.User;
import org.junit.*;

import java.io.IOException;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 11:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserDaoHBaseImplTest {
    private static UserDao userDao;

    @BeforeClass
    public static void init() throws IOException {
        userDao = new UserDaoHBaseImpl();
    }

    @Before
    public void setUp() throws Exception {


        User user = new User();
        user.setEmail("gettest@test.com");
        user.setName("gettestuser");
        userDao.create(user);


        user = new User();
        user.setEmail("deletetest@test.com");
        user.setName("deletetestuser");
        userDao.create(user);
    }

    @After
    public void tearDown() throws Exception {
        userDao.delete("gettest@test.com");
        userDao.delete("test@test.com");
        userDao.delete("deletetest@test.com");

    }


    @Test
    public void testCreate() {


        try {


            User user = new User();
            user.setEmail("test@test.com");
            user.setName("testuser");
            userDao.create(user);


            User foundUser = userDao.get("test@test.com");

            Assert.assertEquals("test@test.com", foundUser.getEmail());
            Assert.assertEquals("testuser", foundUser.getName());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }

    }

    @Test
    public void testDelete() {

        try {
            userDao.delete("deletetest@test.com");


            User foundUser = userDao.get("deletetest@test.com");

            Assert.assertNull(foundUser);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testGet() {


        try {
            User foundUser;
            foundUser = userDao.get("gettest@test.com");


            Assert.assertEquals("gettest@test.com", foundUser.getEmail());
            Assert.assertEquals("gettestuser", foundUser.getName());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }

    }
}
