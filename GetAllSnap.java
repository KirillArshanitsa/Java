package WorkDbAwr;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static java.sql.DriverManager.getConnection;

public class GetAllSnap{

    private String dbUrl;
    private String admUser;
    private String admPass;
    private String fileForAllSnap;
    private String getAllSnapSql ="select SNAP_ID || ' (' ||to_char(BEGIN_INTERVAL_TIME, 'dd/mm/yy_hh24:mi:ss') || ' '|| to_char( END_INTERVAL_TIME, 'dd/mm/yy_hh24:mi:ss') || ')' as RESULT " +
            "from DBA_HIST_SNAPSHOT where DBID = (select max(DBID)from DBA_HIST_SNAPSHOT) order by SNAP_ID desc";


    GetAllSnap(String dbUrl, String admUser, String admPass, String fileForAllSnap){
        this.dbUrl = dbUrl;
        this.admUser = admUser;
        this.admPass = admPass;
        this.fileForAllSnap = fileForAllSnap;
    }

    void getAllSnap () {
        try (Connection mydb = getConnection(dbUrl, admUser, admPass)){
            Statement myquery = mydb.createStatement();
            File myFile = new File(fileForAllSnap);
            myFile.createNewFile();
            FileWriter writer = new FileWriter(myFile);
            ResultSet awrResultSet = myquery.executeQuery(getAllSnapSql);
            while (awrResultSet.next()) {
                if (awrResultSet.getString(1) != null) {
                    writer.write(awrResultSet.getString(1) + "\n");
                }
            }
            writer.flush();
            writer.close();
            System.out.println("Запись файла с номерами AWR Snap выполнена");
        } catch(Exception ex){
            System.out.println("Ошибка при записи файла с номерами AWR Snap");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        String dbName = args[0];
        String dburl = "jdbc:oracle:thin:@" + dbName;
        String tempPassFile = args[1];
        String allSnapFiles = args[2];
        WorkWithCreds workCredsFile = new WorkWithCreds(tempPassFile,dbName);
        GetAllSnap getAllSnap = new GetAllSnap(dburl, workCredsFile.getAdmUser(), workCredsFile.getAdmPass(), allSnapFiles);
        getAllSnap.getAllSnap();
    }
}
