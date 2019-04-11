import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CalcFrequency {
    /**
     * calculates frequency for every byte of byte array
     *
     * @param inputFile  file with data to encode
     * @param outputFile file in which function will write information about each byte frequency
     * @return  file with pairs consists of every byte and it's frequency segment
     */
    public static int Calc(String inputFile, String outputFile) {
        byte[] data;
        try {
            data = Files.readAllBytes(Paths.get(inputFile));
        }
        catch (IOException e) {
            Log.report("Error in reading file: " + inputFile);
            return -1;
        }
        Map<Byte, Double> freqMap = new HashMap<>();
        Map<Byte, Segment> segmentMap = new HashMap<>();
        int len = data.length;
        for (int i = 0; i < len; ++i) {
            freqMap.put(data[i], 0.0);
        }
        for (int i = 0; i < len; ++i){
            freqMap.put(data[i],  freqMap.get(data[i]) + 1.0/len);
        }
        double shiftFromOne = 0.0;
        for (Byte key : freqMap.keySet()) {
            double elemFreq = freqMap.get(key);
            segmentMap.put(key, new Segment(1 - elemFreq - shiftFromOne, 1 - shiftFromOne));
            shiftFromOne += elemFreq;
        }
        try {
            MyWriter writer = new MyWriter(outputFile);
            writer.WriteFrequency(segmentMap);
            writer.CloseFileWriter();
        }
        catch (IOException e) {
            return -1;
        }
        return 0;
    }
}
