public class Grammar {
    public static String DELIMITER = ":";
    public static String SPACES = "\\s";
    public static String EMPTINESS = "";
    public static String LIST_SEPARATOR = ",";

    /**
     * Enum of parameters in config file of manager
     */
    public enum ManagerParams {
        INPUT_FILE,
        OUTPUT_FILE,
        WORKERS_FILE,
        RELATIONS_FILE
    }

    /**
     * Enum of parameters in config file with list of peons
     */
    public enum WorkersParams {
        PEON,
        PEON_FIRST,
        PEON_LAST,
    }

    /**
     * Enum of parameters in config file of peon
     */
    public enum PeonParams {
        CODE_MODE,
        BLOCK_LENGTH,
        FREQ_FILE,
        START_POS,
        SIZE,
        TYPE_LIST,
        NAME,
    }

}
