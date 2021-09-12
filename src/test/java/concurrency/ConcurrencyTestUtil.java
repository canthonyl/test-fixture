package concurrency;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrencyTestUtil {

    public static <T, R> void runConcurrencyTestWith(List<ParallelismContext> allConcurContext, AtomicMutatorContext<T, R> context) {
        for(ParallelismContext cc : allConcurContext) {
            try {
                R expected = context.evalExpectedReslt(cc);
                T instance = context.create();

                ExecutorService es = Executors.newFixedThreadPool(cc.getNumThreads());
                for(int t=0; t<cc.getNumThreads(); t++) {
                    es.submit(() -> {
                        for (int i = 0; i < cc.getNumIterations(); i++) {
                            context.actionOn(instance);
                        }
                    });
                }

                es.shutdown();
                es.awaitTermination(1, TimeUnit.MINUTES);

                R actual = context.evalActualResult(instance);
                assertEquals(expected, actual);

            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    private static <T, R> Collection<R> getResult(ParallelismContext parallelContext, AtomicMutatorContext<T, R> context) {
        List<R> allResults = new LinkedList<>();
        try {
            T instance = context.create();
            Clock clock = Clock.systemDefaultZone();
            SharedExecutionContext<T> sharedExecutionContext = new SharedExecutionContext<>(instance, clock, parallelContext);

            List<Callable<Collection<R>>> callables = new LinkedList<>();
            for (int t = 0; t < parallelContext.getNumThreads(); t++) {
                callables.add(new ThreadExecutor<>(parallelContext.getNumIterations(), context, sharedExecutionContext).getCallable());
            }

            ExecutorService es = Executors.newFixedThreadPool(sharedExecutionContext.numThreads());
            List<Future<Collection<R>>> results = es.invokeAll(callables);
            for(Future<Collection<R>> f : results) {
                allResults.addAll(f.get());
            }

            es.shutdown();
            es.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return allResults;
    }

    public static <T, R> Map<ParallelismContext, Collection<R>> runConcurrencyTestReturningResult(List<ParallelismContext> allConcurContext, AtomicMutatorContext<T, R> context) {
        Map<ParallelismContext, Collection<R>> results = new LinkedHashMap<>();


        for(ParallelismContext multiThreaded : allConcurContext) {
            ParallelismContext singleThreaded = new ParallelismContext(1, multiThreaded.getNumIterations() * multiThreaded.getNumThreads());

            Collection<R> singleThreadedResult = getResult(singleThreaded, context);
            Collection<R> multiThreadedResult = getResult(multiThreaded, context);

            assertEquals(singleThreadedResult.size(), multiThreadedResult.size());
        }

        return results;
    }
}
