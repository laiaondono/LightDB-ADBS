package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.List;

public class ProjectOperator extends Operator {

    private Operator op;
    private List<SelectItem> attrs;

    public ProjectOperator(List<SelectItem> attrProj, Operator o) {
        attrs = attrProj;
        op = o;
        //if (op instanceof SelectOperator) System.out.println("es selectoperator");
        //if (op instanceof JoinOperator) System.out.println("es joinoperator");
    }

    @Override
    public Tuple getNextTuple() {
        Tuple t = op.getNextTuple();
        List<Integer> attrsTuple = new ArrayList<>();
        List<String> tupleSchema = new ArrayList<>();
        //System.out.println("tupla " + t);
        for (SelectItem attr:attrs) {
            attrsTuple.add(t.getAttrPos(attr.toString()));
            tupleSchema.add(attr.toString());
        }
        //System.out.println("attrstuple " + attrsTuple);

        //System.out.println("attrtuple " + attrsTuple);
        long[] newt;
        //System.out.println("* o attrs: " + attrsTuple.toString());

        if (attrsTuple.get(0).toString().equals("-1"))
            newt = t.getTuple();
        else {
            newt = new long[attrs.size()];
            for (int i = 0; i < attrsTuple.size(); ++i)
                newt[i] = t.getValuePos(attrsTuple.get(i));
                //System.out.println("new[i] = " + newt[i]);
        }

        return new Tuple(newt, tupleSchema);
    }

    @Override
    public void reset() {

    }
}
