package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.List;

public abstract class Operator {

    public abstract Tuple getNextTuple();

	public abstract void reset();

	public void dump() {
        try {
            Tuple t = getNextTuple();
            while (t != null) {
                t.dump();
                t = getNextTuple();
            }
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
        }
    }
}
