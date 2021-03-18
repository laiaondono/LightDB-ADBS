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

public class SelectEvalExpression {
    private Tuple t;
    private Expression e;

    public SelectEvalExpression(Tuple t, Expression e) {
        this.t = t;
        this.e = e;
    }

    public Object evaluate () throws JSQLParserException {
        final Stack<Object> stack = new Stack<>();
        Expression parseExpression = CCJSqlParserUtil.parseCondExpression(e.toString());
        ExpressionDeParser deparser = new ExpressionDeParser() {
            @Override
            public void visit(LongValue longValue) {
                super.visit(longValue);
                stack.push(longValue.getValue());
            }

            @Override
            public void visit(Column column) {
                super.visit(column);
                int i = DatabaseCatalog.getAttrPos(column.toString());
                stack.push(t.getValuePos(i));
            }

            @Override
            public void visit(AndExpression andExpression) {
                super.visit(andExpression);
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

        return stack.pop();
    }

}
