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

/**
 * Class used to evaluate if 2 tuples fulfill the conditions of a join expression
 */
public class JoinEvalExpression {
    //tuples
    private Tuple t;
    private Expression e;
    //expression to evaluate
    private Tuple t2;

    /**
     * JoinEvalExpression constructor
     * @param leftTuple tuple
     * @param rightTuple tuple
     * @param e expression to evaluate
     */
    public JoinEvalExpression(Tuple leftTuple, Tuple rightTuple, Expression e) {
        t = leftTuple;
        t2 = rightTuple;
        this.e = e;
    }

    /**
     * Evaluates the join expression of two tuples
     * @return an Object (boolean) that is true if the tuples fulfill the join expression, else false
     * @throws JSQLParserException
     */
    public Object evaluate() throws JSQLParserException {
        //stack that will contain the values of the attributes in the expression and its result
        final Stack<Object> stack = new Stack<>();
        Expression parseExpression = CCJSqlParserUtil.parseCondExpression(e.toString());
        ExpressionDeParser deparser = new ExpressionDeParser() {
            @Override
            public void visit(LongValue longValue) { //pushes the long value to the stack
                super.visit(longValue);
                stack.push(longValue.getValue());
            }

            @Override
            public void visit(Column column) { //pushes the column value to the stack
                super.visit(column);
                int i = DatabaseCatalog.getAttrPos(column.toString()); //get the column position in the table schema
                if (t.getAttrSchema().contains(column.toString())) //if it is an attribute of the first tuple
                    stack.push(t.getValuePos(i)); //push the value in that position of the first tuple
                else
                    stack.push(t2.getValuePos(i)); //else push the value of the second tuple
            }

            @Override
            public void visit(AndExpression andExpression) { //pushes the result of an and expression to the stack
                super.visit(andExpression);
                boolean facRight = Boolean.parseBoolean((stack.pop()).toString());
                boolean facLeft = Boolean.parseBoolean((stack.pop()).toString());
                stack.push(facLeft && facRight);

            }

            @Override
            public void visit(EqualsTo equalsTo) { //pushes the result of an equalsTo comparison to the stack
                super.visit(equalsTo);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());
                stack.push(facLeft == facRight);
            }

            @Override
            public void visit(GreaterThan greaterThan) { //pushes the result of a greaterThan comparison to the stack
                super.visit(greaterThan);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());
                stack.push(facLeft > facRight);
            }

            @Override
            public void visit(GreaterThanEquals greaterThanEquals) { //pushes the result of a greaterThanEquals comparison to the stack
                super.visit(greaterThanEquals);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());
                stack.push(facLeft >= facRight);
            }

            @Override
            public void visit(MinorThan minorThan) { //pushes the result of a minorThan comparison to the stack
                super.visit(minorThan);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());
                stack.push(facLeft < facRight);
            }

            @Override
            public void visit(MinorThanEquals minorThanEquals) { //pushes the result of a minorThanEquals comparison to the stack
                super.visit(minorThanEquals);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());
                stack.push(facLeft <= facRight);
            }

            @Override
            public void visit(NotEqualsTo notEqualsTo) { //pushes the result of a notEqualsTo comparison to the stack
                super.visit(notEqualsTo);
                long facRight = new Long(stack.pop().toString());
                long facLeft = new Long(stack.pop().toString());
                stack.push(facLeft != facRight);
            }
        };

        StringBuilder b = new StringBuilder();
        deparser.setBuffer(b);
        parseExpression.accept(deparser); //evalute the expression according to the ExpressionDeParser

        return stack.pop(); //return the final result (bool)
    }

}
