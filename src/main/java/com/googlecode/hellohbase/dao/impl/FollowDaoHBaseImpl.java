package com.googlecode.hellohbase.dao.impl;

import com.googlecode.hellohbase.dao.api.FollowDao;
import com.googlecode.hellohbase.domain.User;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 10:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class FollowDaoHBaseImpl implements FollowDao {

    private static final byte[] _FOLLOWER = Bytes.toBytes("follower");
    private static final byte[] _FOLLOWED = Bytes.toBytes("followed");
    private static final byte[] _NAME = Bytes.toBytes("name");
    private static final byte[] _MAIL = Bytes.toBytes("mail");
    private static final byte[] _TWEET = Bytes.toBytes("msg");
    private static final byte[] _USERID = Bytes.toBytes("userid");
    private static final byte[] _TIME = Bytes.toBytes("time");
    //private static final byte[] _TIME = Bytes.toBytes("time");
    private static final byte[] _DEFAULT = Bytes.toBytes("data");
    private HTable followerTable, followedTable;
    private HTable tweetsTable;
    private HTable tweetlineTable;


    public FollowDaoHBaseImpl() throws IOException {

        this.followerTable = new HTable(HBaseConfiguration.create(), "followers");
        this.followedTable = new HTable(HBaseConfiguration.create(), "followeds");
        this.tweetsTable = new HTable(HBaseConfiguration.create(), "tweets");
        this.tweetlineTable = new HTable(HBaseConfiguration.create(), "tweetline");

    }

    /**
     * Allows an user to follow another. Creates rows at multiple tables which are follower, followed, tweetline.
     * user follower is recorded to table follower and followed, as follower user.
     * user followed is recorded to table follower and followed, as followed user.
     * row keys are generated so that it can easily fetched by user and date.
     *
     * @param follower   user that wants to follow
     * @param followed user that target to follow
     * @throws IOException
     */
    @Override
    public void follow(User follower, User followed) throws IOException {
        //byte[] followTime = Bytes.toBytes(System.currentTimeMillis());

        Put followerPut = new Put(generateFollowId(followed, follower));
        followerPut.add(_DEFAULT, _FOLLOWER, Bytes.toBytes(follower.getEmail()));
        followerPut.add(_DEFAULT, _FOLLOWED, Bytes.toBytes(followed.getEmail()));
        followerPut.add(_DEFAULT, _USERID, Bytes.toBytes(follower.getUserId()));
        followerTable.put(followerPut);

        Put followedPut = new Put(generateFollowId(follower, followed));
        followedPut.add(_DEFAULT, _FOLLOWER, Bytes.toBytes(follower.getEmail()));
        followedPut.add(_DEFAULT, _FOLLOWED, Bytes.toBytes(followed.getEmail()));
        followedPut.add(_DEFAULT, _USERID, Bytes.toBytes(followed.getUserId()));
        followedTable.put(followedPut);

        /**
         * add old tweets to new followers tweetline
         */
        Scan tweetScan = new Scan();
        tweetScan.setStartRow(Bytes.toBytes(followed.getUserId() + "-"));
        tweetScan.setStopRow(Bytes.toBytes((followed.getUserId() + 1) + "-"));
        ResultScanner tweetRS = tweetsTable.getScanner(tweetScan);


        List<Row> puts = new ArrayList<Row>();
        for (Result result : tweetRS) {
            byte[] timeBytes = result.getValue(_DEFAULT, _TIME);
            Long time = Bytes.toLong(timeBytes);

            Put put = new Put(Bytes.toBytes(follower.getUserId() + "-" + (Long.MAX_VALUE - time) + "-" + followed.getUserId()));


            put.add(_DEFAULT, _NAME, result.getValue(_DEFAULT, _NAME));
            put.add(_DEFAULT, _TWEET, result.getValue(_DEFAULT, _TWEET));
            put.add(_DEFAULT, _TIME, timeBytes);
            puts.add(put);


        }
        tweetRS.close();
        try {
            tweetlineTable.batch(puts);
        } catch (InterruptedException e) {
            e.printStackTrace();  // @TODO log and handle properly.
        }


    }

    /**
     *    Unfollows an user and deletes related record from various tables.
     * @param follower   user that will unfollow
     * @param followed user that being followed
     * @throws IOException
     */
    @Override
    public void unFollow(User follower, User followed) throws IOException {
        Delete followerDelete = new Delete(generateFollowId(follower, followed));
        followerDelete.deleteFamily(_DEFAULT);
        followerTable.delete(followerDelete);

        Delete followedDelete = new Delete(generateFollowId(followed, follower));
        followedDelete.deleteFamily(_DEFAULT);
        followedTable.delete(followedDelete);

        /**
         * delete old tweets from tweetline
         */
        Scan tweetlineScan = new Scan();
        tweetlineScan.setStartRow(Bytes.toBytes(follower.getUserId() + "-"));
        tweetlineScan.setStopRow(Bytes.toBytes((follower.getUserId() + 1) + "-"));
        ResultScanner tweetlineRS = tweetlineTable.getScanner(tweetlineScan);

        List<Row> deletes = new ArrayList<Row>();
        for (Result result : tweetlineRS) {
            Delete delete = new Delete(result.getRow());
            deletes.add(delete);

        }
        tweetlineRS.close();
        try {
            tweetlineTable.batch(deletes);
        } catch (InterruptedException e) {
            e.printStackTrace();  // @TODO log and handle properly.
        }


    }

    private byte[] generateFollowId(User follower, User followed) {
        return Bytes.toBytes(follower.getUserId() + "-" + followed.getUserId());

    }

}
