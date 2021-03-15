package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.util.Stack;

public class EvalExpression {
    private Tuple t;
    private Expression e;

    public EvalExpression (Tuple t, Expression e) {
        this.t = t;
        this.e = e;
    }

    public Object evaluate () throws JSQLParserException {
        final Stack<Object> stack = new Stack<>();
        //System.out.println("where " + e);
        Expression parseExpression = CCJSqlParserUtil.parseCondExpression(e.toString());
        ExpressionDeParser deparser = new ExpressionDeParser() {
            @Override
            public void visit(LongValue longValue) {
                super.visit(longValue);
                stack.push(longValue.getValue());
                //System.out.println("stack longvalue " + stack.toString());
                //System.out.println("long value " + longValue.toString());
            }

            @Override
            public void visit(Column column) {
                super.visit(column);
                int i = DatabaseCatalog.getAttrPos(column.toString());
                stack.push(t.getTuplePos(i));
                //System.out.println("stack collumn " + stack.toString());
                //System.out.println("column " + column.toString());
            }

            @Override
            public void visit(AndExpression andExpression) {
                super.visit(andExpression);
                //System.out.println("stack andexpression " + stack.toString());
                boolean facRight = Boolean.parseBoolean((stack.pop()).toString());
                boolean facLeft = Boolean.parseBoolean((stack.pop()).toString());

                stack.push(facLeft && facRight);

            }

            @Override
            public void visit(EqualsTo equalsTo) {
                super.visit(equalsTo);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());

                stack.push(facLeft == facRight);
            }

            @Override
            public void visit(GreaterThan greaterThan) {
                super.visit(greaterThan);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());

                stack.push(facLeft > facRight);
            }

            @Override
            public void visit(GreaterThanEquals greaterThanEquals) {
                super.visit(greaterThanEquals);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());

                stack.push(facLeft >= facRight);
            }

            @Override
            public void visit(MinorThan minorThan) {
                super.visit(minorThan);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());
                //System.out.println("minor than: fac1 " + facRight + ", fac2 " + facLeft);

                stack.push(facLeft < facRight);
            }

            @Override
            public void visit(MinorThanEquals minorThanEquals) {
                super.visit(minorThanEquals);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());

                stack.push(facLeft <= facRight);
            }

            @Override
            public void visit(NotEqualsTo notEqualsTo) {
                super.visit(notEqualsTo);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());

                stack.push(facLeft != facRight);
            }
        };

        StringBuilder b = new StringBuilder();
        deparser.setBuffer(b);
        parseExpression.accept(deparser);

        Object elem = stack.pop();
        //System.out.println(e + " = " + elem + "\n");
        return elem;
    }

}
