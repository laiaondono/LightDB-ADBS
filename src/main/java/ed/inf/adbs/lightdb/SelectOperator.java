package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;

/**
 * Class that represents the select operator
 */
public class SelectOperator extends Operator {
    //child operator (scan)
    private ScanOperator scanOp;
    //expression to evaluate
    private Expression where;

    /**
     * SelectOperator constructor
     * @param so scan operator (child)
     * @param e expression to evaluate
     */
    public SelectOperator(ScanOperator so, Expression e) {
        scanOp = so;
        where = e;
    }

    /**
     * Returns the next tuple that fulfills the where expression
     * @return next tuple that fulfills the where expression
     */
    @Override
    public Tuple getNextTuple() {
        try {
            Tuple t = scanOp.getNextTuple(); //get next tuple
            while (t != null) {
                //get the selection conditions in the where clause
                Expression whereSelectOnly = SelectStatement.getSelectionCondsTable(scanOp.getTable());
                SelectEvalExpression see = new SelectEvalExpression(t, whereSelectOnly);
                if (Boolean.parseBoolean(see.evaluate().toString())) //if the tuple fulfills those conditions, return it
                    return t;
                t = scanOp.getNextTuple(); //else get the next tuple
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calls the child operator's reset method
     */
    @Override
    public void reset() {
        scanOp.reset();
    }
}
