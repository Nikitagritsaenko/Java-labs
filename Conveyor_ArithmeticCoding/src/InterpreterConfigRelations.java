import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class InterpreterConfigRelations  {
    /**
     * reads relations file, checks lexemes and put lexemes into relationMap
     *
     * @param config  name of config file
     * @param relationsList  list of peon relations
     */
    public int Interpret(String config, ArrayList<Pair<String, String>> relationsList) {
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
                if (lexemePair.length < 2) {
                    Log.report("Error: wrong relations, please define the consumers.");
                    System.out.println("Error: wrong relations, please define the consumers.");
                    return -1;
                }
                if (lexemePair[0].equals(lexemePair[1])) {
                    Log.report("Error: peon refers on himself.");
                    System.out.println("Error: peon refers on himself.");
                    return -1;
                }
                relationsList.add(new Pair<>(lexemePair[0], lexemePair[1]));
            }
        }
        catch (IOException e) {
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
