import com.oracle.tools.packager.IOUtils;

import java.io.*;
import java.util.Arrays;

public class MyReader {
    private DataInputStream inputDIS;
    private BufferedReader inputBR;

    /**
     * Creates wrapper for reader
     * @param filename name of file from which we will read data
     * @throws IOException
     */
    public MyReader(String filename) throws IOException {
        OpenStream(filename);
        InitBuffReader(filename);
    }
    public MyReader(DataInputStream inputDIS) {
        this.inputDIS = inputDIS;
    }

    /**
     * reads buffer from input stream
     *
     * @param bufferSize size of buffer
     * @return buffer that was read
     */
    public final byte[] readBuff(int bufferSize) {
        byte[] buff = new byte[bufferSize];
        int i;
        for (i = 0; i < bufferSize; i++) {
            byte elem;
            try {
                elem = inputDIS.readByte();
            }
            catch (IOException e) {
                break;
            }
            buff[i] = elem;
        }
        if (i == 0) {
            return null;
        }
        byte[] dataArray;
        dataArray = Arrays.copyOf(buff, i);
        return dataArray;
    }

    /**
     * opens input stream
     *
     * @param fileName name of file to open
     */
    private void OpenStream(String fileName) throws IOException{
        try {
            inputDIS = new DataInputStream(new FileInputStream(fileName));
        } catch (FileNotFoundException e) {
            Log.report("Can't open file " + fileName);
            System.out.println("Can't open file " + fileName);
            throw new IOException();
        }
    }

    /**
     * initialises buffered reader
     *
     * @param fileName name of file to open
     */
    private void InitBuffReader(String fileName) throws IOException{
        try {
            inputBR = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        } catch (FileNotFoundException e) {
            Log.report("Can't open file " + fileName);
            System.out.println("Can't open file " + fileName);
            throw new IOException();
        }
    }

    /**
     * close input stream
     */
    public void closeReader() throws IOException{
        try {
            if (inputDIS != null)
                inputDIS.close();
        } catch (IOException e) {
            Log.report("Input stream can't be closed.");
            System.out.println("Input stream can't be closed.");
            throw new IOException();
        }
    }

    /**
     * reads line from input stream
     *
     * @return line that was read from stream
     */
    public String ReadLine() throws IOException{
        try {
            if (inputBR != null)
                return inputBR.readLine();
        } catch (IOException e) {
            Log.report("String can't be read.");
            System.out.println("String can't be read.");
            throw new IOException();
        }
        return null;
    }

    /**
     * @return input stream
     */
    public DataInputStream getStream() {
        return inputDIS;
    }

}
