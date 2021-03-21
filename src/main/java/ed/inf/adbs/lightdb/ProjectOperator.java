package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a project operator (used when we have a select clause different to *)
 */
public class ProjectOperator extends Operator {
    //child operator
    private Operator op;
    //attributes wanted in the select clause
    private List<SelectItem> attrs;

    /**
     * ProjectOperator constructor
     * @param attrProj attributes wanted in the select clause
     * @param o child operator
     */
    public ProjectOperator(List<SelectItem> attrProj, Operator o) {
        attrs = attrProj;
        op = o;
    }

    /**
     * Returns the next tuple with only the attributes wanted in the select clause
     * @return tuple with the desired attributes
     */
    @Override
    public Tuple getNextTuple() {
        Tuple t = op.getNextTuple();
        List<Integer> attrsTuple = new ArrayList<>();
        List<String> tupleSchema = new ArrayList<>();
        for (SelectItem attr:attrs) {
            attrsTuple.add(t.getAttrPos(attr.toString())); //store the position of the attributes wanted in the tuple
            tupleSchema.add(attr.toString()); //store the new tuple's schema
        }
        long[] newt;

        if (attrsTuple.get(0).toString().equals("-1")) //case of select *
            newt = t.getTuple(); //new tuple's values are the same
        else {
            newt = new long[attrs.size()];
            for (int i = 0; i < attrsTuple.size(); ++i) //for every attribute in the position array
                newt[i] = t.getValuePos(attrsTuple.get(i)); //add the value of the tuple in that position
        }

        return new Tuple(newt, tupleSchema);
    }

    /**
     * Calls the child operator's reset method
     */
    @Override
    public void reset() {
        op.reset();
    }
}
