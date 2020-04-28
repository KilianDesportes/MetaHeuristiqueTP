package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.awt.event.WindowStateListener;
import java.lang.reflect.Array;
import java.time.format.ResolverStyle;
import java.util.*;

public class DescentSolver implements Solver {

    /**
     * A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     * <p>
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     * <p>
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     */
    static class Block {
        /**
         * machine on which the block is identified
         */
        final int machine;
        /**
         * index of the first task of the block
         */
        final int firstTask;
        /**
         * index of the last task of the block
         */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     * <p>
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     * <p>
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /**
         * Apply this swap on the given resource order, transforming it into a new solution.
         */
        public void applyOn(ResourceOrder order) {

            int swapTask1 = this.t1;
            int swapTask2 = this.t2;
            int machine = this.machine;

            //System.out.println("Swapping " + swapTask1 + " and " + swapTask2 + " on  " + machine);

            Task temp = order.tasksByMachine[machine][swapTask1];
            order.tasksByMachine[machine][swapTask1] = order.tasksByMachine[machine][swapTask2];
            order.tasksByMachine[machine][swapTask2] = temp;

        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {

        GreedySolver solverGreedy = new GreedySolver();

        solverGreedy.setPriority(1);

        Result r = solverGreedy.solve(instance, deadline);

        ResourceOrder rso = new ResourceOrder(r.schedule);

        ResourceOrder optimalRso = rso.copy();
        int optimalMakespan = optimalRso.toSchedule().makespan();

        boolean newVoisin = true;

        while (newVoisin == true) {

            newVoisin=false;

            ResourceOrder temp = optimalRso.copy();

            List<Swap> alSwap = new ArrayList<>();
            List<Block> alBlock = this.blocksOfCriticalPath(temp);
            for (int i = 0; i < alBlock.size(); i++) {
                alSwap.addAll(this.neighbors(alBlock.get(i)));
            }

            for (int i = 0; i < alSwap.size(); i++) {

                alSwap.get(i).applyOn(temp);

                int makespanCurrent = temp.toSchedule().makespan();

                if (optimalMakespan > makespanCurrent) {
                    optimalRso = temp;
                    optimalMakespan = makespanCurrent;
                    newVoisin=true;
                }

            }

        }


        return new Result(instance, optimalRso.toSchedule(), Result.ExitCause.Blocked);
    }

    int indexTaskOnMachine(ResourceOrder rso, int machine, Task taskObj) {

        int indexTask = -1;

        for (int i = 0; i < rso.instance.numJobs; i++) {
            if (rso.tasksByMachine[machine][i].equals(taskObj)) {
                indexTask = i;
            }
        }

        return indexTask;

    }

    /**
     * Returns a list of all blocks of the critical path.
     */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {

        Schedule sch = order.toSchedule();

        List<Task> listTask = sch.criticalPath();

        /*
        System.out.println("---Critical path ---");
        for(int i = 0 ; i < listTask.size() ; i++){
            System.out.print(listTask.get(i) + " ");
        }
        System.out.println("\n---------------");
*/

        ArrayList<Integer> alMachines = new ArrayList<Integer>();

        int indexDebut = -1;
        int indexFin = -1;
        int currentMachine = -1;

        ArrayList<Block> alBlock = new ArrayList<>();

        for (int j = 0; j < listTask.size() - 1; j++) {
            int task = listTask.get(j).task;
            int job = listTask.get(j).job;
            int machine = order.instance.machine(job, task);
            int indexTask = indexTaskOnMachine(order, machine, listTask.get(j));

            if (currentMachine == -1 || currentMachine != machine) {
                currentMachine = machine;
                indexDebut = indexTask;
            }

            int nextTask = listTask.get(j + 1).task;
            int nextJob = listTask.get(j + 1).job;
            int nextMachine = order.instance.machine(nextJob, nextTask);
            int nextIndexTask = indexTaskOnMachine(order, nextMachine, listTask.get(j + 1));

            if (nextMachine != machine) {
                indexFin = indexTask;
                if (indexDebut != indexFin) {
                    alBlock.add(new Block(machine, indexDebut, indexFin));
                }
            } else if (j == listTask.size() - 2) {
                indexFin = nextIndexTask;
                if (indexDebut != indexFin) {
                    alBlock.add(new Block(machine, indexDebut, indexFin));
                }
            }

        }
/*
        System.out.println("Size of block list = " + alBlock.size());
        for(int k = 0 ; k < alBlock.size() ; k++){
            System.out.println("machine = " + alBlock.get(k).machine
                    + " | firstTask = " + alBlock.get(k).firstTask
                    + " | lastTask = " + alBlock.get(k).lastTask);
        }*/

        return alBlock;
    }

    /**
     * For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood
     */
    List<Swap> neighbors(Block block) {

        int machine = block.machine;
        int firstTask = block.firstTask;
        int lastTask = block.lastTask;

        ArrayList<Swap> alSwap = new ArrayList<>();

        //System.out.println("Machine " + machine + " lastTask " + lastTask);

        for (int i = firstTask; i < lastTask; i++) {
            for (int j = i + 1; j <= lastTask; j++) {
                alSwap.add(new Swap(machine, i, j));
                // System.out.println("Swap between " + i + " and " + j );
            }
        }

        return alSwap;
    }

}
