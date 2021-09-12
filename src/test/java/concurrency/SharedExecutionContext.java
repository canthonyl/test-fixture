package concurrency;

import java.time.Clock;
import java.util.concurrent.CountDownLatch;

public class SharedExecutionContext<T> {

    private final Clock clock;
    private final ParallelismContext parallelismContext;
    private final T instanceUnderTest;
    private final CountDownLatch countDownLatch;

    public SharedExecutionContext(T instanceUnderTest, Clock clock, ParallelismContext context){
        this.instanceUnderTest = instanceUnderTest;
        this.clock = clock;
        this.parallelismContext = context;
        this.countDownLatch = new CountDownLatch(context.getNumIterations() * context.getNumThreads());
    }

    public Clock getClock(){
        return clock;
    }
    public int numIteration(){ return parallelismContext.getNumIterations();}
    public int numThreads(){ return parallelismContext.getNumThreads();}
    public T instanceUnderTest(){ return instanceUnderTest; }

    void countDown(){
        countDownLatch.countDown();
    }

    void waitForCompletion(){
        try {
            countDownLatch.await();
        } catch (InterruptedException ie){
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        }
    }


}
