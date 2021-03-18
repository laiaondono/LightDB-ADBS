package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;

public class SelectOperator extends Operator {

    private ScanOperator scanOp;
    private Expression where;

    public SelectOperator(ScanOperator so, Expression e) {
        scanOp = so;
        where = e;
    }

    @Override
    public Tuple getNextTuple() {
        try {
            Tuple t = scanOp.getNextTuple();
            while (t != null) {
                //System.out.println("tuplle a evaluar " + t.toString());
                Expression whereSelectOnly = SelectStatement.getSelectionCondsTable(scanOp.getTable());
                SelectEvalExpression see = new SelectEvalExpression(t, whereSelectOnly);
                //System.out.println("where select " + whereSelectOnly);
                if (Boolean.parseBoolean(see.evaluate().toString())) {
                    //System.out.println("good");
                    return t;
                }
                t = scanOp.getNextTuple();
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void reset() {
        scanOp.reset();
    }
}
