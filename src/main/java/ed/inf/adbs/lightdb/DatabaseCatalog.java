package ed.inf.adbs.lightdb;

import java.io.*;
import java.util.*;

/**
 * Singleton class used to keep track of database information (paths, schema, data, etc.)
 */
public class DatabaseCatalog {
    //DatabaseCatalog instance
    private static DatabaseCatalog dbCatalog = null;
    //paths
    private static String outputFilePath = "";
    private static String dataFolderPath = "";
    private static String schemaFilePath = "";
    //aliases
    private static HashMap<String, String> aliases = new HashMap<>(); //<alias, table>
    //position for each attribute of the schema file
    private static HashMap<String, Integer> attrPos = new HashMap<>();

    /**
     * DatabaseCatalog constructor
     */
    private DatabaseCatalog() {
    }

    /**
     * Returns the instance of the DatabaseCatalog, making sure there is only one instance
     * @return DatabaseCatalog instance
     */
    public static DatabaseCatalog getInstance() {
        if (dbCatalog == null) //if there is no instance, create one and return it. Else return that instance
            dbCatalog = new DatabaseCatalog();
        return dbCatalog;
    }

    /**
     * Initialises the information of the DatabaseCatalog (paths and schema)
     * @param db db directory path
     * @param op output file path
     */
    public void initialiseInfo(String db, String op) {
        try {
            dataFolderPath = db + "/data";
            schemaFilePath = db + "/schema.txt";
            outputFilePath = op;
            aliases = new HashMap<>();
            attrPos = new HashMap<>();

            BufferedReader br = new BufferedReader(new FileReader(schemaFilePath));
            String line;
            while ((line = br.readLine()) != null) {                //for every table in the schema,
                String[] splitLine = line.split("\\s+");     //keep track of the position (column number) of
                for (int i = 1; i < splitLine.length; ++i)          //each of their attributes
                    attrPos.put(splitLine[0] + "." + splitLine[i], i-1);
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath));
            bw.write(""); //empty the output file
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the path of a table's data file
     * @param table table name or alias
     * @return path of the table's data file
     */
    public static String getTablePath(String table) {
        String table2 = (aliases.size() == 0) ? table : aliases.get(table); //if it is an alias, use the table name
        return dataFolderPath + "/" + table2 + ".csv";
    }

    /**
     * Sets the tables' aliases in a hashmap (if any)
     * @param aliases hashmap with the table alias-table name relation or null
     */
    public static void setAliases(HashMap<String, String> aliases) {
        DatabaseCatalog.aliases = aliases;
    }

    /**
     * Returns the output file path
     * @return output file path
     */
    public static String getOutputFilePath() {
        return outputFilePath;
    }

    /**
     * Return the position of the attribute in the table's schema
     * @param attr attribute of a table (structure: Table.Attr or Alias.Attr)
     * @return position of the attribute in the table's schema
     */
    public static int getAttrPos(String attr) {
        if (attr.equals("*")) return -1; //used in ProjectOperator when the projected attributes are all the attributes

        if (aliases.size() == 0) //if no aliases are used, it is a direct map
            return attrPos.get(attr);
        else { //transform the attribute to have the table name and not the alias
            String[] splitAttr = attr.split("\\.");
            return attrPos.get(aliases.get(splitAttr[0]) + "." + splitAttr[1]);
        }
    }

    /**
     * Returns the table schema (attributes)
     * @param t table name or alias
     * @return table t's schema
     */
    public static List<String> getTableSchema(String t) {
        List<String> s = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(schemaFilePath));
            String line;
            String table = (aliases.size() == 0) ? t : aliases.get(t); //if it is an alias, use the table name
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split("\\s+");
                if (splitLine[0].equals(table)) { //for the table t's line
                    for (int i = 1; i < splitLine.length; ++i)
                        s.add(t + "." + splitLine[i]); //add each attribute to the list with the table's name or alias
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
}