package ed.inf.adbs.lightdb;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class Tuple {
    private long[] tuple;
    private HashMap<String, Integer> attrSchema; //todo guardar atributs (sailor.A o s.a) i index de la tupla


    public Tuple(long[] t) {
        this.tuple = t;
        attrSchema = new HashMap<>();

    }

    public long getTuplePos(int i) {
        return tuple[i];
    }

    public long[] getTuple() {
        return tuple;
    }

    public int getTupleSize() {
        return tuple.length;
    }

    @Override
    public String toString() {
        if (tuple.length < 1) return ""; //todo cal?
 //       return Arrays.toString(tuple);
        String t = "";
        for (int i = 0; i < tuple.length; ++i) {
            t += tuple[i];
            if (i != tuple.length - 1) t += ",";
        }
        return t;
    }

    public void dump() {
        try {
            String outputPath = DatabaseCatalog.getOutputPath();
            String t = toString() + "\n";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
            bw.write(t);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
