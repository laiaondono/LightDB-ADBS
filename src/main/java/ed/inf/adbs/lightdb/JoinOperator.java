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
        leftOp = lo;
        rightOp = ro;
        this.e = e;
        leftTuple = leftOp.getNextTuple();
        rightTuple = rightOp.getNextTuple();
        System.out.println("ini left tuple " + leftTuple);
        System.out.println("ini right t " + rightTuple);
    }

    @Override
    public Tuple getNextTuple() { //todo canviar
        try {
            Tuple ret = null;

            while (leftTuple != null && rightTuple != null) {
                JoinEvalExpression ee = new JoinEvalExpression(leftTuple, rightTuple, e);
                //System.out.println("where join " + e);
                if (e == null) {
                    ret = new Tuple(leftTuple, rightTuple);
                    //System.out.println("e null");
                }
                else if (Boolean.parseBoolean(ee.evaluate().toString())) {
                    ret = new Tuple(leftTuple, rightTuple);
                    //System.out.println("condicio okk");
                }
                System.out.println("left tuple " + leftTuple);
                System.out.println("right t " + rightTuple);

                next();
                if (ret != null) {
                    System.out.println("join ret tuple " + ret);
                    return ret;
                }
            }
            return null;
        } catch (JSQLParserException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void reset() {

    }

    private void next() { //todo canviar
        if (leftTuple == null) return;

        if (rightTuple != null)
            rightTuple = rightOp.getNextTuple();

        if (rightTuple == null) {
            leftTuple = leftOp.getNextTuple();
            rightOp.reset();
            rightTuple = rightOp.getNextTuple();
        }
    }
}
