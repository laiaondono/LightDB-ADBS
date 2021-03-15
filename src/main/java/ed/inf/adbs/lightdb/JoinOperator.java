package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Expression;

import java.util.List;

public class JoinOperator extends Operator {
    private Operator leftOp;
    private Operator rightOp;
    private Expression e;
    //todo SI NO FUNCIONA, prrovar afegir atributs tuples
        //keep track of the current positions in the outer and inner tables,
        // so that you can resume join processing on subsequent GetNextTuple() calls @99

    public JoinOperator (Operator lo, Operator ro, Expression e) {
        //super(lo, ro); todo ???
        leftOp = lo;
        rightOp = ro;
        this.e = e;
    }

    @Override
    public Tuple getNextTuple() {
        Tuple leftTuple = leftOp.getNextTuple();
        while (leftTuple != null) {
            Tuple rightTuple = rightOp.getNextTuple();
            if (rightTuple == null) {
                rightOp.reset();
                leftTuple = leftOp.getNextTuple();
            }
            else {
                //fer join o producte cartesia

            }
        }

        return null;
    }

    @Override
    public void reset() {

    }
}
