import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InterpreterConfigManager implements Interpreter<Enum, String> {

    private static final Map<String, Enum> lexemeMap;

    static {
        lexemeMap = new HashMap<>();
        lexemeMap.put("IN", Grammar.ManagerParams.INPUT_FILE);
        lexemeMap.put("OUT", Grammar.ManagerParams.OUTPUT_FILE);
        lexemeMap.put("WORKERS", Grammar.ManagerParams.WORKERS_FILE);
        lexemeMap.put("RELATIONS", Grammar.ManagerParams.RELATIONS_FILE);
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
     * reads config file, checks lexemes and put lexemes into resultMap
     *
     * @param config  name of config file
     * @param resultMap  map of lexemes and it's values
     * @return error code (0 - success, -1 - otherwise)
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

        if (resultMap.putIfAbsent(Grammar.ManagerParams.OUTPUT_FILE, "output_default.txt") == null) {
            Log.report("Missing output file, using default output file - output_default.txt");
        }

        for (Enum elem: Grammar.ManagerParams.values()) {
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
