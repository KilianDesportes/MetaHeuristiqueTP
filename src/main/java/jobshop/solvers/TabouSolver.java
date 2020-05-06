package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.*;

public class TabouSolver implements Solver {

    GreedySolver solverGreedy = new GreedySolver();

    int maxIter = 500;

    int dureeTaboo = 10;

    public void setGreedyPrio(int i){
        solverGreedy.setPriority(i);
    }

    public void setMaxIter(int i){
        this.maxIter = i ;
    }

    public void setDureeTaboo(int i){
        this.dureeTaboo = i ;
    }

    @Override
    public Result solve(Instance instance, long deadline) {

        solverGreedy.setPriority(6);

        Result r = solverGreedy.solve(instance, deadline);

        ResourceOrder rso = new ResourceOrder(r.schedule);

        ResourceOrder bestSolution = rso.copy();

        ResourceOrder currentSolution = rso.copy();

        int nbJobs = instance.numJobs;
        int nbMachines = instance.numMachines;
        int mul = nbJobs * nbMachines;

        int sTaboo[][] = new int[mul][mul];

        //Init a 0 de la matrice tabou
        for(int i = 0 ; i < mul ; i ++){
            for(int j = 0 ; j < mul ; j ++){
                sTaboo[i][j] = 0;
            }
        }

        int k = 0;

        while(k < maxIter && (deadline - System.currentTimeMillis() > 1)) {

            k++;

            List<DescentSolver.Swap> alSwap = new ArrayList<>();
            List<DescentSolver.Block> alBlock = this.blocksOfCriticalPath(currentSolution);
            for (int i = 0; i < alBlock.size(); i++) {
                alSwap.addAll(this.neighbors(alBlock.get(i)));
            }

            ResourceOrder bestVoisin = null;
            int bestVoisinT1 = 0;
            int bestVoisinT2 = 0;


            for (int i = 0; i < alSwap.size(); i++) {

                ResourceOrder swapTestEnCours = currentSolution.copy();

                alSwap.get(i).applyOn(swapTestEnCours);

                int t1 = (alSwap.get(i).machine * instance.numJobs) + alSwap.get(i).t1;

                int t2 = (alSwap.get(i).machine * instance.numJobs) + alSwap.get(i).t2;

                Schedule newSch = swapTestEnCours.toSchedule();

                if(newSch != null){

                    int swapTestMakespan = newSch.makespan();

                    if(bestVoisin == null || bestVoisin.toSchedule().makespan() > swapTestMakespan ){

                        if(sTaboo[t1][t2] < k) {
                            bestVoisin = swapTestEnCours.copy();
                            bestVoisinT1 = t1;
                            bestVoisinT2 = t2;
                        }

                    }else if((sTaboo[t1][t2] >= k && bestSolution.toSchedule().makespan() > swapTestMakespan)){

                        bestSolution = swapTestEnCours.copy();
                        bestVoisin = swapTestEnCours.copy();
                        bestVoisinT1 = t1;
                        bestVoisinT2 = t2;

                    }
                }
            }

            if(bestVoisin!=null){
                sTaboo[bestVoisinT1][bestVoisinT2] = k + dureeTaboo;

                currentSolution = bestVoisin.copy();
                if(bestSolution.toSchedule().makespan() > bestVoisin.toSchedule().makespan()){
                    bestSolution = bestVoisin.copy();
                }
            }

        }

        return new Result(instance, bestSolution.toSchedule(), Result.ExitCause.Blocked);

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

    List<DescentSolver.Block> blocksOfCriticalPath(ResourceOrder order) {

        Schedule sch = order.toSchedule();

        List<Task> listTask = sch.criticalPath();

        ArrayList<Integer> alMachines = new ArrayList<Integer>();

        int indexDebut = -1;
        int indexFin = -1;
        int currentMachine = -1;

        ArrayList<DescentSolver.Block> alBlock = new ArrayList<>();

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
                    alBlock.add(new DescentSolver.Block(machine, indexDebut, indexFin));
                }
            } else if (j == listTask.size() - 2) {
                indexFin = nextIndexTask;
                if (indexDebut != indexFin) {
                    alBlock.add(new DescentSolver.Block(machine, indexDebut, indexFin));
                }
            }

        }

        return alBlock;
    }


    List<DescentSolver.Swap> neighbors(DescentSolver.Block block) {

        int machine = block.machine;
        int firstTask = block.firstTask;
        int lastTask = block.lastTask;

        ArrayList<DescentSolver.Swap> alSwap = new ArrayList<>();

        if(lastTask-firstTask == 1){
            alSwap.add(new DescentSolver.Swap(machine,lastTask,firstTask));
        }else{
            alSwap.add(new DescentSolver.Swap(machine,firstTask,firstTask+1));
            alSwap.add(new DescentSolver.Swap(machine,lastTask-1,lastTask));
        }

        /*for (int i = firstTask; i < lastTask; i++) {
            for (int j = i + 1; j <= lastTask; j++) {
                alSwap.add(new DescentSolver.Swap(machine, i, j));
            }
        }*/

        return alSwap;
    }

}