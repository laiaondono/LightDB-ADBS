package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuplicateEliminationOperator extends Operator {
    private List<Tuple> tuples;
    private Operator op;

    public DuplicateEliminationOperator(Operator op, List<Tuple> sortedTuples) {
        this.op = op;
        tuples = sortedTuples;
    }

    public List<Tuple> getUniqueTuples() {
        List<Tuple> uniqueTuples = new ArrayList<>();
        Tuple lastUniqueTuple = tuples.get(0);
        uniqueTuples.add(lastUniqueTuple);
        int iunique = 0;
        for (int i = 1; i < tuples.size(); ++i) {
            Tuple currentTuple = tuples.get(i);
            lastUniqueTuple = uniqueTuples.get(iunique);
            if (!Arrays.toString(currentTuple.getTuple()).equals(Arrays.toString(lastUniqueTuple.getTuple()))) {
                uniqueTuples.add(currentTuple);
                ++iunique;
            }
        }
        tuples = uniqueTuples;
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
