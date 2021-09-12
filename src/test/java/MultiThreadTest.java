import concurrency.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MultiThreadTest {


    @Test
    public void test1(){
        List<ParallelismContext> parallelismContexts = List.of(new ParallelismContext(2, 100000), new ParallelismContext(4, 50000));

        Consumer<Counter> methodToTest = c -> c.incrementAndGet(1);

        AtomicMutatorContext<Counter, Long> context = new AtomicMutatorContext.SimpleMutatorContext<Counter, Long>()
                .supplier(ConcurrentCounter::new)
                .runnable(methodToTest)
                .actualResultFrom(Counter::get)
                .expectedResultFrom(ParallelismContext::totalIteratons)
                .build();

        ConcurrencyTestUtil.runConcurrencyTestWith(parallelismContexts, context);
    }

    @Test
    public void test2(){
       List<ParallelismContext> parallelismContexts = List.of(new ParallelismContext(2, 100000), new ParallelismContext(4, 50000));

       Consumer<ExecutionContext> advanceClock = ec -> ec.sharedContext.getClock() /* do stuff to clock */;
       Predicate<Collection<String>> allElementDistinct = result -> result.size() == result.stream().distinct().count();

       //all elements distinct
        AtomicMutatorContext<Counter, String> context = new AtomicMutatorContext.AccumulatingResultContext<Counter, String>()
            .supplier(ConcurrentCounter::new)
            .callable(c -> Long.valueOf(c.incrementAndGet(1)).toString())
            .beforeOperation(advanceClock)
            ///.verifyResult(allElementDistinct)
            .build();

        //can perform additional checking
        ConcurrencyTestUtil.runConcurrencyTestReturningResult(parallelismContexts, context);


    }
}

interface Counter {
    long incrementAndGet(long incAmount);
    long get();
}

class ConcurrentCounter implements Counter {

    private AtomicLong currentCount = new AtomicLong(0L);

    @Override
    public long incrementAndGet(long incAmount){
        return currentCount.addAndGet(incAmount);
    }

    @Override
    public long get() {
        return currentCount.get();
    }
}

class NonConcurrentCounter implements Counter {

    private Long currentCount = 0L;

    @Override
    public long incrementAndGet(long incAmount){
        return currentCount += incAmount;
    }

    @Override
    public long get() {
        return currentCount;
    }
}
