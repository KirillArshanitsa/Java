package WorkDbAwr;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static java.sql.DriverManager.getConnection;

class GetAwrFromDb {
    private String dbUrl;
    private String admUser;
    private String admPass;

    GetAwrFromDb(String dbUrl, String admUser, String admPass){
        this.dbUrl = dbUrl;
        this.admUser = admUser;
        this.admPass = admPass;
    }

    void getNoClusterAwrToHtmlFile (String awrFileName, String beginSnap, String endSnap) {
        try (Connection mydb = getConnection(dbUrl, admUser, admPass)){
            String sqlGetID = "select max(distinct DBID) as DBID  from DBA_HIST_SNAPSHOT";
            Statement myquery = mydb.createStatement();
            ResultSet dbIdResultSet = myquery.executeQuery(sqlGetID);
            dbIdResultSet.next();
            String dbId = dbIdResultSet.getString(1);
            File myFile = new File(awrFileName);
            myFile.createNewFile();
            FileWriter writer = new FileWriter(myFile);
            String sqlGetAwr = "SELECT output FROM TABLE (dbms_workload_repository.awr_report_html(" + dbId  + ",1," +  beginSnap + "," + endSnap + "))";
            ResultSet awrResultSet = myquery.executeQuery(sqlGetAwr);
            while (awrResultSet.next()) {
                if (awrResultSet.getString(1) != null) {
                    writer.write(awrResultSet.getString(1));
                }
            }
            writer.flush();
            writer.close();
        } catch(Exception ex){
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
