import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Approximates PI using the Monte Carlo method.  Demonstrates
 * use of Callables, Futures, and thread pools.
 */
public class Pi
{
    public static void main(String[] args) throws Exception
    {
        long total=0;
        long numIterations = Long.parseLong(args[0]);
        int numWorkers = Integer.parseInt(args[1]);
        total = new Master().doRun(numIterations, numWorkers,args[2]);
        System.out.println("total from Master = " + total);
    }
}

/**
 * Creates workers to run the Monte Carlo simulation
 * and aggregates the results.
 */
class Master {
    public long doRun(long totalCount, int numWorkers, String filename) throws InterruptedException, ExecutionException, IOException {

        long startTime = System.currentTimeMillis();

        // Create a collection of tasks
        List<Callable<Long>> tasks = new ArrayList<Callable<Long>>();
        for (int i = 0; i < numWorkers; ++i)
        {
            tasks.add(new Worker(totalCount));
        }

        // Run them and receive a collection of Futures
        ExecutorService exec = Executors.newFixedThreadPool(numWorkers);
        List<Future<Long>> results = exec.invokeAll(tasks);
        long total = 0;

        // Assemble the results.
        for (Future<Long> f : results)
        {
            // Call to get() is an implicit barrier.  This will block
            // until result from corresponding worker is ready.
            total += f.get();
        }
        double pi = 4.0 * total / totalCount / numWorkers;

        long stopTime = System.currentTimeMillis();

        System.out.println("\nPi : " + pi );
        System.out.println("Error: " + (Math.abs((pi - Math.PI)) / Math.PI) +"\n");

        System.out.println("Ntot: " + totalCount*numWorkers);
        System.out.println("Available processors: " + numWorkers);
        System.out.println("Time Duration (ms): " + (stopTime - startTime));

        //System.out.println( (Math.abs((pi - Math.PI)) / Math.PI) +" "+ totalCount*numWorkers +" "+ numWorkers +" "+ (stopTime - startTime));
        WriteToFile writeToFile = new WriteToFile();
        writeToFile.write(totalCount*numWorkers,numWorkers,(stopTime - startTime),pi,(Math.abs((pi - Math.PI))),filename);
        exec.shutdown();
        return total;
    }
}

/**
 * Task for running the Monte Carlo simulation.
 */
class Worker implements Callable<Long>
{
    private long numIterations;
    public Worker(long num)
    {
        this.numIterations = num;
    }

    @Override
    public Long call()
    {
        long circleCount = 0;
        Random prng = new Random ();
        for (int j = 0; j < numIterations; j++)
        {
            double x = prng.nextDouble();
            double y = prng.nextDouble();
            if ((x * x + y * y) < 1)  ++circleCount;
        }
        return circleCount;
    }
}
