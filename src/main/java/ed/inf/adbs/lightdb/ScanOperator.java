package ed.inf.adbs.lightdb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Operator that represents the scan operator (leaf operator)
 */
public class ScanOperator extends Operator {
    //table name to be scanned
    private String table;
    //path of the table's data file
    private String path;
    //buffer reader to scan the data file
    private BufferedReader br;

    /**
     * ScanOperator constructor
     * @param table table name or alias
     */
    public ScanOperator(String table) {
        try {
            this.table = table;
            this.path = DatabaseCatalog.getTablePath(table);
            br = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Reads the next line of the data file
     * @return tuple with the line's values
     */
    @Override
    public Tuple getNextTuple() {
        try {
            String line = br.readLine(); //read next line
            if (line == null) return null; //if there are no more lines, we have finished the scanning
            String[] parts = line.split(","); //separate the values of the line (csv file so split by ",")
            long[] cols = new long[parts.length]; //array that will store the values of the new tuples
            for (int i = 0; i < parts.length; ++i)
                cols[i] = Integer.parseInt(parts[i]); //add every value to the array
            return new Tuple(cols, DatabaseCatalog.getTableSchema(table)); //create the new tuple with its schema
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    /**
     * Reset the buffer reader (used for the inner table in join)
     */
    @Override
    public void reset() {
        try {
            br.close();
            br = new BufferedReader(new FileReader(path));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Returns the table name or alias of the scan operator
     * @return table name or alias
     */
    public String getTable() {
        return table;
    }
}
