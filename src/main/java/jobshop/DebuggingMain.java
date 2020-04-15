package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.GreedySolver;

import java.io.IOException;
import java.nio.file.Paths;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            ResourceOrder rso = new ResourceOrder(instance);

            GreedySolver grSo = new GreedySolver();

            Result r = grSo.solve(instance,0);


        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}