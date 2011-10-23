package com.googlecode.hellohbase.lookup;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * .
 * User: Hızır Sefa İrken
 * Date: 10/23/11
 * Time: 12:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class Lookup {

    private Map<String, HTable> tables = new HashMap<String, HTable>();


    /**
     * Lookup desired table.
     *
     * @param tableName desired table name.
     * @return found htable
     */
    public HTable table(String tableName) {
        HTable htable = tables.get(tableName);

        if (htable == null) {
            try {
                htable = new HTable(HBaseConfiguration.create(), tableName);
                tables.put(tableName, htable);
            } catch (IOException e) {
                e.printStackTrace();  //@TODO log and handle properly.
                return null;
            }
        }
        return htable;

    }
}
