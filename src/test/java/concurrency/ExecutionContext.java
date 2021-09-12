package concurrency;

import java.util.LinkedList;
import java.util.List;

public class ExecutionContext<T,R> {

    public final SharedExecutionContext<T> sharedContext;

    final List<R> results;
    int currentIteration;

    public ExecutionContext(SharedExecutionContext<T> sharedContext){
        this.sharedContext = sharedContext;
        this.results = new LinkedList<>();
    }

    public int currentIteration(){ return currentIteration; }

    T instance(){
        return sharedContext.instanceUnderTest();
    }

    void updateCurrentIteration(int i){
        currentIteration = i;
    }

    void addResult(R result){
        this.results.add(result);
    }


}
