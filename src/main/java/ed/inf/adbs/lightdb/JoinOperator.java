package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;

/**
 * Class that represents a join operator
 */
public class JoinOperator extends Operator {
    //operators of the tables to join
    private Operator leftOp;
    private Operator rightOp;
    //join expression
    private Expression e;
    //tuples of each table
    private Tuple leftTuple;
    private Tuple rightTuple;

    /**
     * JoinOperator constructor
     * @param lo outer table operator
     * @param ro inner table operator
     * @param e join expression
     */
    public JoinOperator (Operator lo, Operator ro, Expression e) {
        leftOp = lo;
        rightOp = ro;
        this.e = e;
        //we get the first tuple of each table ("initialise the join")
        leftTuple = leftOp.getNextTuple();
        rightTuple = rightOp.getNextTuple();
    }

    /**
     * Returns the next tuple resulting of the join condition (simple (tuple) nested loop join)
     * @return next tuple resulting of the join condition
     */
    @Override
    public Tuple getNextTuple() {
        try {
            Tuple t = null;
            while (leftTuple != null) { //while we still have tuples from the outer table to explore
                JoinEvalExpression jee = new JoinEvalExpression(leftTuple, rightTuple, e);
                //if there is no expression (cartesian product) or the tuples fulfill the join condition
                if (e == null || Boolean.parseBoolean(jee.evaluate().toString()))
                    t = new Tuple(leftTuple, rightTuple); //create a new tuple that has all the attributes of both tuples

                //update tuples
                if (rightTuple != null) //get next tuple for the inner table
                    rightTuple = rightOp.getNextTuple();

                if (rightTuple == null) { //if it was the last one
                    rightOp.reset(); //reset the inner operator (to start the inner table scan again)
                    rightTuple = rightOp.getNextTuple(); //get the first tuple of the inner table
                    leftTuple = leftOp.getNextTuple(); //get the next outer table's tuple
                }
                if (t != null) //after updating the tuples, we check if we got a tuple that fulfills the join condition
                    return t; //if so we return it
            }
        } catch (JSQLParserException ex) {
            ex.printStackTrace();
        }
        return null; //if no tuple fulfills the condition we return nulll
    }

    //Not used
    @Override
    public void reset() {

    }
}
