package ed.inf.adbs.lightdb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ScanOperator extends Operator {
    private String table;
    private String path;
    private BufferedReader br;

    public ScanOperator(String table) { //initialisation: opens a file scan on the appropriate data file
        try {
            this.table = table;
            System.out.println("table tuplle " + table);
            this.path = DatabaseCatalog.getTablePath(table);
            br = new BufferedReader(new FileReader(path));

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Tuple getNextTuple() { //reads the next line from the file and returns the next tuple
        try {
            String line = br.readLine();
            if (line == null) return null;
            String[] parts = line.split(",");
            long[] cols = new long[parts.length];
            for (int i = 0; i < parts.length; ++i) {
                cols[i] = Integer.parseInt(parts[i]);
            }
            return new Tuple(cols, DatabaseCatalog.getTableSchema(table));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

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

    public String getTable() {
        return table;
    }
}
