import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Coder {
    private static Map<Byte, Segment> freqMap = new HashMap<>();

    /**
     * @param freqFile name of file with every byte frequency
     * @return map of bytes and theirs segments
     * @throws IOException
     */
    public static Map<Byte, Segment> ReadSegmentMap(String freqFile)  {
        Map<Byte, Segment> map = new HashMap<>();
        MyReader reader;
        try {
            reader = new MyReader(freqFile);
        }
        catch (IOException e) {
            return null;
        }
        String line;
        try {
            while ((line = reader.ReadLine()) != null) {
                String[] subStr = line.split(":");
                String[] frontiers = subStr[1].split(" ");
                Byte elem = Byte.parseByte(subStr[0]);
                double low = Double.parseDouble(frontiers[0]);
                double high = Double.parseDouble(frontiers[1]);
                map.put(elem, new Segment(low, high));
            }
        }
        catch (IOException e) {
            return null;
        }
        return map;
    }


    public Coder() { }

    /**
     * Creating coder by file with map of bytes and its frequencies
     * @param freqFile file with map of bytes and its frequencies
     */
    public static void Init(String freqFile) {
        if (freqMap.size() == 0) {
            freqMap = ReadSegmentMap(freqFile);
        }
    }

    /**
     * choose what to do: encode or decode
     *
     * @param block  block of data to encode or decode
     * @param peonData   information about peon: who is him, coder or decoder; his block length
     * @return encoded/decoded array of bytes
     */
    public static byte[] RunCoders(byte[] block, PeonData peonData) {
        if (peonData.getCodeMode() == 0) {
            /*byte[] encoded = Encode(block);
            byte[] resDouble = new byte[Double.BYTES];
            System.arraycopy(encoded, 0, resDouble, 0, Double.BYTES);
            byte[] resInt = new byte[Integer.BYTES];
            System.arraycopy(encoded, resDouble.length, resInt, 0, Integer.BYTES);
            return Decode(resDouble, Convert.ByteArrayToInt(resInt));*/
            return Encode(block);
        }
        else if (peonData.getCodeMode() == 1) {
            byte[] decoded = null, merged;
            int informSize = block.length / (Double.BYTES + Integer.BYTES);
            for (int i = 0; i < informSize; i++) {
                byte[] resDouble = new byte[Double.BYTES];
                System.arraycopy(block, (Double.BYTES + Integer.BYTES) * i, resDouble, 0, Double.BYTES);
                byte[] resInt = new byte[Integer.BYTES];
                System.arraycopy(block, (Double.BYTES + Integer.BYTES) * i + resDouble.length, resInt, 0, Integer.BYTES);
                byte[] res = Decode(resDouble, Convert.ByteArrayToInt(resInt));
                if (decoded == null) {
                    merged = res.clone();
                }
                else {
                    merged = Arrays.copyOf(decoded, res.length + decoded.length);
                    System.arraycopy(res, 0, merged, decoded.length, res.length);
                }
                decoded = merged.clone();
            }
            return decoded;
        }
        else return block;
    }

    /**
     * encode array of bytes
     *
     * @param data  array of bytes to encode
     * @return decoded array of bytes
     */
    public static byte[] Encode(byte[] data) {
        double newHigh, oldHigh = 1.0, oldLow = 0.0, newLow;
        for (int i = 0; i < data.length; ++i) {
            byte c = data[i];
            newHigh = oldLow + (oldHigh - oldLow) * freqMap.get(c).high;
            newLow = oldLow + (oldHigh - oldLow) * freqMap.get(c).low;
            oldHigh = newHigh;
            oldLow = newLow;
        }
        double code = (oldLow + oldHigh) / 2;
        byte[] doubleRes = Convert.DoubleToByteArray(code);
        byte[] intRes = Convert.IntegerToByteArray(data.length);
        byte[] result = Arrays.copyOf(doubleRes, doubleRes.length + intRes.length);
        System.arraycopy(intRes, 0, result, doubleRes.length, intRes.length);
        for(int i = 0; i < result.length; i++) {
            System.out.print(result[i] + " ");
        }
        System.out.println();
        return result;
    }

    /**
     * decode array of bytes
     *
     * @param data  array of bytes to decode
     * @return decoded array of bytes
     */
    public static byte[] Decode(byte[] data, int blockLength) {
        byte elem = 0;
        byte[] result = new byte[blockLength];
        double code = Convert.ByteArrayToDouble(data);
        for (int i = 0; i < blockLength; ++i) {
            for (Byte key : freqMap.keySet()) {
                Segment value = freqMap.get(key);
                if (value.low < code && value.high > code) {
                    elem = key;
                    break;
                }
            }
            result[i] = elem;
            Segment value = freqMap.get(elem);
            code = (code - value.low) / (value.high - value.low);
        }
        return result;
    }
}
