import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PeonData {
    private final int codeMode;
    private final int blockLength;
    private final String freqFile;
    private int startPos;
    private int size;
    private ArrayList<AdapterType> typeList = new ArrayList<>();
    private String name;

    private static final Map<String, AdapterType> typesMap;
    static {
        typesMap = new HashMap<>();
        typesMap.put("BYTE", AdapterType.BYTE);
        typesMap.put("CHAR", AdapterType.CHAR);
        typesMap.put("DOUBLE", AdapterType.DOUBLE);
    }

    /**
     * reads config file, checks lexemes and put lexemes into resultMap
     *
     * @param codeMode defines who is worker: coder or decoder
     * @param blockLength the length of block which peon can read from input stream
     * @param freqFile name of file with frequencies of every byte
     * @param size size of data (in bytes) that this peon wants to get as consumer
     * @param startPos start position of data that peon gets "size" times
     * @param types list of types whit that peon can work
     * @param name name of peon to identify him
     */
    PeonData(int codeMode, int blockLength, String freqFile, int startPos, int size, String types, String name) {
        this.codeMode = codeMode;
        this.freqFile = freqFile;
        this.blockLength = blockLength;
        this.startPos = startPos;
        this.size = size;
        this.name = name;
        String[] peonTypes = types.split(Grammar.LIST_SEPARATOR);
        for (String s: peonTypes) {
            typeList.add(typesMap.get(s));
        }
    }

    /**
     * gets code mode of peon (coder / decoder)
     * @return code mode
     */
    public int getCodeMode() {
        return codeMode;
    }

    /**
     * gets name of peon
     * @return name of peon
     */
    public String getName() {
        return name;
    }

    /**
     * gets types whit that peon can work
     * @return types
     */
    public ArrayList<AdapterType> getTypeList() {
        return typeList;
    }

    /**
     * gets the length of block which peon can read from input stream
     * @return length of block
     */
    public int getBlockLength() {
        return blockLength;
    }

    /**
     * gets start position of data to read
     * @return position
     */
    public int getStartPos() {
        return startPos;
    }

    /**
     * gets size (in bytes) to read
     * @return size
     */
    public int getSize() {
        return size;
    }
}