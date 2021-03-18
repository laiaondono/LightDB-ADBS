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
        //System.out.println("where evalexpr " + e);
        Expression parseExpression = CCJSqlParserUtil.parseCondExpression(e.toString());
        ExpressionDeParser deparser = new ExpressionDeParser() {
            @Override
            public void visit(LongValue longValue) {
                super.visit(longValue);
                //System.out.println("stack long ini " + stack.toString());
                stack.push(longValue.getValue());
                //System.out.println("stack long fi " + stack.toString());
                //System.out.println("stack longvalue " + stack.toString());
                //System.out.println("long value " + longValue.toString());
            }

            @Override
            public void visit(Column column) {
                super.visit(column);
                //System.out.println("column " + column.toString());
                int i = DatabaseCatalog.getAttrPos(column.toString());
                //System.out.println("comparem atribut t " + column.toString() + " que te valor " + t.getValuePos(i));
                //System.out.println("stack collumn ini t " + stack.toString());
                stack.push(t.getValuePos(i));
                //System.out.println("stack collumn " + stack.toString());
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
                //System.out.println("stack equalsto " + stack.toString());
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
