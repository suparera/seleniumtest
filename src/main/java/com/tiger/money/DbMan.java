package com.tiger.money;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: suparera
 * Date: 8/22/13
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class DbMan {
    public static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/settradedb?user=suparera&password=!@34erdfcvAB";

    public static BoneCP connPool;
    static {
        try {
            Class.forName(DRIVER_CLASS_NAME);
            BoneCPConfig config = new BoneCPConfig();	// create a new configuration object
            config.setJdbcUrl(JDBC_URL);
            connPool = new BoneCP(config);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }



    public static Connection getConnection(){
        try {
            return connPool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
