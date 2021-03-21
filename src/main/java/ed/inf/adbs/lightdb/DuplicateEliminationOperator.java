package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that represents the operator that eliminates duplicated tuples (used for distincts)
 */
public class DuplicateEliminationOperator extends Operator {
    //sorted tuples of which the duplicates will be eliminated
    private List<Tuple> tuples;
    //child operator
    private Operator op;

    /**
     * DuplicateEliminationOperator constructor
     * @param op child operator
     * @param sortedTuples sorted tuples
     */
    public DuplicateEliminationOperator(Operator op, List<Tuple> sortedTuples) {
        this.op = op;
        tuples = sortedTuples;
    }

    /**
     * Eliminated the duplicated tuples from the list of sorted tuples
     * @return list with sorted and no duplicated tuples
     */
    public List<Tuple> getUniqueTuples() {
        List<Tuple> uniqueTuples = new ArrayList<>();
        Tuple lastUniqueTuple = tuples.get(0); //we get the first tuple
        uniqueTuples.add(lastUniqueTuple); //and add it to the final list
        int iunique = 0;
        for (int i = 1; i < tuples.size(); ++i) { //for all other tuples
            Tuple currentTuple = tuples.get(i);
            lastUniqueTuple = uniqueTuples.get(iunique); //last unique tuple in the uniquetuples list
            //if the tuples are different (comparing only the long[] attribute, which has the values)
            if (!Arrays.toString(currentTuple.getTuple()).equals(Arrays.toString(lastUniqueTuple.getTuple()))) {
                uniqueTuples.add(currentTuple); //we add it to the uniquetuples list
                ++iunique; //increment the last position of the uniquetuples list
            }
        }
        tuples = uniqueTuples; //update the attribute
        return tuples;
    }

    //not used
    @Override
    public Tuple getNextTuple() {
        return null;
    }

    //not used
    @Override
    public void reset() {

    }
}
