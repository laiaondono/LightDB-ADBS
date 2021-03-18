package ed.inf.adbs.lightdb;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tuple {
    private long[] tuple;
    private List<String> attrSchema; //todo guardar atributs (sailor.A o s.a) i index de la tupla


    public Tuple(long[] t, List<String> s) {
        this.tuple = t;
        attrSchema = s;

    }

    public Tuple(Tuple leftTuple, Tuple rightTuple) {
        tuple = new long[leftTuple.getTupleSize() + rightTuple.getTupleSize()];
        for (int i = 0; i < tuple.length; ++i) {
            if (i < leftTuple.getTupleSize())
                tuple[i] = leftTuple.getValuePos(i);
            else tuple[i] = rightTuple.getValuePos(i - leftTuple.getTupleSize());
        }
        attrSchema = new ArrayList<>();
        attrSchema.addAll(leftTuple.getAttrSchema());
        attrSchema.addAll(rightTuple.getAttrSchema());
    }

    public long getValuePos(int i) {
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

    public List<String> getAttrSchema() {
        return attrSchema;
    }

    public int getAttrPos(String attr) {
        if (attr.equals("*")) return -1;
        return attrSchema.indexOf(attr);
    }

}
