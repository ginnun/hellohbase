package com.googlecode.hellohbase.dao.impl;

import com.googlecode.hellohbase.dao.api.UserDao;
import com.googlecode.hellohbase.domain.User;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 9:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserDaoHBaseImpl implements UserDao {
    private static final byte[] _NAME = Bytes.toBytes("name");
    private static final byte[] _ID = Bytes.toBytes("id");
    private static final byte[] _MAIL = Bytes.toBytes("mail");
    private static final byte[] _TWEET = Bytes.toBytes("msg");
    private static final byte[] _DEFAULT = Bytes.toBytes("data");
    private HTable usersTable;

    public UserDaoHBaseImpl() throws IOException {
        this.usersTable = new HTable(HBaseConfiguration.create(), "users");
    }

    @Override
    public void create(User user) throws IOException {


        Put userPut = new Put(Bytes.toBytes(user.getEmail()));
        userPut.add(_DEFAULT, _NAME, Bytes.toBytes(user.getName()));
        userPut.add(_DEFAULT, _ID, Bytes.toBytes(user.getUserId()));

        usersTable.put(userPut);

    }

    @Override
    public void delete(String email) throws IOException {
        Delete userDelete = new Delete(Bytes.toBytes(email));
        userDelete.deleteFamily(_DEFAULT);
        usersTable.delete(userDelete);
    }

    @Override
    public User get(String email) throws IOException {
        Get userGet = new Get(Bytes.toBytes(email));

        Result result = usersTable.get(userGet);
        KeyValue columnLatest = result.getColumnLatest(_DEFAULT, _NAME);
        KeyValue columnId = result.getColumnLatest(_DEFAULT, _ID);


        if (columnLatest == null)
            return null; // user not found

        User foundUser = new User();
        foundUser.setEmail(email);
        foundUser.setName(new String(columnLatest.getValue()));
        foundUser.setUserId(Bytes.toLong(columnId.getValue()));

        return foundUser;


    }


}
