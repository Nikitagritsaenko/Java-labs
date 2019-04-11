import javafx.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Peon implements Executor {
    private PeonData peonData;
    private Object data;
    private ArrayList<Executor> consumers = new ArrayList<>();   // list of consumers for provider
    private Map<Executor, Pair<Object, AdapterType>> adapters = new HashMap<>();  //for consumer: providers & adapters
    private ArrayList<Object> allProvidersData = new ArrayList<>();
    ArrayList<Executor> providers = new ArrayList<>();
    private boolean readyToWrite;
    private boolean readyToRead;
    private boolean available;
    private boolean over;
    private MyReader reader;
    private MyWriter writer;
    private final int sleepTimeMsc = 1500;
    private boolean isSorted = false;

    /**
     * sets flag if this peon can write data at this moment
     * @param flag boolean flag
     */
    private void setReadyToWrite(boolean flag) {
        readyToWrite = flag;
    }

    /**
     * sets flag if this peon can read data at this moment
     * @param flag boolean flag
     */
    private void setReadyToRead(boolean flag) {
        readyToRead = flag;
    }

    /**
     * sets flag if this peon is available at this moment
     * @param flag boolean flag
     */
    private void setAvailable(boolean flag) {
        available = flag;
    }

    /**
     * sets flag if this peon finished work with current block of data
     * @param flag boolean flag
     */
    private void setOver(boolean flag) {
        over = flag;
    }

    /**
     * Returns time which peon must sleep
     * @return time in milliseconds
     */
    private int getSleepTime() {
        return sleepTimeMsc;
    }

    /**
     * Checks if current peon ready to write data
     * @return flag if peon ready to write
     */
    public boolean isReadyToWrite() {
        return this.readyToWrite;
    }

    /**
     * Checks if current peon ready to read data
     * @return flag if peon ready to read
     */
    public boolean isReadyToRead() {
        return readyToRead;
    }

    /**
     * Checks if current peon is available
     * @return flag if peon is available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     *  Checks if current peon is finished to work with current block of data
     * @return flag if peon is finished
     */
    public boolean isOver() {
        return over;
    }

    /**
     * constructs peon
     */
    public Peon() {
        setReadyToRead(true);
        setReadyToWrite(false);
        setAvailable(true);
        setOver(true);
    }

    @Override
    public void setInput(DataInputStream inputStream) {
        reader = new MyReader(inputStream);
    }

    @Override
    public void setOutput(DataOutputStream outputStream) {
        setReadyToWrite(true);
        writer = new MyWriter(outputStream);
    }

    /**
     * choose appropriate adapter and create it by type
     *
     * @param type type that already has been chosen
     * @return adapter
     */
    private Object ChooseAdapter(AdapterType type) {
        if (type == AdapterType.BYTE) {
            ByteTransfer adapter = new ByteTransfer();
            if (peonData.getCodeMode() == 1) {
                Object prev_adapter = adapters.entrySet().iterator().next().getValue().getKey();
                int offset = ((ByteTransfer) (prev_adapter)).getStart();
                ((ByteAdapter) adapter).setMetrics(peonData.getStartPos(), offset);
            }
            else ((ByteAdapter) adapter).setMetrics(peonData.getStartPos(), peonData.getStartPos());
            return adapter;
        }
        if (type == AdapterType.DOUBLE) {
            DoubleTransfer adapter = new DoubleTransfer();
            if (peonData.getCodeMode() == 1) {
                Object prev_adapter = adapters.entrySet().iterator().next().getValue().getKey();
                int offset = ((DoubleTransfer) (prev_adapter)).getStart();
                ((DoubleAdapter) adapter).setMetrics(peonData.getStartPos(), offset);
            }
            else ((DoubleAdapter) adapter).setMetrics(peonData.getStartPos(), peonData.getStartPos());
            return adapter;
        }
        if (type == AdapterType.CHAR) {
            CharTransfer adapter = new CharTransfer();
            if (peonData.getCodeMode() == 1) {
                Object prev_adapter = adapters.entrySet().iterator().next().getValue().getKey();
                int offset = ((CharTransfer) (prev_adapter)).getStart();
                ((CharAdapter) adapter).setMetrics(peonData.getStartPos(), offset);
            }
            else ((CharAdapter) adapter).setMetrics(peonData.getStartPos(), peonData.getStartPos());
            return adapter;
        }
        return null;
    }

    @Override
    public void setConsumer(Executor consumer) throws IOException {
        Object adapter;
        for (AdapterType type : consumer.getReadableTypes()) {
            for (AdapterType thisType : this.getReadableTypes()) {
                if (type == thisType) {
                    adapter = ChooseAdapter(type);
                    if (adapter == null) {
                        throw new IOException();
                    }
                    consumer.setAdapter(this, adapter, type); //give to all consumers appropriate adapter
                    consumers.add(consumer);
                    return;
                }
            }
        }
    }

    @Override
    public void setConfigFile(String configFile) throws IOException {
        Map<Enum, String> paramMap = new HashMap<>();
        InterpreterConfigPeon interpreterPeon = new InterpreterConfigPeon();
        if (interpreterPeon.Interpret(configFile, paramMap) != 0) {
            throw new IOException();
        }
        int codeMode = Integer.parseInt(paramMap.get(Grammar.PeonParams.CODE_MODE));
        int blockLength = Integer.parseInt(paramMap.get(Grammar.PeonParams.BLOCK_LENGTH));
        int startPos = Integer.parseInt(paramMap.get(Grammar.PeonParams.START_POS));
        int size = Integer.parseInt(paramMap.get(Grammar.PeonParams.SIZE));
        String freqFile = paramMap.get(Grammar.PeonParams.FREQ_FILE);
        String typeList = paramMap.get(Grammar.PeonParams.TYPE_LIST);
        String name = paramMap.get(Grammar.PeonParams.NAME);
        peonData = new PeonData(codeMode, blockLength, freqFile, startPos, size, typeList, name);
        Coder.Init(freqFile);
    }

    @Override
    public ArrayList<AdapterType> getReadableTypes() {
        return peonData.getTypeList();
    }

    @Override
    public void setAdapter(Executor provider, Object adapter, AdapterType type) {
        adapters.put(provider, new Pair<>(adapter, type));
    }

    /**
     * Checks if all consumers of this peon ready to get his data
     * @return flag if all consumers of this peon ready to get his data
     */
    synchronized private boolean AreAllConsumersReadyToGet() {
        for (Executor ex: consumers) {
            if (!ex.isReadyToRead()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all providers of this peon are available
     * @return flag if all providers of this peon are available
     */
    synchronized private boolean AreAllProvidersAvailable() {
        for (Executor ex: adapters.keySet()) {
            if (!ex.isAvailable()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all providers of this peon are ready to give data to him
     * @return flag if all providers of this peon are ready to give data to him
     */
    synchronized private boolean AreAllProvidersReadyToWrite() {
        for (Executor ex: adapters.keySet()) {
            if (!ex.isReadyToWrite()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all providers of this peon finished to work with old block of data
     * @return flag if all providers of this peon finished to work with old block of data
     */
    synchronized private boolean AreAllProvidersOver() {
        for (Executor ex: adapters.keySet()) {
            if (!ex.isOver()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Runs algorithm of worker who only read data from input file
     */
    private void RunReader() {
        System.out.println("Reader");
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            if (!AreAllConsumersReadyToGet()) {
                continue;
            }
            data = reader.readBuff(peonData.getBlockLength());
            if (data == null) {
                setReadyToWrite(false);
                setAvailable(false);
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    return;
                }
                break;
            } else {
                setOver(true);
                setReadyToWrite(true);
                setAvailable(true);
                System.out.println("got data " + ((byte[]) data).length);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(getSleepTime());
            } catch (InterruptedException e) {
                return;
            }
            setOver(false);
            setReadyToRead(true);
            setReadyToWrite(false);
            setAvailable(true);
        }
    }

    /**
     * Runs algorithm of workers who read data from another workers and give coded data to next workers
     */
    private void RunPeon() {
        System.out.println(this.peonData.getName());
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            if (!AreAllProvidersReadyToWrite()) {
                continue;
            }
            Executor provider = adapters.keySet().iterator().next();
            if (!provider.isOver()) {
                continue;
            }
            System.out.println(this.peonData.getName() + " got his provider ");
            put(provider);
            setReadyToRead(false);
            setOver(true);

            System.out.println(this.peonData.getName() + " CODING");
            if (data != null)
                data = Coder.RunCoders((byte[]) data, peonData);
            while (!AreAllConsumersReadyToGet());
            setReadyToWrite(true);
            setAvailable(true);
            setReadyToRead(true);

            try {
                TimeUnit.MILLISECONDS.sleep(100);
                setReadyToWrite(false);
                TimeUnit.MILLISECONDS.sleep(getSleepTime()-100);
            } catch (InterruptedException e) {
                return;
            }
            setOver(false);
        }
    }

    /**
     * Runs algorithm of worker who only write data to output file
     */
    private void RunWriter() {
        System.out.println("Writer");
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            if (!AreAllProvidersAvailable()) {
                continue;
            }
            if (!AreAllProvidersOver()) {
                continue;
            }
            setReadyToRead(true);
            setAvailable(true);

            GetDataFromAllProviders();

            setReadyToRead(false);
            setAvailable(true);

            WriteAllDataToFile();

            setReadyToRead(true);
            setAvailable(true);
            try {
                TimeUnit.MILLISECONDS.sleep(getSleepTime());
            } catch (InterruptedException e) {
                return;
            }
            setReadyToRead(true);
            setReadyToWrite(true);
            setAvailable(true);
        }
    }

    /**
     * Calls specified function for each type of peon (reader/writer/simple peon)
     */
    public void run() {
        if (reader != null) {
            RunReader();
        }
        else if (writer != null) {
            RunWriter();
        }
        else {
            RunPeon();
        }
    }

    /**
     * reads data from provider and cast it to appropriate type
     * @param adapter reference to class that can read data using method getNext___()
     * @param type appropriate type to identify adapter
     * @return data read from provider
     */
    private Object MakePut(Object adapter, AdapterType type) {
        int i;
        Object data = null;
        int startPos = peonData.getStartPos(), size = peonData.getSize();

        if (type == AdapterType.BYTE) {
            byte[] tmpDataArray = new byte[size];
            ((ByteAdapter)adapter).setMetrics(startPos, startPos);
            for (i = 0; i < size; i += 1) {
                Byte tmp = ((ByteAdapter)adapter).getNextByte();
                if (tmp == null) {
                    break;
                }
                tmpDataArray[i] = tmp;
            }
            if (i == 0) {
                return null;
            }
            byte[] dataArray;
            dataArray = Arrays.copyOf(tmpDataArray,i);
            data = dataArray;
        }
        if (type == AdapterType.DOUBLE) {
            double[] tmpDataArray = new double[size / Double.BYTES];
            ((DoubleAdapter)adapter).setMetrics(startPos, size);
            for (i = 0; i < tmpDataArray.length; i += 1) {
                Double tmp = ((DoubleAdapter)adapter).getNextDouble();
                if (tmp == null) {
                    break;
                }
                tmpDataArray[i] = tmp;
            }
            if (i == 0) {
                return null;
            }
            double[] dataArray;
            dataArray = Arrays.copyOf(tmpDataArray, i);
            data = dataArray;
            data = Convert.DoubleArrayToByteArray(dataArray);
        }
        if (type == AdapterType.CHAR) {
            char[] tmpDataArray = new char[size / Character.BYTES];
            ((CharAdapter)adapter).setMetrics(startPos, size);
            for (i = 0; i < tmpDataArray.length; i += 1) {
                Character tmp = ((CharAdapter)adapter).getNextChar();
                if (tmp == null) {
                    break;
                }
                tmpDataArray[i] = tmp;
            }
            if (i == 0) {
                return null;
            }
            char[] dataArray;
            dataArray = Arrays.copyOf(tmpDataArray, i);
            data = dataArray;
            data = Convert.CharArrayToByteArray(dataArray);
        }
        return data;
    }

    /**
     * puts data to current peon using adapter from provider
     * @param provider provider of this peon who has some useful data
     */
    synchronized private void put(Executor provider) {
        Object adapter = adapters.get(provider).getKey();
        AdapterType type = adapters.get(provider).getValue();

        data = MakePut(adapter, type);
        if (data != null)
            System.out.println(this.peonData.getName() + " gets data from providers " + ((byte[])data).length);
    }

    /**
     * This function for workers who has more than one provider.
     * It collects data from every provider to array list.
     */
    private void GetDataFromAllProviders() {
        if (!isSorted)
            getSortedProviderList();
        System.out.println(this.peonData.getName() + " got provider list");
        for (Executor ex: providers) {
            for (Map.Entry<Executor, Pair<Object, AdapterType>> provider : adapters.entrySet()) {
                if (ex.equals(provider.getKey())) {
                    System.out.println("Put: to " + this.peonData.getName());
                    Object data = MakePut(provider.getValue().getKey(), provider.getValue().getValue());
                    if (data != null)
                        allProvidersData.add(data);
                }
            }
        }
    }

    /**
     * Writes data from all providers to file (this function for writer)
     */
    private void WriteAllDataToFile() {
        System.out.println(this.peonData.getName() + " writing to file" + "\n -------------------------------");
        for (Object data: allProvidersData) {
            if (data != null)
                writer.writeBuff((byte[])data);
        }
        allProvidersData.clear();
    }

    /**
     * Gets offset of encoder worker
     * @param ex   some worker
     * @param type type of adapter
     * @return offset of encoder worker
     */
    private int getOffset(Executor ex, AdapterType type) {
        if (type.equals(AdapterType.BYTE)) {
            return  ((ByteAdapter)(adapters.get(ex).getKey())).getStart();
        }
        if (type.equals(AdapterType.DOUBLE)) {
            return ((DoubleAdapter)(adapters.get(ex).getKey())).getStart();
        }
        if (type.equals(AdapterType.CHAR)) {
            return ((CharAdapter)(adapters.get(ex).getKey())).getStart();
        }
        return 0;
    }

    /**
     * returns provider if its single,
     * otherwise gets all providers and sorts them by offset.
     */
    private void getSortedProviderList() {
        providers = new ArrayList<>();
        for (Map.Entry<Executor, Pair<Object, AdapterType>> provider: adapters.entrySet()) {
            providers.add(provider.getKey());
        }
        System.out.println(this.peonData.getName() + " got provider list");

        if (providers.size() == 1) {
            return;
        }
        Collections.sort(providers, new Comparator<Executor>() {
            public int compare(Executor first,
                               Executor second) {
                AdapterType type1 = adapters.get(first).getValue();
                int pos1 = getOffset(first, type1);
                AdapterType type2 = adapters.get(second).getValue();
                int pos2 = getOffset(second, type2);
                return pos1 - pos2;
            }
        });
        isSorted = true;
    }

    class ByteTransfer implements ByteAdapter {
        private int currentPosition = 0;
        private Pair<Integer, Integer> blockMetrics;
        @Override
        public void setMetrics(int start, int offset) {
            blockMetrics = new Pair<>(start, offset);
            currentPosition = start;
        }
        @Override
        public Integer getStart() {
            return blockMetrics.getValue();
        }
        @Override
        public Byte getNextByte() {
            if (data == null)
                return null;
            if (currentPosition >= ((byte[]) data).length) {
                currentPosition = 0;
                return null;
            }
            return ((byte[]) data)[currentPosition++];
        }
    }

    class CharTransfer implements CharAdapter {
        private int currentPosition = 0;
        private Pair<Integer, Integer> blockMetrics;
        @Override
        public void setMetrics(int start, int offset) {
            blockMetrics = new Pair<>(start, offset);
            currentPosition = start;
        }
        @Override
        public Integer getStart() {
            return blockMetrics.getValue();
        }
        @Override
        public Character getNextChar() {
            if (data == null)
                return null;
            if (currentPosition  >= ((byte[]) data).length) {
                currentPosition = 0;
                return null;
            }
            byte[] oneCharacter = new byte[Character.BYTES];
            for (int i = 0; i < oneCharacter.length; i++) {
                if (currentPosition  >= ((byte[]) data).length) {
                    currentPosition = 0;
                    return null;
                }
                else
                    oneCharacter[i] = ((byte[]) data)[currentPosition++];
            }
            return Convert.ByteArrayToCharacter(oneCharacter);
        }
    }

    class DoubleTransfer implements DoubleAdapter {
        private int currentPosition = 0;
        private Pair<Integer, Integer> blockMetrics;
        @Override
        public void setMetrics(int start, int offset) {
            blockMetrics = new Pair<>(start, offset);
            currentPosition = start;
        }
        @Override
        public Integer getStart() {
            return blockMetrics.getValue();
        }
        @Override
        public Double getNextDouble() {
            if (data == null)
                return null;
            if (currentPosition  >= ((byte[]) data).length) {
                currentPosition = 0;
                return null;
            }
            byte[] oneDouble = new byte[Double.BYTES];
            for (int i = 0; i < oneDouble.length; i++) {
                if (currentPosition  >= ((byte[]) data).length) {
                    currentPosition = 0;
                    return null;
                }
                oneDouble[i] = ((byte[]) data)[currentPosition++];
            }
            return Convert.ByteArrayToDouble(oneDouble);
        }
    }
}
