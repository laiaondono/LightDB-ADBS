package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.*;

/**
 * Class that represents the sort operator (used for the order by clause)
 */
public class SortOperator extends Operator {
    //resulting tuples that need to be sorted
    private List<Tuple> tuples;
    //list with the position (in the tuple) of the order by attributes
    private List<Integer> order = new ArrayList<>();
    //child operator
    private Operator op;

    /**
     * SortOperator constructor
     * @param op child operator
     * @param order list with the order by attributes
     */
    public SortOperator(Operator op, List<OrderByElement> order) {
        this.op = op;
        tuples = this.op.getQueryResult(); //get the resulting tuples of the query
        List<String> tupleSchema = new ArrayList<>();
        if (tuples.size() != 0)
            tupleSchema = tuples.get(0).getAttrSchema(); //get the schema of the resulting tuples
        if (order != null) //if we have an order by clause
            for(OrderByElement elem:order)
                this.order.add(tupleSchema.indexOf(elem.toString())); //add the position in the tuple to the order list
        else for(String elem:tupleSchema) //if we don't have an order by clause
                this.order.add(tupleSchema.indexOf(elem)); //the order is the same as the tuple has already

        Collections.sort(tuples, new TupleComparator()); //sort the tuples according to the TupleComparator
    }

    /**
     * Class that represents a TupleComparator to order the resulting tuples
     */
    private class TupleComparator implements Comparator<Tuple> {
        @Override
        public int compare(Tuple t1, Tuple t2) {
            for (Integer o:order) { //for each position in the order list
                long valt1 = t1.getValuePos(o); //get the value in that position for t1
                long valt2 = t2.getValuePos(o); //get the value in that position for t2
                int c = Long.compare(valt1, valt2); //compare both values
                if (c != 0) return c; //if they are not the same return it, else compare the next attribute in the order by clause
            }
            return 0;
        }
    }

    /**
     * Returns the sorted tuples
     * @return sorted tuples
     */
    public List<Tuple> getSortedTuples() {
        return tuples;
    }

    //Not used
    @Override
    public Tuple getNextTuple() {
        return null;
    }

    //Not used
    @Override
    public void reset() {

    }
}
