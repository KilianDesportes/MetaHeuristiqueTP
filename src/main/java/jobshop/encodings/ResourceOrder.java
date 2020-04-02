package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.time.format.ResolverStyle;
import java.util.Arrays;

public class ResourceOrder extends Encoding {

    public final Task[][] ressourcesOrderTab;

    private int[] tabNextToSet;

    public ResourceOrder(Instance instance) {
        super(instance);
        ressourcesOrderTab = new Task[instance.numTasks][instance.numJobs];
        for (int i = 0; i < instance.numTasks; i++) {
            for (int j = 0; j < instance.numJobs; j++) {
                ressourcesOrderTab[i][j] = null;
            }
        }
        tabNextToSet = new int[instance.numMachines];
        Arrays.fill(tabNextToSet, 0);
    }

    public void addTask(int ressource, int job, int task) {
        Task temp_task = new Task(job, task);
        ressourcesOrderTab[ressource][tabNextToSet[ressource]++] = temp_task;
    }

    @Override
    public Schedule toSchedule() {

        int[][] startTimes = new int[instance.numJobs][instance.numTasks];

        for (int i = 0; i < instance.numMachines; i++) {
            for (int j = 0; j < instance.numJobs; j++) {
                int currentJob = ressourcesOrderTab[i][j].job;
                int currentTask = ressourcesOrderTab[i][j].task;

                if (currentTask == 0) {
                    startTimes[currentJob][currentTask] = 0;
                } else {
                    startTimes[currentJob][currentTask] = startTimes[currentJob][currentTask - 1] + instance.duration(currentJob, currentTask - 1);
                }
            }
        }

        for (int i = 0; i < instance.numMachines; i++) {

            int startTime = 0;

            for (int j = 1; j < instance.numJobs; j++) {

                //Les tâches pour chaques machines

                int currentMachine = i;
                int currentJob = ressourcesOrderTab[i][j].job;
                int currentTask = ressourcesOrderTab[i][j].task;

                int timeToFinish = instance.duration(currentJob, currentTask);


                int jobBefore = ressourcesOrderTab[i][(j - 1)].job;
                int taskBefore = ressourcesOrderTab[i][(j - 1)].task;

                int durationTaskBefore = instance.duration(jobBefore, taskBefore);

                int currentStart = startTimes[currentJob][currentTask];

                int endOfTaskBefore = startTimes[jobBefore][taskBefore] + durationTaskBefore;

                /*Debugging prints
                System.out.println("_______________________________");
                System.out.println("currentMachine : " + currentMachine );
                System.out.println("currentJob : " + currentJob );
                System.out.println("currentTask : " + currentTask );
                System.out.println("timeToFinish : " + timeToFinish );
                System.out.println("jobBefore : " + jobBefore );
                System.out.println("taskBefore : " + taskBefore );
                System.out.println("durationTaskBefore : " + durationTaskBefore );
                System.out.println("currentStart : " + currentStart );
                System.out.println("endOfTaskBefore : " + endOfTaskBefore );
                */

                if (currentStart < endOfTaskBefore) {
                    int gap = endOfTaskBefore - startTimes[currentJob][currentTask];
                    startTimes[currentJob][currentTask] = endOfTaskBefore;

                    //Décaler toutes les tâches suivant liés au même job ? qui ne peuvent se lancer si la précédente n'est pas finie ?

                }

            }
        }

        return new Schedule(instance, startTimes);

    }

    public ResourceOrder fromSchedule(Schedule sched) {

        ResourceOrder rso = new ResourceOrder(this.instance);

            for (int j = 0 ; j < instance.numJobs; j++) {
                for (int k = 0; k < instance.numTasks; k++) {
                    rso.addTask(instance.machine(j,k),j,k);
                }
            }

        return rso;

    }
}