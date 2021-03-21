package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that represents the select statement and is in charge of generating the query plan and executes it
 */
public class SelectStatement {
    //select attributes
    private List<SelectItem> sel;
    //distinct
    private Distinct dist;
    //tables involved
    private List<String> schema;
    //where
    private Expression where;
    //only select conditions
    private static HashMap<String, Expression> selectionConds = new HashMap<>();
    //only join conditions
    private HashMap<String, Expression> joinConds = new HashMap<>();
    //orderby
    private List<OrderByElement> orderBy;

    /**
     * SelectStatement constructor
     * @param s query statement
     */
    public SelectStatement(Statement s) {
        Select select = (Select) s;
        PlainSelect body = (PlainSelect) select.getSelectBody();
        sel = body.getSelectItems();
        dist = body.getDistinct();
        orderBy = body.getOrderByElements();

        HashMap<String, String> aliases = new HashMap<>();
        schema = new ArrayList<>();

        FromItem from = body.getFromItem();
        Alias aliasFrom = from.getAlias();
        if (aliasFrom != null) { //if the query uses aliases
            String[] splitFrom = from.toString().split("\\s+");
            aliases.put(aliasFrom.getName(), splitFrom[0]); //store the corresponding table relation
            schema.add(aliasFrom.getName()); //add the alias to the schema
        }
        else
            schema.add(from.toString()); //add the table name to the schema

        List<Join> joins = body.getJoins();
        if (joins != null) { //if we have joins
            for (Join j:joins) {
                FromItem fromJoin = j.getRightItem(); //get the join table
                Alias aliasJoin = fromJoin.getAlias();
                if (aliasJoin != null) { //if the query uses aliases
                    String[] splitFromJoin = fromJoin.toString().split("\\s+");
                    aliases.put(aliasJoin.getName(), splitFromJoin[0]); //store the corresponding table relation
                    schema.add(aliasJoin.getName()); //add the alias to the schema
                }
                else
                    schema.add(fromJoin.toString()); //add the join table name to the schema
            }
        }

        DatabaseCatalog.setAliases(aliases); //set the aliases hashmap to the db catalog (may be empty!)

        //map every table to the select expressions (column op value or value op value) with its attributes
        HashMap<String, List<Expression>> auxSelectionConds = new HashMap<>();
        //map every table to the join expressions (column op column) with its attributes
        HashMap<String, List<Expression>> auxJoinConds = new HashMap<>();
		for (String t:schema) {
            auxSelectionConds.put(t, new ArrayList<>());
			auxJoinConds.put(t, new ArrayList<>());
		}

        where = body.getWhere();
        List<Expression> allExpressionsWhere = new ArrayList<>();
        if (where != null) allExpressionsWhere = splitExpressions(); //split the expressions of the where clause
        for (Expression e:allExpressionsWhere) {
            List<String> tablesInExpr = getTablesInExpression(e); //number of tables involved in expression e
            int tablePos = getLastPos(tablesInExpr);
            String tableName = schema.get(tablePos); //table to which the expression e will be added to
            if (tablesInExpr.size() < 2) //select expression
                auxSelectionConds.get(tableName).add(e);
            else auxJoinConds.get(tableName).add(e); //join expression
        }

        for (String t:schema) {
            //create an expression with all separate select expresssions involving table t and add it to selectionConds
            selectionConds.put(t, joinExpressions(auxSelectionConds.get(t)));
            //create an expression with all separate join expresssions involving table t and add it to joinConds
            joinConds.put(t, joinExpressions(auxJoinConds.get(t)));
        }
    }

    /**
     * Splits the where clause
     * @return list with every expression in where
     */
    private List<Expression> splitExpressions() {
        List<Expression> list = new ArrayList<>();
        Expression aux = where;
        while (aux instanceof AndExpression) { //for every and expression
            AndExpression ae = (AndExpression) aux;
            list.add(ae.getRightExpression()); //add the right expression
            aux = ae.getLeftExpression(); //update aux with the left part of the expression
        }
        list.add(aux); //add the last expression left

        return list;
    }

    /**
     * Returns a list with the tables involved in an expression
     * @param e expression
     * @return list with the tables involved in e
     */
    private List<String> getTablesInExpression(Expression e) {
        List<String> list = new ArrayList<>();
        String[] splitExp = e.toString().split("\\s+");
        if (splitExp[0].contains(".")) //if contain "." it is a column reference (else it is an integer)
            list.add(splitExp[0]);
        if (splitExp[2].contains(".")) //if contain "." it is a column reference (else it is an integer)
            list.add(splitExp[2]);
		if (list.size() == 2 && list.get(0).equals(list.get(1))) //if it is a self join remove one table
            list.remove(1);

		return list;
    }

    /**
     * Return an expression that joins the expressions in the list with an "and"
     * @param le list of expressions
     * @return expression containing all expressions in le joined with an "and"
     */
    private Expression joinExpressions(List<Expression> le) {
        if (le.size() == 0) return null; //if that table does not have any atribute in the where clause
        Expression e = le.get(0); //get the first expression
        for (int i = 1; i < le.size(); ++i)
            e = new AndExpression(e, le.get(i)); //create the and expression adding another expression to the left
        return e;
    }

    /**
     * Returns the position of the last table in the schema
     * @param tablesInExpr tables involved in an expression
     * @return position of the last table in the schema
     */
    private int getLastPos(List<String> tablesInExpr) {
        int pos = 0;
        for (String t:tablesInExpr) {
            String[] splitExpr = t.split("\\.");
            int tablePos = schema.indexOf(splitExpr[0]); //get the table position in the schema
            if (tablePos > pos) pos = tablePos; //if the position of the current table is higher, update pos
        }
        return pos;
    }

    /**
     * Generates the operator tree and executes the query
     */
    public void generateAndExecuteQueryPlan() {
        Operator root = new ScanOperator(schema.get(0)); //create a scan operator for the first table
        Expression where2 = selectionConds.get(schema.get(0));
        if (where2 != null) { //if that table is involved in an expression
            root = new SelectOperator((ScanOperator) root, where); //create a select operator for it
        }
        for (int i = 1; i < schema.size(); ++i) { //for the following tables (if any)
            String t = schema.get(i); //get the table name
            Operator root2 = new ScanOperator(t); //create a scan operator for this table
            Expression where3 = selectionConds.get(schema.get(i));
            if (where3 != null)  //if that table is involved in an expression
                root2 = new SelectOperator((ScanOperator) root2, where3); //create a select operator for it
            Expression whereJoin = joinConds.get(schema.get(i)); //get the join expression for that table (may be empty: cartesian product))
            root = new JoinOperator(root, root2, whereJoin); //create a join operator for the first/last operator and the new one
        }

        if (!sel.toString().equals("[*]")) //if we only want some attributes
            root = new ProjectOperator(sel, root); //create a project operator

        if (orderBy != null) { //if we have an order by clause in the query
            root = new SortOperator(root, orderBy); //create a sort operator
            if (dist != null) { //if we also have a distinct
                //create a duplicate elimination operator with the resulting sorted tuples
                root = new DuplicateEliminationOperator(root, ((SortOperator) root).getSortedTuples());
                //dump the resulting unique tuples
                root.dump(((DuplicateEliminationOperator) root).getUniqueTuples());
            }
            //if we don't have a distinct we dump the resulting sorted tuples
            else root.dump(((SortOperator) root).getSortedTuples());
        }
        else { //if we don't have an order by clause
            if (dist != null) {//if we do have a distinct
                root = new SortOperator(root, orderBy); //we need to sort the tuples anyway
                //create a duplicate elimination operator with the resulting sorted tuples
                root = new DuplicateEliminationOperator(root, ((SortOperator) root).getSortedTuples());
                //dump the resulting unique tuples
                root.dump(((DuplicateEliminationOperator) root).getUniqueTuples());
            }
            else root.dump(); //if no order by nor distinct clause, use the "basic" dump method to write the resulting tuples
        }
    }

    /**
     * Returns a select expression where table is involved
     * @param table table name
     * @return select expression where table is involved
     */
    public static Expression getSelectionCondsTable(String table) {
        return selectionConds.get(table);
    }
}
