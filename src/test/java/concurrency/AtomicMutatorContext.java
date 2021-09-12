package concurrency;

import java.util.Collection;
import java.util.function.*;

public class AtomicMutatorContext<T,R>{
    private Supplier<T> supplier;
    private Consumer<T> action;
    private Function<T, R> result;
    private Predicate<Collection<R>> verifier;
    private Function<ParallelismContext, R> expectedResult;
    private Function<ParallelismContext, Collection<R>> expectedAllResult;
    private Consumer<ExecutionContext> beforeOps = ec -> {};
    private Consumer<ExecutionContext> afterOps = ec -> {};

    public T create(){
        return supplier.get();
    }

    public void actionOn(T instance){
        action.accept(instance);
    }

    public R evalActualResult(T instance){
        return result.apply(instance);
    }

    public R evalExpectedReslt(ParallelismContext cc) {
        return expectedResult.apply(cc);
    }

    public Function<T, R> getResult(){ return result;}

    public boolean verifyResult(Collection<R> instance){
        return this.verifier.test(instance);
    }

    public Consumer<ExecutionContext> getBeforeOps() {
        return beforeOps;
    }

    public Consumer<ExecutionContext> getAfterOps() {
        return afterOps;
    }

    public static class SimpleMutatorContext<T,R>{
        private AtomicMutatorContext<T,R> context = new AtomicMutatorContext();

        public SimpleMutatorContext<T,R> runnable(Consumer<T> methodUnderTest) {
            context.action = methodUnderTest;
            return this;
        }

        public SimpleMutatorContext<T,R> supplier(Supplier<T> supplier) {
            context.supplier = supplier;
            return this;
        }

        public SimpleMutatorContext<T,R> actualResultFrom(Function<T, R> resultRetriever) {
            context.result = resultRetriever;
            return this;
        }

        public SimpleMutatorContext<T,R> expectedResultFrom(Function<ParallelismContext, R> expectedResult) {
            context.expectedResult = expectedResult;
            return this;
        }

        public AtomicMutatorContext<T,R> build(){
            return context;
        }

    }

    public static class AccumulatingResultContext<T, R>{
        private AtomicMutatorContext context = new AtomicMutatorContext();


        public AccumulatingResultContext<T,R> supplier(Supplier<T> supplier) {
            context.supplier = supplier;
            return this;
        }

        public AccumulatingResultContext<T,R> callable(Function<T, R> result){
            context.result = result;
            return this;
        }

        public AccumulatingResultContext<T,R> verifyResult(Predicate<Collection<R>> verifier) {
            context.verifier = verifier;
            return this;
        }

        public AccumulatingResultContext<T,R> beforeOperation(Consumer<ExecutionContext> ops) {
            context.beforeOps = ops;
            return this;
        }

        public AccumulatingResultContext<T,R> afterOperation(Consumer<ExecutionContext> ops) {
            context.afterOps = ops;
            return this;
        }

        public AtomicMutatorContext<T,R> build(){
            return context;
        }

        public AccumulatingResultContext<T, R> expectedResult(Function<ParallelismContext, Collection<R>> evalResult) {
            context.expectedAllResult = evalResult;
            return this;
        }
    }

}
