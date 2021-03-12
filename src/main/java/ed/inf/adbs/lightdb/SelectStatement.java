package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
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
        allExpressionsWhere = getAllExpressions();
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

        //froms -> schema
        //exps -> all epresionswhere


        generateOpTree();
    }

    private List<Expression> getAllExpressions() {
        List<Expression> list = new ArrayList<>();
        while (where instanceof AndExpression) {
            AndExpression ae = (AndExpression) where; //todo mirar si canvia el valor de where
            list.add(ae.getRightExpression());
            where = ae.getLeftExpression(); //ae
        }
        list.add(where); //todo que fa?

        return list;
    }

    private void generateOpTree() {
        Operator root = new ScanOperator(schema.get(0));
        if (where != null)
            root = new SelectOperator((ScanOperator) root, where);


        root.dump();


    }
}
