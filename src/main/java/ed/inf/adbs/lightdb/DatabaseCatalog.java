package ed.inf.adbs.lightdb;

import java.io.*;
import java.util.*;

public class DatabaseCatalog {
    private static DatabaseCatalog dbCatalog = null;
    //input path
    private static String inputPath = "";
    //output path
    private static String outputPath = "";
    //db folder path
    private static String dbFolderPath = "";
    private static String dataFolderPath = "";
    private static String schemaPath = "";
    //aliases
    private static HashMap<String, String> aliases = new HashMap<>(); //<table, alias>
    //schemas
    private static HashMap<String, Integer> attrPos = new HashMap<>();


    private DatabaseCatalog() {
        aliases = new HashMap<>();
    }

    public static DatabaseCatalog getInstance() {
        if (dbCatalog == null)
            dbCatalog = new DatabaseCatalog();
        return dbCatalog;
    }

    public void initialiseInfo(String db, String ip, String op) {
        try {
            dbFolderPath = db;
            dataFolderPath = dbFolderPath + "/data";
            schemaPath = dbFolderPath + "/schema.txt";
            inputPath = ip;
            outputPath = op;
            aliases = new HashMap<>();

            attrPos = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader(schemaPath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split("\\s+");
                String alias = aliases.get(splitLine[0]);
                for (int i = 1; i < splitLine.length; ++i)
                    attrPos.put(alias + "." + splitLine[i], i-1);
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
            bw.write("");
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTablePath(String table) {
        String table2 = (aliases.size() == 0) ? table : getTableFromAlias(table);
        if (table2.equals("Boats")) return dataFolderPath + "/Boats.csv"; // todo si hi ha 2 // o nomes 1
        if (table2.equals("Reserves")) return dataFolderPath + "/Reserves.csv";
        else return dataFolderPath + "/Sailors.csv";
    }

    public static void setAliases(HashMap<String, String> aliases) {
        try {
            DatabaseCatalog.aliases = aliases;
            attrPos = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader(schemaPath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split("\\s+");
                String tableName = (aliases.size() == 0) ? splitLine[0] : aliases.get(splitLine[0]);
                for (int i = 1; i < splitLine.length; ++i)
                    attrPos.put(tableName + "." + splitLine[i], i-1);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getOutputPath() {
        return outputPath;
    }

    public static int getAttrPos(String attr) {
        if (attr.equals("*")) return -1;
        return attrPos.get(attr);
    }

    public static List<String> getTableSchema(String t) {
        List<String> s = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(schemaPath));
            String line;
            String table = (aliases.size() == 0) ? t : getTableFromAlias(t);
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split("\\s+");
                if (splitLine[0].equals(table)) {
                    for (int i = 1; i < splitLine.length; ++i)
                        s.add(t + "." + splitLine[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    private static String getTableFromAlias(String a) {
        for (Map.Entry<String, String> entry:aliases.entrySet()) {
            if (Objects.equals(a, entry.getValue())) {
                return entry.getKey();
            }
        }
        return "";
    }
}