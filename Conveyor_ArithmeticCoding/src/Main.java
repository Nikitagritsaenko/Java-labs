import java.io.IOException;
import java.lang.System;

public class Main {
    public static void main(String[] args)  {
        if (args.length == 1) {
            /*if (CalcFrequency.Calc("input.txt", "frequencyMap.txt") != 0) {
                System.exit(-1);
            }*/
            try {
                Manager manager = new Manager(args[0]);
                manager.StartConveyor();
            }
            catch (IOException e) {
                System.exit(-1);
            }
        } else {
            Log.report("Wrong file format.");
        }
        Log.close();
    }
}
