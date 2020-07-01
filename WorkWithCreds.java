package WorkDbAwr;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class WorkWithCreds {
    private String admUser;
    private String admPass;

    String getAdmUser(){
        return this.admUser;
    }
    String getAdmPass(){
        return this.admPass;
    }

    WorkWithCreds(String pathToPropFile, String dbConnectStr){
        try {
            Path pathToFile = Paths.get(pathToPropFile);
            List<String > lines =  Files.readAllLines(pathToFile);
            for (String line :lines){
                 String tempList [] = line.split("=");
                 if (tempList[0].equals(dbConnectStr)){
                     String tempListLoginPass [] = tempList[1].split(";");
                     this.admUser = tempListLoginPass[0];
                     this.admPass = tempListLoginPass[1];
                }
            }
            if (this.admUser ==null & this.admPass == null){
                System.out.println("Не определён логин и пароль для подкл к БД");
                System.exit(1);
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
