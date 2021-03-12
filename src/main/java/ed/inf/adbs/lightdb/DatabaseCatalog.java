package ed.inf.adbs.lightdb;

import java.io.*;
import java.util.HashMap;

public class DatabaseCatalog {
    private static DatabaseCatalog dbCatalog = null;
    //input i output path
    private static String inputPath = "";
    private static String outputPath = "";
    //db folder
    private static String dbFolderPath = "";
    private static String dataFolderPath = "";
    private static String schemaPath = "";
    //expected_output folder
    private static String expected_outputFolderPath = "";
    //aliases
    private static HashMap<String, String> aliases = new HashMap<>(); //<table, alias>
    //schemas todo cal implementar-lo
    private static HashMap<String, long[]> schemas = new HashMap<>();


    private DatabaseCatalog() {
        aliases = new HashMap<>();
    }

    public static DatabaseCatalog getInstance() {
        if (dbCatalog == null)
            dbCatalog = new DatabaseCatalog();
        return dbCatalog;
    }

    public void initialiseInfo(String db, String i, String o) {
        try {
            dbFolderPath = db;
            dataFolderPath = dbFolderPath + "/data";
            schemaPath = dataFolderPath + "/schema.txt";
            inputPath = i;
            outputPath = o;

            BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
            bw.write("");
            bw.close();

            dataFolderPath = dbFolderPath + "/data";
            schemaPath = dataFolderPath + "/schema.txt";
            aliases = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTablePath(String table) {
        if (table == "Boats") return dataFolderPath + "/Boats.csv"; // todo si hi ha 2 // o nomes 1
        if (table == "Reserves") return dataFolderPath + "/Reserves.csv";
        else return dataFolderPath + "/Sailors.csv";
    }

    public static HashMap<String, String> getAliases() {
        return aliases;
    }

    public static void setAliases(HashMap<String, String> aliases) {
        DatabaseCatalog.aliases = aliases;
    }

    public static String getOutputPath() {
        return outputPath;
    }

    public static void setOutputPath(String outputPath) {
        outputPath = outputPath;
    }
}