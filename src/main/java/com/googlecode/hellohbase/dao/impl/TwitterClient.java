package com.googlecode.hellohbase.dao.impl;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;


/**
 * A basic implementation of a twitter data layer using HBase.
 *
 * @author Narendra Yadla
 */
public class TwitterClient {

    private static HTable tweetsTable;
    private static HTable usersTable;
    private static final int TIMELINE_LENGTH = 20;

    static {
        // The config file hbase-site.xml must be present in the class path
        try {
            //
            tweetsTable = new HTable(HBaseConfiguration.create(), "tweets");
            tweetsTable.setAutoFlush(true);
            usersTable = new HTable(HBaseConfiguration.create(), "users");
            usersTable.setAutoFlush(true);
        } catch (IOException e) {
            //LOGGER.error(e);
        }
    }

    /**
     * @param userHandle
     * @throws IOException
     */
    public static void getTimeline(String userHandle) throws IOException {
        Get getUser = new Get(Bytes.toBytes(userHandle));
        Result row = usersTable.get(getUser);

        //Get the timeline family map for userHandle
        //Structure of timeline map : key = tweet_id, value : user_handle of the tweeter
        NavigableMap<byte[], byte[]> timelineMap = row.getFamilyMap(Bytes.toBytes("timeline"));

        int numOfTweets = 0;
        for (Entry<byte[], byte[]> tweet : timelineMap.entrySet()) {
            if (numOfTweets < TIMELINE_LENGTH) {
                //this timeline map is already sorted by keys apriori by HBase. (during new_tweet, follow_user and other operations)
                //Print user handle who tweeted the tweet
                System.out.println("Tweet from " + Bytes.toString(tweet.getValue()) + ": ");
                //Print the tweet body now. Since we store only the tweet ids in the user timeline..we do a get operation on the tweet id
                //Get operations on HBase are really fast since HBase sorts keys in a lexicographic manner.
                Get getTweet = new Get(tweet.getKey());
                //get the tweet body from the tweetsTable
                String tweetMessage = Bytes.toString(tweetsTable.get(getTweet).getFamilyMap(Bytes.toBytes("message")).firstKey());
                System.out.println(tweetMessage + "\n");
                numOfTweets++;
            } else {
                break;
            }
        }
    }

    /**
     * Called when a user tweets
     *
     * @param twitterHandle
     * @param tweetMessage
     * @param twitterEpoch
     * @throws IOException
     */
    public static void addTweet(String twitterHandle, String tweetMessage, String twitterEpoch) throws IOException {
        //add the tweet to the tweet table
        byte[] tweetId = Bytes.toBytes(twitterEpoch);
        Put tweetPut = new Put(tweetId);
        tweetPut.add(Bytes.toBytes("userHandle"), Bytes.toBytes(twitterHandle), null);
        tweetPut.add(Bytes.toBytes("message"), Bytes.toBytes(tweetMessage), null);
        tweetsTable.put(tweetPut);

        //add the tweet to the follower timeline
        Get userGet = new Get(Bytes.toBytes(twitterHandle));
        userGet.addFamily(Bytes.toBytes("followers"));
        Result user = usersTable.get(userGet);
        NavigableMap<byte[], byte[]> followerMap = user.getFamilyMap(Bytes.toBytes("followers"));
        if (followerMap != null && !followerMap.isEmpty()) {
            Set<byte[]> followers = followerMap.keySet();
            //Do a batch put.
            List<Put> timelinePuts = new ArrayList<Put>();
            for (byte[] follower : followers) {
                //add this tweet to the timeline of each follower..this is really simple hbase sorts columns inside column family
                Put put = new Put(follower);
                put.add(Bytes.toBytes("timeline"), tweetId, null);
                timelinePuts.add(tweetPut);
            }
            usersTable.put(timelinePuts);
        }
    }


    /**
     * Called when a tweet is deleted from the twitter space.
     *
     * @param twitterEpoch
     * @throws IOException
     */
    public static void deleteTweet(String twitterEpoch) throws IOException {
        //add the tweet to the tweet table
        byte[] tweetId = Bytes.toBytes(twitterEpoch);
        Delete tweetDelete = new Delete(tweetId);
        tweetsTable.delete(tweetDelete);

        //delete the tweet from all the timelines across all the users.
        //Fetch the scanner for the timelines that are following this tweet.
        Scan timelineScan = new Scan();
        byte[] timelineFamily = Bytes.toBytes("timeline");
        timelineScan.addColumn(timelineFamily, tweetId);
        ResultScanner timelineScanner = usersTable.getScanner(timelineScan);
        //Do a batch delete.
        List<Delete> tweetDeleteList = new ArrayList<Delete>();
        for (Result timelineResult = timelineScanner.next(); (timelineResult != null); timelineResult = timelineScanner.next()) {
            Delete tweetDeleteFromTimeline = new Delete(timelineResult.getRow());
            tweetDeleteFromTimeline.deleteColumn(timelineFamily, tweetId);
            tweetDeleteList.add(tweetDeleteFromTimeline);
        }
        timelineScanner.close();
        usersTable.delete(tweetDeleteList);
    }

    /**
     * Called when a user follows another user.
     *
     * @param sourceHandle
     * @param destinationHandle
     * @throws IOException
     */
    public static void followUser(String sourceHandle, String destHandle) throws IOException {
        //update the timeline of source user to include the tweets of the destHandle.
        Scan tweetScanOfDest = new Scan();
        tweetScanOfDest.addColumn(Bytes.toBytes("userhandle"), Bytes.toBytes(destHandle));
        ResultScanner tweetScannerOfDest = tweetsTable.getScanner(tweetScanOfDest);

        List<Put> tweetPutList = new ArrayList<Put>();
        Put putTweetInSourceTimeline = new Put(Bytes.toBytes(sourceHandle));
        for (Result timelineResult = tweetScannerOfDest.next(); (timelineResult != null); timelineResult = tweetScannerOfDest.next()) {
            putTweetInSourceTimeline.add(Bytes.toBytes("timeline"), timelineResult.getRow(), null);
            tweetPutList.add(putTweetInSourceTimeline);
        }
        tweetScannerOfDest.close();
        usersTable.put(tweetPutList);

        //add destHandle to the list of follows of source user.
        Put sourceUserPut = new Put(Bytes.toBytes(sourceHandle));
        sourceUserPut.add(Bytes.toBytes("follows"), Bytes.toBytes(destHandle), null);
        usersTable.put(sourceUserPut);

        //add sourceHandle to the list of followers of dest user.
        Put destUserPut = new Put(Bytes.toBytes(destHandle));
        sourceUserPut.add(Bytes.toBytes("followers"), Bytes.toBytes(sourceHandle), null);
        usersTable.put(destUserPut);
    }

    /**
     * Called when a user unfollows another user.
     *
     * @param sourceHandle
     * @param destHandle
     * @throws IOException
     */
    public static void unfollowUser(String sourceHandle, String destHandle) throws IOException {
        //update the timeline of source user to exclude the tweets of the destHandle.
        Scan tweetScanOfDest = new Scan();
        tweetScanOfDest.addColumn(Bytes.toBytes("userhandle"), Bytes.toBytes(destHandle));
        ResultScanner tweetScannerOfDest = tweetsTable.getScanner(tweetScanOfDest);

        List<Delete> tweetDeleteList = new ArrayList<Delete>();
        Delete deleteTweetFromSourceTimeline = new Delete(Bytes.toBytes(sourceHandle));
        for (Result timelineResult = tweetScannerOfDest.next(); (timelineResult != null); timelineResult = tweetScannerOfDest.next()) {
            deleteTweetFromSourceTimeline.deleteColumn(Bytes.toBytes("timeline"), timelineResult.getRow());
            tweetDeleteList.add(deleteTweetFromSourceTimeline);
        }
        tweetScannerOfDest.close();
        usersTable.delete(tweetDeleteList);

        //delete destHandle from the list of follows of source user.
        Delete sourceUserDelete = new Delete(Bytes.toBytes(sourceHandle));
        sourceUserDelete.deleteColumn(Bytes.toBytes("follows"), Bytes.toBytes(destHandle));
        usersTable.delete(sourceUserDelete);

        //delete sourceHandle from the list of followers of dest user.
        Delete destUserDelete = new Delete(Bytes.toBytes(destHandle));
        sourceUserDelete.deleteColumn(Bytes.toBytes("followers"), Bytes.toBytes(sourceHandle));
        usersTable.delete(destUserDelete);
    }

    /**
     * Called when a new user account is created in the twitter-space
     *
     * @param userHandle
     * @throws IOException
     */
    public static void addUser(String twitterHandle) throws IOException {
        Put userPut = new Put(Bytes.toBytes(twitterHandle));
        usersTable.put(userPut);
    }

    public static void createTables() {
        try {
            System.out.println("starting...");

            System.out.println("getting config...");
            Configuration hc = HBaseConfiguration.create();
            System.out.println("connecting...");
            HBaseAdmin hba = new HBaseAdmin(hc);

            //row key is monotonous increasing unique twitter timestamp
            HTableDescriptor tweetsTable = new HTableDescriptor("tweets");
            //add column families
            tweetsTable.addFamily(new HColumnDescriptor("userhandle")); //user handle of the tweeter
            tweetsTable.addFamily(new HColumnDescriptor("message")); //tweet message
            System.out.println("creating tweets table...");
            hba.createTable(tweetsTable);

            //row key is user handle of the twitter user
            HTableDescriptor usersTable = new HTableDescriptor("users");
            //add column families
            //followers of this user
            usersTable.addFamily(new HColumnDescriptor("followers"));
            //people this user follows
            usersTable.addFamily(new HColumnDescriptor("follows"));
            //store latest tweet ids of the users this user follows (not necessarily the entire timeline, this can be preconfigured..
            //a user rarely sees beyond the latest 500 tweets) of people this user follows
            //..hbase sorts the columns automatically (i.e. in a chronological fashion)
            usersTable.addFamily(new HColumnDescriptor("timeline"));
            System.out.println("creating users table...");
            hba.createTable(usersTable);

            System.out.println("done!");
        } catch (Exception r) {


        }
    }

}