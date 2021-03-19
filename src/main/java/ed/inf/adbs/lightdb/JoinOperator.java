package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Operator {
    private Operator leftOp;
    private Operator rightOp;
    private Expression e;
    private Tuple leftTuple;
    private Tuple rightTuple;

    public JoinOperator (Operator lo, Operator ro, Expression e) {
        leftOp = lo; //outer table
        rightOp = ro;
        this.e = e;
        leftTuple = leftOp.getNextTuple();
        rightTuple = rightOp.getNextTuple();
    }

    @Override
    public Tuple getNextTuple() {
        try {
            Tuple t = null;
            while (leftTuple != null) {
                JoinEvalExpression jee = new JoinEvalExpression(leftTuple, rightTuple, e);
                if (e == null || Boolean.parseBoolean(jee.evaluate().toString()))
                    t = new Tuple(leftTuple, rightTuple);

                if (rightTuple != null)
                    rightTuple = rightOp.getNextTuple();

                if (rightTuple == null) {
                    rightOp.reset();
                    leftTuple = leftOp.getNextTuple();
                    rightTuple = rightOp.getNextTuple();
                }
                if (t != null)
                    return t;
            }
        } catch (JSQLParserException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void reset() {

    }
}
