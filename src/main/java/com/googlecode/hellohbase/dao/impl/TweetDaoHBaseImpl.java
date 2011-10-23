package com.googlecode.hellohbase.dao.impl;

import com.googlecode.hellohbase.dao.api.TweetDao;
import com.googlecode.hellohbase.domain.Tweet;
import com.googlecode.hellohbase.domain.User;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 11:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class TweetDaoHBaseImpl implements TweetDao {

    private static final byte[] _NAME = Bytes.toBytes("name");
    private static final byte[] _MAIL = Bytes.toBytes("mail");
    private static final byte[] _TWEET = Bytes.toBytes("msg");
    private static final byte[] _DEFAULT = Bytes.toBytes("data");
    private static final byte[] _USERID = Bytes.toBytes("userid");
    private static final byte[] _TIME = Bytes.toBytes("time");
    private HTable tweetsTable;
    private HTable tweetlineTable;
    private HTable followersTable;


    public TweetDaoHBaseImpl() throws IOException {
        tweetsTable = new HTable(HBaseConfiguration.create(), "tweets");
        tweetlineTable = new HTable(HBaseConfiguration.create(), "tweetline");
        followersTable = new HTable(HBaseConfiguration.create(), "followers");

    }

    @Override
    public void tweet(User user, String tweetText) throws IOException {
        final long epoch = System.currentTimeMillis();
        final long tranposeEpoch = Long.MAX_VALUE - epoch;
        final byte[] epochBytes = Bytes.toBytes(epoch);

        final byte[] tweetBytes = Bytes.toBytes(tweetText);
        byte[] nameBytes = Bytes.toBytes(user.getName());

        /**
         * put tweet into tweets
         */

        Put tweetRowPut = new Put(generateTweetId(user));

        tweetRowPut.add(_DEFAULT, _NAME, nameBytes);
        tweetRowPut.add(_DEFAULT, _MAIL, Bytes.toBytes(user.getEmail()));
        tweetRowPut.add(_DEFAULT, _TWEET, tweetBytes);
        tweetRowPut.add(_DEFAULT, _TIME, epochBytes);

        tweetsTable.put(tweetRowPut);


        /**
         * put tweets for followers
         */
        Scan followerScan = new Scan();
        followerScan.setStartRow(Bytes.toBytes(user.getUserId() + "-"));
        followerScan.setStopRow(Bytes.toBytes((user.getUserId() + 1) + "-"));

        ResultScanner followerRS = followersTable.getScanner(followerScan);

        /**
         * put users on tweet to her own  tweetline
         */
        Put put = new Put(Bytes.toBytes(user.getUserId() + "-" + tranposeEpoch + "-" + user.getUserId()));
        put.add(_DEFAULT, _NAME, nameBytes);
        put.add(_DEFAULT, _TWEET, tweetBytes);
        put.add(_DEFAULT, _TIME, epochBytes);


        List<Row> puts = new ArrayList<Row>();
        puts.add(put);
        for (Result result : followerRS) {

            Long followerid = Bytes.toLong(result.getColumnLatest(_DEFAULT, _USERID).getValue());

            put = new Put(Bytes.toBytes(followerid + "-" + tranposeEpoch + "-" + user.getUserId()));
            put.add(_DEFAULT, _NAME, nameBytes);
            put.add(_DEFAULT, _TWEET, tweetBytes);
            put.add(_DEFAULT, _TIME, epochBytes);

            puts.add(put);
        }
        followerRS.close();
        try {
            tweetlineTable.batch(puts);
        } catch (InterruptedException e) {
            e.printStackTrace();  //@TODO log and handle properly.
        }


    }

    @Override
    public List<Tweet> loadTweets(User user) throws IOException {

        Scan tweetlineScan = new Scan();
        tweetlineScan.setStartRow(Bytes.toBytes(user.getUserId() + "-"));
        tweetlineScan.setStopRow(Bytes.toBytes((user.getUserId() + 1) + "-"));

        ResultScanner tweetlineRS = tweetlineTable.getScanner(tweetlineScan);

        List<Tweet> tweets = new ArrayList();

        for (Result result : tweetlineRS) {
            Tweet tweet = new Tweet();

            tweet.setMsg(Bytes.toString(result.getValue(_DEFAULT, _TWEET)));
            tweet.setUserName(Bytes.toString(result.getValue(_DEFAULT, _NAME)));
            tweet.setTime(new Date(Bytes.toLong(result.getValue(_DEFAULT, _TIME))));

            tweets.add(tweet);

        }
        tweetlineRS.close();
        return tweets;

    }


    private byte[] generateTweetId(User user) {
        return Bytes.toBytes(user.getUserId() + "-" + (Long.MAX_VALUE - System.currentTimeMillis()));

    }

}
