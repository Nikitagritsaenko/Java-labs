import java.util.*;

public interface Interpreter <K, V> {

    /**
     *
     * @param config name of config file
     * @param resultMap map of something data, that Interpreter feels reading config file
     * @return error code (0 - success, -1 - otherwise)
     */
    int Interpret(String config, Map<K, V> resultMap);
}


