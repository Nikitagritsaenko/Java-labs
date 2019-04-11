import java.io.*;
import java.util.Map;

public class MyWriter {
    private DataOutputStream outputDOS;
    private FileWriter writer;
    private static final String lineSeparator = System.getProperty("line.separator");

    /**
     * Creates wrapper for writer
     * @param fileName name of file in which we will put data
     * @throws IOException
     */
    public MyWriter(String fileName) throws IOException {
        OpenStream(fileName);
        InitFileWriter(fileName);
    }
    public MyWriter(DataOutputStream outputDOS) {
        this.outputDOS = outputDOS;
    }

    /**
     * opens output stream
     *
     * @param fileName name of file to open
     */
    private void OpenStream(String fileName) throws IOException{
        try {
            outputDOS = new DataOutputStream(new FileOutputStream(fileName));
        } catch (IOException e) {
            Log.report("Can't open file " + fileName);
            System.out.println("Can't open file " + fileName);
            throw new IOException();
        }
    }

    /**
     * closes output stream
     */
    public void CloseStream() {
        try {
            if (outputDOS != null)
                outputDOS.close();
        } catch (IOException e) {
            Log.report("Output stream can't be closed.");
            System.out.println("Output stream can't be closed.");
        }
    }


    /**
     * closes file writer
     */
    public void CloseFileWriter() {
        try {
            if (writer != null)
                writer.close();
        } catch (IOException e) {
            Log.report("Can't close FileWriter");
            System.out.println("Can't close FileWriter");
        }
    }

    /**
     * writes buffer to output file
     *
     * @param data buffer to write to file
     */
    public void writeBuff(byte[] data) {
        try {
            outputDOS.write(data);
        } catch (IOException e) {
            Log.report("Writing output file error.");
            System.out.println("Writing output file error.");
        }
    }

    /**
     * initialises file writer
     *
     * @param outputFile name of file to open
     */
    private void InitFileWriter(String outputFile) {
        try {
            writer = new FileWriter(outputFile);
        } catch (IOException ex) {
            Log.report("Can't init FileWriter");
            System.out.println("Can't init FileWriter");
        }
    }

    /**
     * Writes in file pairs consists of all bytes and their frequencies in source file
     * @param freqMap map of frequencies of every byte
     * @throws IOException
     */
    public void WriteFrequency(Map<Byte, Segment> freqMap) throws IOException {
        for (Map.Entry<Byte, Segment> elem : freqMap.entrySet()) {
            Segment segment = elem.getValue();
            double low = segment.low;
            double high = segment.high;
            writer.write(elem.getKey() + ":" + low + " " + high + lineSeparator);
        }

    }

    /**
     * closes output stream
     */
    public void closeWriter() {
        CloseStream();
        CloseFileWriter();
    }

    /**
     * gets output stream
     * @return output stream
     */
    public DataOutputStream getStream() {
        return outputDOS;
    }
}
