package TSP.TSP_Sotto_Problema;

import java.util.Collections;
import java.util.List;

public class Exception_Unfeasible_Problem extends Exception {
    public final List<Integer> chiaviNodiInvalidi;

    public Exception_Unfeasible_Problem(List<Integer> oneWayNodesKeys) {
        this.chiaviNodiInvalidi = Collections.unmodifiableList(oneWayNodesKeys);
    }
}
