package concurrency;

public class ParallelismContext {
    private int numThreads;
    private int numIterations;

    public ParallelismContext(int numThreads, int numIterations){
        this.numThreads = numThreads;
        this.numIterations = numIterations;
    }

    public int getNumThreads(){
        return numThreads;
    }

    public int getNumIterations(){
        return numIterations;
    }

    public long totalIteratons(){
        return numIterations * numThreads;
    }
}