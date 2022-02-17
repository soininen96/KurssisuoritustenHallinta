package kurssisuoritustenhallinta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Sisaltaa metodit yhteyden luomiseen tietokantaan seka tietokannan ja taulujen
 * luomiseen
 * @author Patrik Soininen
 */
public class TietokantaYhteys {
    
    /**
     * tietokannan osoite
     */
    private static final String CONNSTRING = "*************************************************"
            + "*********?user=*************&password=************";

    /**
     * luo yhteyden tietokantaan
     * @return connection
     * @throws SQLException
     */
    protected static Connection openConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(CONNSTRING);
        return conn;
    }

    /**
     * sulkee yhteyden tietokantaan
     * @param c connection
     * @throws SQLException
     */
    protected static void closeConnection(Connection c) throws SQLException {
        if (c != null) {
            c.close();
        }
        System.out.println("\t>> Tietokantayhteys suljettu");
    }

    /**
     * luo tietokannan
     * @param c connection
     * @param db tietokannan nimi
     * @throws SQLException
     */
    protected static void createDatabase(Connection c, String db) throws SQLException {

        Statement stmt = c.createStatement();
        stmt.executeQuery("DROP DATABASE IF EXISTS " + db);
        System.out.println("\t>> Tietokanta " + db + " tuhottu");

        stmt.executeQuery("CREATE DATABASE " + db);
        System.out.println("\t>> Tietokanta " + db + " luotu");
    }

    /**
     * ottaa tietokannan kayttoon
     * @param c connection
     * @throws SQLException
     */
    protected static void useDatabase(Connection c) throws SQLException {
        
        Statement stmt = c.createStatement();
        stmt.executeQuery("USE 2006291_Suoritus_Tietokanta");
    }

    /**
     * luo yksittaisen taulun tietokantaan
     * @param c connection
     * @param sql string muodossa oleva sql kysely joka luo taulun
     * @throws SQLException
     */
    protected static void createTable(Connection c, String sql) throws SQLException {

        Statement stmt = c.createStatement();
        stmt.executeQuery(sql);
        System.out.println("\t>> Taulu luotu");
    }
}
