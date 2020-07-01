package WorkDbAwr;

import java.io.File;

public class MainWork {
    public static void main(String[] args) {
        String dbName = args[0];
        String dburl = "jdbc:oracle:thin:@" + dbName;
        String passFile = args[1];
        String beginAwrSmap = args[2];
        String endAwrSnap = args[3];
        String awrFileName = args[4];
        System.out.println("Создание AWR Report для бд " + dbName + " , снапшоты " + beginAwrSmap + " - " +endAwrSnap + " в файл " + awrFileName);
        WorkWithCreds workCredsFile = new WorkWithCreds(passFile,dbName);
        GetAwrFromDb getAwr = new GetAwrFromDb(dburl, workCredsFile.getAdmUser(), workCredsFile.getAdmPass());
        System.out.println("Статус удаления файла " + new File(passFile).delete());
        System.out.println("Запись AWR Report в файл.");
        getAwr.getNoClusterAwrToHtmlFile(awrFileName, beginAwrSmap, endAwrSnap);
        System.out.println("Finish");
    }
}
