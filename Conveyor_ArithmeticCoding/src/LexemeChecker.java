import java.util.Map;

public class LexemeChecker {
    /**
     * checks if pair (of lexeme and it's value) consists of 2 values (not of 1 or 0)
     *
     * @param lexemePair  pair (of lexeme and it's value)
     * @return true, if pair is correct, false otherwise
     */
    private static boolean IsPairSizeCorrect(String[] lexemePair) {
        if (lexemePair.length != 2 || lexemePair[0].isEmpty() || lexemePair[1].isEmpty()) {
            Log.report("Invalid format of config file ");
            System.out.println("Invalid format of config file");
            return false;
        }
        return true;
    }


    /**
     * checks if pair (of lexeme and it's value) consists of correct lexemes
     *
     * @param lexemePair  pair (of lexeme and it's value)
     * @param lexemeMap   map of lexemes and it's allowed values
     * @return true, if pair is correct, false otherwise
     */
    public static final boolean IsLexemesCorrect(String[] lexemePair, Map<String, Enum> lexemeMap) {
        if (!lexemeMap.containsKey(lexemePair[0])) {
            Log.report("Unexpected lexeme " + lexemePair[0]);
            System.out.println("Unexpected lexeme " + lexemePair[0]);
            return false;
        }
        return true;
    }

    /**
     * checks if pair (of lexeme and it's value) is correct
     *
     * @param lexemePair  pair (of lexeme and it's value)
     * @param lexemeMap   map of lexemes and it's allowed values
     * @return true, if pair is correct, false otherwise
     */
    public static final boolean IsPairCorrect(String[] lexemePair, Map<String, Enum> lexemeMap) {
        if (!IsPairSizeCorrect(lexemePair)) {
            return false;
        }
        if (!IsLexemesCorrect(lexemePair, lexemeMap)) {
            return false;
        }
        return true;
    }
}
