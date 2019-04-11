import javafx.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Manager {
    private Map<Enum, String> configMap;
    private ArrayList<Executor> peons;
    private ArrayList<String> names = new ArrayList<>();
    private ExecutorService pool;
    /**
     * constructing Manager
     * @param configFile name of config file
     */
    public Manager(String configFile) throws IOException {
        peons = new ArrayList<>();
        ArrayList<Pair<String, Enum>> workersList = new ArrayList<>();
        ArrayList<Pair<String, String>> relationsList = new ArrayList<>();
        configMap = new EnumMap(Grammar.ManagerParams.class);
        InterpreterConfigManager interpreterManager = new InterpreterConfigManager();
        InterpreterConfigWorkers interpreterWorkers = new InterpreterConfigWorkers();
        InterpreterConfigRelations interpreterRelations = new InterpreterConfigRelations();
        if (interpreterManager.Interpret(configFile, configMap) != 0) {
            throw new IOException();
        }
        if (interpreterWorkers.Interpret(configMap.get(Grammar.ManagerParams.WORKERS_FILE), workersList) != 0) {
            throw new IOException();
        }
        if (interpreterRelations.Interpret(configMap.get(Grammar.ManagerParams.RELATIONS_FILE), relationsList) != 0) {
            throw new IOException();
        }
        if (CreatePeons(workersList) != 0) {
            throw new IOException();
        }
        LinkPeons(relationsList);
    }

    /**
     * puts in map this workers only workers of specific type
     * @param workersList list of workers
     * @param lexeme some word that identifies specific type of peon
     */
    private int CreatePeonsOfType(ArrayList<Pair<String, Enum>> workersList, Enum lexeme)  {
        for (Pair<String, Enum> pair: workersList) {
            Enum e = pair.getValue();
            String prop = pair.getKey();
            String[] propArr = prop.split(Grammar.LIST_SEPARATOR);
            if (e.equals(lexeme)) {
                try {
                    Executor currentPeon = (Executor) Class.forName(propArr[2]).getDeclaredConstructor().newInstance();
                    try {
                       currentPeon.setConfigFile(propArr[1]);
                    }
                    catch (IOException ex) {
                        return -1;
                    }
                    peons.add(currentPeon);
                }
                catch (ReflectiveOperationException ex) {
                    return -1;
                }
                names.add(propArr[0]);
            }
        }
        return 0;
    }

    /**
     * Creates all peons using information in map of workers
     * @param workersList list of workers
     * @return error code (0 - success, -1 - otherwise)
     */
    private int CreatePeons(ArrayList<Pair<String, Enum>> workersList) {
        if (CreatePeonsOfType(workersList, Grammar.WorkersParams.PEON_FIRST) != 0) {
            return -1;
        }
        if (CreatePeonsOfType(workersList, Grammar.WorkersParams.PEON) != 0) {
            return -1;
        }
        if (CreatePeonsOfType(workersList, Grammar.WorkersParams.PEON_LAST) != 0) {
            return -1;
        }
        return 0;
    }

    /**
     * Sets the relations for executor with some index
     * @param relationsList pairs of relations: for ex. "peon1:peon2"
     * @param index index of peon
     */
    private void Link(ArrayList<Pair<String, String>> relationsList, int index) throws IOException {
        String currentName, consumerNames = new String();
        Executor current = peons.get(index);
        currentName = names.get(index);
        for (Pair p: relationsList) {
            if (p.getKey().equals(currentName)) {
                consumerNames = p.getValue().toString();
            }
        }
        String[] namesArray = consumerNames.split(Grammar.LIST_SEPARATOR);
        for (String consumerName: namesArray) {
            for (int i = 0; i < names.size(); ++i) {
                String peonName = names.get(i);
                if (consumerName.equals(peonName)) {
                    Executor ex = peons.get(i);
                    try {
                        current.setConsumer(ex);
                    }
                    catch (IOException e) {
                        Log.report("Link failed.");
                        throw new IOException();
                    }
                }
            }
        }

    }

    /**
     * Links peons that already was created without relations between them
     * @param relationsList pairs of relations: for ex. "peon1:peon2" means, that peon2 is consumer of peon1
     */
    private void LinkPeons(ArrayList<Pair<String, String>> relationsList) throws IOException{
        for (int index = 0; index < relationsList.size(); index++) {
            Link(relationsList, index);
        }
    }

    /**
     * Runs the conveyor
     */
    public void StartConveyor() {
        MyReader reader;
        MyWriter writer;
        Executor firstPeon = peons.get(0);
        Executor lastPeon = peons.get(peons.size() - 1);
        try {
            reader = new MyReader(configMap.get(Grammar.ManagerParams.INPUT_FILE));
            writer = new MyWriter(configMap.get(Grammar.ManagerParams.OUTPUT_FILE));
        }
        catch (IOException e) {
            return;
        }
        firstPeon.setInput(reader.getStream());
        lastPeon.setOutput(writer.getStream());

        pool = Executors.newFixedThreadPool(peons.size());

        for (Executor worker : peons) {
            pool.execute(() -> {
                worker.run();
                pool.shutdownNow();
                writer.closeWriter();
                try {
                    reader.closeReader();
                }
                catch (IOException ex) {
                    System.out.println("Exception occurred in conveyor");
                }
            });
        }

    }

}