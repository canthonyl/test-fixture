package concurrency;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class ThreadExecutor<T, R>  {

    private final int numIterations;
    private final ExecutionContext<T,R> executionContext;
    private final Function<T, R> methodUnderTest;
    private final List<R> results;
    private final T instance;
    private final AtomicMutatorContext<T,R> atomicContext;

    public ThreadExecutor(int numIterations, AtomicMutatorContext<T,R> atomicContext, SharedExecutionContext<T> sharedExecContext) {
        this.atomicContext = atomicContext;
        this.methodUnderTest = atomicContext.getResult();

        this.instance = sharedExecContext.instanceUnderTest();
        this.numIterations = numIterations;

        this.executionContext = new ExecutionContext<T,R>(sharedExecContext);
        this.results = this.executionContext.results;
    }

    public Callable<Collection<R>> getCallable() {
        return () -> {
            for (int i = 0; i < numIterations; i++) {
                executionContext.updateCurrentIteration(i);
                atomicContext.getBeforeOps().accept(executionContext);
                results.add(methodUnderTest.apply(instance));
                atomicContext.getAfterOps().accept(executionContext);
            }
            return results;
        };
    }

    public List<R> getResults(){
        return results;
    }

}
