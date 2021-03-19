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

public class SelectStatement {
    //select attributes
    private List<SelectItem> sel;
    //distinct
    private Distinct dist;
    //tables involved
    private List<String> schema;
    //where
    private Expression where;
    private static HashMap<String, Expression> selectionConds = new HashMap<>();
    private HashMap<String, Expression> joinConds = new HashMap<>();
    //orderby
    private List<OrderByElement> orderBy;
    //operator
    Operator op;

    public SelectStatement(Statement s) {
        Select select = (Select) s;
        PlainSelect body = (PlainSelect) select.getSelectBody();
        sel = body.getSelectItems();
        dist = body.getDistinct();
        FromItem from = body.getFromItem();
        List<Join> joins = body.getJoins();
        where = body.getWhere();
        List<Expression> allExpressionsWhere = new ArrayList<>();
        if (where != null) allExpressionsWhere = splitExpressions();
        orderBy = body.getOrderByElements();

        HashMap<String, String> aliases = new HashMap<>();
        schema = new ArrayList<>();

        Alias aliasFrom = from.getAlias();
        if (aliasFrom != null) {
            String[] splitFrom = from.toString().split("\\s+");
            aliases.put(aliasFrom.getName(), splitFrom[0]);
            schema.add(aliasFrom.getName());
        }
        else
            schema.add(from.toString());

        if (joins != null) {
            for (Join j:joins) {
                FromItem fromJoin = j.getRightItem();
                Alias aliasJoin = fromJoin.getAlias();
                if (aliasJoin != null) {
                    String[] splitFromJoin = fromJoin.toString().split("\\s+");
                    aliases.put(aliasJoin.getName(), splitFromJoin[0]);
                    schema.add(aliasJoin.getName());
                }
                else
                    schema.add(fromJoin.toString());
            }
        }

        DatabaseCatalog.setAliases(aliases);

        HashMap<String, List<Expression>> auxSelectionConds = new HashMap<>();
        HashMap<String, List<Expression>> auxJoinConds = new HashMap<>();
		for (String t:schema) {
            auxSelectionConds.put(t, new ArrayList<>());
			auxJoinConds.put(t, new ArrayList<>());
		}

        for (Expression e : allExpressionsWhere) {
            List<String> tables = getTablesInExpression(e);
            int tablePos = lastIdx(tables);
            String tableName = schema.get(tablePos);
            if (tables.size() == 1)
                auxSelectionConds.get(tableName).add(e);
            else auxJoinConds.get(tableName).add(e);
        }

        for (String t:schema) {
            selectionConds.put(t, joinExpressions(auxSelectionConds.get(t)));
            joinConds.put(t, joinExpressions(auxJoinConds.get(t)));
        }
    }

    private List<Expression> splitExpressions() {
        List<Expression> list = new ArrayList<>();
        Expression aux = where;
        while (aux instanceof AndExpression) {
            AndExpression ae = (AndExpression) aux;
            list.add(ae.getRightExpression());
            aux = ae.getLeftExpression();
        }
        list.add(aux);

        return list;
    }

    private List<String> getTablesInExpression(Expression e) {
        List<String> list = new ArrayList<>();
        String[] splitExp = e.toString().split("\\s+");
        if (splitExp[0].contains("."))
            list.add(splitExp[0]);
        if (splitExp[2].contains("."))
            list.add(splitExp[2]);
		if (list.size() == 2 && list.get(0).equals(list.get(1)))
            list.remove(1);

		return list;
    }

    private Expression joinExpressions(List<Expression> le) {
        if (le.size() == 0) return null; //if that table does not have any atribute in the where clause
        Expression e = le.get(0);
        for (int i = 1; i < le.size(); ++i)
            e = new AndExpression(e, le.get(i));
        return e;

    }

    private int lastIdx(List<String> tabs) { //todo provar el primer index + canviar
        if (tabs == null) return schema.size() - 1; //???
        int idx = 0;
        for (String tab : tabs) {
            String[] splitAttr = tab.split("\\.");
            int tableIndex = schema.indexOf(splitAttr[0]);
            if (tableIndex > idx) idx = tableIndex;
        }
        return idx;
    }

    public void generateOpTree() {
        Operator root = new ScanOperator(schema.get(0));
        //System.out.println("scan op " + schema.get(0) + "   id " + root.toString());
        Expression where2 = selectionConds.get(schema.get(0));
        if (where2 != null) {
            root = new SelectOperator((ScanOperator) root, where);
            //System.out.println("select op " + schema.get(0) + "   id " + root.toString());
        }
        for (int i = 1; i < schema.size(); ++i) {
            String t = schema.get(i);
            Operator root2 = new ScanOperator(t);
            //System.out.println("scan op " + t + "   id " + root2.toString());
            Expression where3 = selectionConds.get(schema.get(i));
            if (where3 != null) {
                root2 = new SelectOperator((ScanOperator) root2, where3);
                //System.out.println("select op " + schema.get(i)  + "   id " + root2.toString());
            }
            Expression whereJoin = joinConds.get(schema.get(i));
            //System.out.println("join op tables: " + schema.get(0) + " and " + schema.get(i));
            //System.out.println("where condition join: " + whereJoin);
            root = new JoinOperator(root, root2, whereJoin);
        }

        if (!sel.toString().equals("[*]"))
            root = new ProjectOperator(sel, root);

        if (orderBy != null) {
            List<Tuple> result = root.getQueryResult();
            SortOperator root2 = new SortOperator(result, orderBy);
            root2.dump(root2.getSortedTuples());
        }
        else
            root.dump();


    }

    public static Expression getSelectionCondsTable(String table) {
        return selectionConds.get(table);
    }
}
