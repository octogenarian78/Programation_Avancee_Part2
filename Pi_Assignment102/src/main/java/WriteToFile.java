import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteToFile {

  public static void write(long totalIterations, int numWorkers, long durationMs, double approxPi, double error, String fileName) throws IOException {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(fileName + ".csv", true));

      File file = new File(fileName + ".csv");
      if (file.length() == 0) {
        writer.write("totalIterations;numWorkers;Duree;Pi;Error\n");
      }

      String resultLine = String.format(
        "%d;%d;%d;%.30f;%.30f\n",
        totalIterations,
        numWorkers,
        durationMs,
        approxPi,
        error
      );


      writer.write(resultLine);

    } catch (IOException e) {
      System.err.println("Erreur lors de l'écriture dans le fichier : " + e.getMessage());
    } finally {
      try {
        if (writer != null) {
          writer.close();
        }
      } catch (IOException e) {
        System.err.println("Erreur lors de la fermeture du BufferedWriter : " + e.getMessage());
      }
    }
  }
}
