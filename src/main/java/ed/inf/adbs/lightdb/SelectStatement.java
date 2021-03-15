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
    //from
    private FromItem from;
    //joins
    private List<Join> joins;
    //aliases
    private HashMap<String, String> aliases;
    //where
    private Expression where;
    private List<Expression> allExpressionsWhere;
    private HashMap<String, List<Expression>> auxSelectionConds = new HashMap<>();
    private HashMap<String, List<Expression>> auxJoinConds = new HashMap<>();
    private HashMap<String, Expression> selectionConds = new HashMap<>();
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
        from = body.getFromItem();
        joins = body.getJoins();
        where = body.getWhere();
        allExpressionsWhere = splitExpressions();
        orderBy = body.getOrderByElements();

        aliases = new HashMap<>();
        schema = new ArrayList<>();

        Alias aliasFrom = from.getAlias();
        if (aliasFrom != null) {
            aliases.put(aliasFrom.getName(), from.toString()); //todo o aliasFrom a secas
            schema.add(aliasFrom.getName()); //todo o aliasFrom a secas
        }
        else {
            aliases.put(from.toString(), from.toString()); // o from.getname.tostrting
            schema.add(from.toString());
        }

        if (joins != null) {
            for (Join j:joins) {
                FromItem fromJoin = j.getRightItem();
                Alias aliasJoin = fromJoin.getAlias();
                if (aliasJoin != null) {
                    aliases.put(fromJoin.toString(), aliasJoin.getName());
                    schema.add(aliasJoin.getName());
                }
                else schema.add(fromJoin.toString());
            }
        }
        else {

        }
        DatabaseCatalog.setAliases(aliases);

		for (String t:schema) {
            auxSelectionConds.put(t, new ArrayList<>());
			auxJoinConds.put(t, new ArrayList<>());
		}

		for(Expression e:allExpressionsWhere) {
            List<String> tables = getTablesInExpression(e);
            int tablePos = lastIdx(tables);
            String tableName = schema.get(tablePos);
            if (tables.size() == 1) //s.a = 3
                auxSelectionConds.get(tableName).add(e);
            else auxJoinConds.get(tableName).add(e); //s.a = r.d
        }

        for (String t:schema) {
            selectionConds.put(t, joinExpressions(auxSelectionConds.get(t)));
            joinConds.put(t, joinExpressions(auxJoinConds.get(t)));
        }

        //generateOpTree(); fem crida al main
    }

    private List<Expression> splitExpressions() {
        List<Expression> list = new ArrayList<>();
        Expression aux = where;
        while (aux instanceof AndExpression) {
            AndExpression ae = (AndExpression) aux; //todo mirar si canvia el valor de where
            list.add(ae.getRightExpression());
            aux = ae.getLeftExpression(); //ae
        }
        list.add(aux); //todo que fa?

        return list;
    }

    private List<String> getTablesInExpression(Expression e) {
        List<String> list = new ArrayList<>();
		Expression right = ((AndExpression) e).getRightExpression();
		Expression left = ((AndExpression) e).getLeftExpression();

		if (right instanceof Column)
            list.add(((Column) right).getTable().toString());
		if (left instanceof Column)
            list.add(((Column) left).getTable().toString());
		if (list.size() == 2 && list.get(0).equals(list.get(1)))
            list.remove(1);

		return list;
    }

    private Expression joinExpressions(List<Expression> le) {
        Expression e = le.get(0);
        for (int i = 1; i < le.size(); ++i)
            e = new AndExpression(e, le.get(i));
        return e;

    }

    private int lastIdx(List<String> tabs) { //todo provar el primer index
        if (tabs == null) return schema.size() - 1; //???
        int idx = 0;
        for (String tab : tabs) {
            idx = Math.max(idx, schema.indexOf(tab));
        }
        return idx;
    }

    public void generateOpTree() {
        Operator root = new ScanOperator(schema.get(0));
        if (where != null) {
            root = new SelectOperator((ScanOperator) root, where);
            //System.out.println("empezamosss");
            //for (int i = 0; i < sel.size(); ++i)
              //  System.out.println("eo " + sel.get(i));
        }
        for (int i = 1; i < schema.size(); ++i) {
            Operator root2 = new ScanOperator(schema.get(i));
            Expression where2 = selectionConds.get(schema.get(i));
            if (where2 != null)
                root2 = new SelectOperator((ScanOperator) root2, where2);
            Expression whereJoin = joinConds.get(schema.get(i));
            root = new JoinOperator(root, root2, whereJoin);
        }

        root = new ProjectOperator(sel, root); //nomes si no es select *

        root.dump();


    }
}
