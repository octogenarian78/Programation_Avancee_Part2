// Estimate the value of Pi using Monte-Carlo Method, using parallel program
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
class PiMonteCarlo {
    AtomicInteger nAtomSuccess;
    long nThrows;
    double value;
    class MonteCarlo implements Runnable {
        @Override
        public void run() {
            double x = Math.random();
            double y = Math.random();
            if (x * x + y * y > 1)
                nAtomSuccess.incrementAndGet();
        }
    }

    public AtomicInteger getnAtomSuccess() {
        return nAtomSuccess;
    }

    public PiMonteCarlo(long i) {
        this.nAtomSuccess = new AtomicInteger(0);
        this.nThrows = i;
        this.value = 0;
    }

    public double getPi() {
        int nProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newWorkStealingPool(nProcessors);
        for (int i = 1; i <= nThrows; i++) {
            Runnable worker = new MonteCarlo();
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        value = 4.0 - 4.0 * nAtomSuccess.get() / nThrows;
        return value;
    }
}
public class Assignment102 {
    public static void main(String[] args) throws IOException {
        long numIteration = Long.parseLong(args[0]);
        PiMonteCarlo PiVal = new PiMonteCarlo(numIteration);
        int numworker = Integer.parseInt(args[1]);
        long startTime = System.currentTimeMillis();
        double value = PiVal.getPi();
        long stopTime = System.currentTimeMillis();




        System.out.println("\nPi : " + value);
        System.out.println("Error: " + (value - Math.PI)+"\n");
//		System.out.println("Error: " + (value - Math.PI) / Math.PI * 100 + " %");
        System.out.println("Ntot : " + numIteration);
        System.out.println("Available processors: " + numworker);
        System.out.println("Time Duration (ms): " + (stopTime - startTime));

        WriteToFile writeToFile = new WriteToFile();
        writeToFile.write(numIteration,numworker,(stopTime - startTime),value,(value - Math.PI),args[2]);
    }
}
