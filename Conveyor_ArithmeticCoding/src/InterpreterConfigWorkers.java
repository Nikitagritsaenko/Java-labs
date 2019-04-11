import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InterpreterConfigWorkers{

    private static final Map<String, Enum> lexemeMap;
    static {
        lexemeMap = new HashMap<>();
        lexemeMap.put("PEON", Grammar.WorkersParams.PEON);
        lexemeMap.put("PEON_FIRST", Grammar.WorkersParams.PEON_FIRST);
        lexemeMap.put("PEON_LAST", Grammar.WorkersParams.PEON_LAST);
    }

    /**
     * reports if some very useful parameter not found
     *
     * @param resultList  list of parameters that has been read from config file
     * @param elem  parameter that might be lost
     * @return code error (0 - success, -1 - otherwise)
     */
    private int NotFoundReport(ArrayList<Pair<String, Enum>> resultList, Enum elem) {
        for (int i = 0; i < resultList.size(); i++) {
            if (resultList.get(i).getValue() == elem) {
                return 0;
            }
        }
        Log.report(elem + "not found");
        System.out.println(elem + "not found");
        return -1;
    }

    /**
     * reads config file of manager, checks lexemes and gets all the config filenames of peons
     *
     * @param config  name of config file
     * @param resultList  list of peon properties: type, name, config file
     */
    public int Interpret(String config, ArrayList<Pair<String, Enum>> resultList)  {
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
                String[] ParamSet = ParamString.split(Grammar.DELIMITER);
                if (!LexemeChecker.IsLexemesCorrect(ParamSet, lexemeMap))
                    return -1;
                resultList.add(new Pair<>(ParamSet[1], lexemeMap.get(ParamSet[0])));
            }
        }
        catch (IOException e) {
            return -1;
        }
        if (NotFoundReport(resultList, Grammar.WorkersParams.PEON_FIRST) != 0) {
            return -1;
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
