import java.io.*;
import java.net.*;
/**
 * Worker is a server. It computes PI by Monte Carlo method and sends 
 * the result to Master.
 */
public class WorkerSocket {
    static int port = 25545; //default port
    private static boolean isRunning = true;
    
    /**
     * compute PI locally by MC and sends the number of points 
     * inside the disk to Master. 
     */
    public static void main(String[] args) throws Exception {

	if (!("".equals(args[0]))) port=Integer.parseInt(args[0]);
	System.out.println(port);
        ServerSocket s = new ServerSocket(port);
        System.out.println("Server started on port " + port);
        Socket soc = s.accept();
	
        // BufferedReader bRead for reading message from Master
        BufferedReader bRead = new BufferedReader(new InputStreamReader(soc.getInputStream()));

        // PrintWriter pWrite for writing message to Master
        PrintWriter pWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())), true);
	String str;
        while (isRunning) {
	    str = bRead.readLine();          // read message from Master
	    if (!(str.equals("END"))){
		System.out.println("Server receives totalCount = " +  str);
		
		// compute
		System.out.println("TODO : compute Monte Carlo and send total");

	        pWrite.println(str);         // send number of points in quarter of disk
	    }else{
		isRunning=false;
	    }	    
        }
        bRead.close();
        pWrite.close();
        soc.close();
   }
}