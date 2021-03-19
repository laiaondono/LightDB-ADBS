package ed.inf.adbs.lightdb;

import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.*;

public class SortOperator extends Operator {
    private List<Tuple> tuples;
    private List<Integer> order = new ArrayList<>();

    public SortOperator(List<Tuple> tuples, List<OrderByElement> order) {
        this.tuples = tuples;
        List<String> tupleSchema = tuples.get(0).getAttrSchema(); //todo si tuples.size es 0
        for(OrderByElement elem:order) {
            this.order.add(tupleSchema.indexOf(elem.toString()));
        }
        Collections.sort(tuples, new TupleComparator());
        System.out.println("tuples sorted " + tuples);
    }

    private class TupleComparator implements Comparator<Tuple> {
        @Override
        public int compare(Tuple t1, Tuple t2) {
            for (Integer o:order) {
                long valt1 = t1.getValuePos(o);
                long valt2 = t2.getValuePos(o);
                int c = Long.compare(valt1, valt2);
                if (c != 0) return c;
            }
            return 0;
        }
    }

    public List<Tuple> getSortedTuples() {
        return tuples;
    }

    @Override
    public Tuple getNextTuple() {
        return null;
    }

    @Override
    public void reset() {

    }
}
