package ed.inf.adbs.lightdb;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a tuple of a table's data
 */
public class Tuple {
    //tuple values
    private long[] tuple;
    //tuple schema
    private List<String> attrSchema;

    /**
     * Tuple constructor
     * @param t values
     * @param s schema
     */
    public Tuple(long[] t, List<String> s) {
        this.tuple = t;
        attrSchema = s;

    }

    /**
     * Tuple constructor that joins two tuples to create a new one
     * @param leftTuple tuple
     * @param rightTuple tuple
     */
    public Tuple(Tuple leftTuple, Tuple rightTuple) {
        tuple = new long[leftTuple.getTupleSize() + rightTuple.getTupleSize()];
        for (int i = 0; i < tuple.length; ++i) {
            if (i < leftTuple.getTupleSize())
                tuple[i] = leftTuple.getValuePos(i); //fill the new tuple with the attributes of the first tuple
            else tuple[i] = rightTuple.getValuePos(i - leftTuple.getTupleSize());  //fill the new tuple with the attributes of the first tuple
        }
        attrSchema = new ArrayList<>(); //create the schema of the new tuple
        attrSchema.addAll(leftTuple.getAttrSchema());
        attrSchema.addAll(rightTuple.getAttrSchema());
    }

    /**
     * Returns the value of the tuple in a position
     * @param i index of the tuple
     * @return value of the tuple in i
     */
    public long getValuePos(int i) {
        return tuple[i];
    }

    /**
     * Returns the tuple values
     * @return tuple values
     */
    public long[] getTuple() {
        return tuple;
    }

    /**
     * Returns the tuple size
     * @return tuple length
     */
    public int getTupleSize() {
        return tuple.length;
    }

    /**
     * Returns the tuple values in a string
     * @return tuple values in a string
     */
    @Override
    public String toString() {
        String t = "";
        for (int i = 0; i < tuple.length; ++i) {
            t += tuple[i];
            if (i != tuple.length - 1) t += ",";
        }
        return t;
    }

    /**
     * Appends the tuple to the end of the output file
     */
    public void dump() {
        try {
            String outputPath = DatabaseCatalog.getOutputFilePath();
            String t = toString() + "\n";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
            bw.write(t);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the tuple's schema
     * @return tuple's schema
     */
    public List<String> getAttrSchema() {
        return attrSchema;
    }

    /**
     * Returns the position of an attribute in the tuple
     * @param attr attribute of the tuple
     * @return position of an attribute in the tuple
     */
    public int getAttrPos(String attr) {
        if (attr.equals("*")) return -1;
        return attrSchema.indexOf(attr);
    }

}
