import java.io.FileWriter;
import java.io.IOException;

public class Log {
    private static FileWriter logWriter;
    private static final String lineSeparator = System.getProperty("line.separator");

    /**
     * initialise log
     */
    private static void init() {
        try {
            logWriter = new FileWriter("log.log");
        } catch (IOException e) {
            System.out.println("Can't create log file");
            e.printStackTrace();
        }
        report("Program start");
    }

    /**
     * writes to log message with error
     * @param report message to write to log
     */
    public static void report(String report) {
        if (logWriter == null) {
            init();
        }
        try {
            logWriter.write(report);
            logWriter.write(lineSeparator);
        } catch (IOException e) {
            System.out.println("Can't write to log file");
            e.printStackTrace();
        }
    }

    /**
     * closes log
     */
    public static void close() {
        try {
            report("Program end");
            if (logWriter != null) {
                logWriter.flush();
                logWriter.close();
            }
        } catch (IOException e) {
            System.out.println("Can't close log file");
            e.printStackTrace();
        }
    }
}
