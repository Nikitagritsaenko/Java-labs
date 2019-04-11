import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class InterpreterConfigPeon implements Interpreter<Enum, String> {

    private static final Map<String, Enum> lexemeMap;

    static {
        lexemeMap = new HashMap<>();
        lexemeMap.put("CODE_MODE", Grammar.PeonParams.CODE_MODE);
        lexemeMap.put("BLOCK_LENGTH", Grammar.PeonParams.BLOCK_LENGTH);
        lexemeMap.put("FREQ_FILE", Grammar.PeonParams.FREQ_FILE);
        lexemeMap.put("START_POS", Grammar.PeonParams.START_POS);
        lexemeMap.put("SIZE", Grammar.PeonParams.SIZE);
        lexemeMap.put("TYPE_LIST", Grammar.PeonParams.TYPE_LIST);
        lexemeMap.put("NAME", Grammar.PeonParams.NAME);
    }

    /**
     * reports if some very useful parameter not found
     *
     * @param resultMap  map of parameters that has been read from config file
     * @param elem  parameter that might be lost
     * @return error code (0 - success, -1 - otherwise)
     */
    private int NotFoundReport(Map<Enum, String> resultMap, Enum elem) {
        if (!resultMap.containsKey(elem)) {
            Log.report(elem + "not found");
            System.out.println(elem + "not found");
            return -1;
        }
        return 0;
    }

    /**
     * reads config file of peon, checks lexemes and put lexemes into resultMap
     *
     * @param config  name of config file
     * @param resultMap  map of lexemes and it's values
     */
    public int Interpret(String config, Map<Enum, String> resultMap) {
        MyReader reader;
        try {
            reader = new MyReader(config);
        }
        catch (IOException e) {
            return -1;
        }
        String ParamString;
        try {
            while ((ParamString = reader.ReadLine()) != null) {
                if ((ParamString = ParamString.replaceAll(Grammar.SPACES, Grammar.EMPTINESS)).isEmpty())
                    continue;
                String[] lexemePair = ParamString.split(Grammar.DELIMITER);
                if (!LexemeChecker.IsPairCorrect(lexemePair, lexemeMap))
                    return -1;
                resultMap.put(lexemeMap.get(lexemePair[0]), lexemePair[1]);
            }
        }
        catch (IOException e) {
            return -1;
        }
        if (resultMap.putIfAbsent(Grammar.PeonParams.CODE_MODE, "0") == null) {
            Log.report("Missing CODE_MODE, using default: 0 for coding");
        }
        if (resultMap.putIfAbsent(Grammar.PeonParams.BLOCK_LENGTH, "5") == null) {
            Log.report("Missing block length, using default block length - 5 symbols");
        }
        for (Enum elem: Grammar.PeonParams.values()) {
            if (NotFoundReport(resultMap, elem) != 0) {
                return -1;
            }
        }
        try {
            reader.closeReader();
        }
        catch (IOException e) {
            return -1;
        }
        return 0;
    }
}
