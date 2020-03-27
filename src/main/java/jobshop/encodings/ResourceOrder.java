package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.util.Arrays;

public class ResourceOrder extends Encoding {

    public final Task[][] ressourcesOrderTab;

    private int[] tabNextToSet;

    public ResourceOrder(Instance instance) {
        super(instance);
        ressourcesOrderTab = new Task[instance.numTasks][instance.numJobs];
        for(int i =0;i<instance.numTasks;i++) {
            for (int j = 0; j < instance.numJobs; j++) {
                ressourcesOrderTab[i][j] = null;
            }
        }
        tabNextToSet = new int[instance.numMachines];
        Arrays.fill(tabNextToSet,0);
    }

    public void addTask(int ressource, int job, int task){
        Task temp_task = new Task(job,task);
        ressourcesOrderTab[ressource][tabNextToSet[ressource]++] = temp_task;
    }

    @Override
    public Schedule toSchedule() {

        // time at which each machine is going to be freed
        int[] nextFreeTimeResource = new int[instance.numMachines];

        // for each job, the first task that has not yet been scheduled
        int[] nextTask = new int[instance.numJobs];

        int[] taskDone = new int[instance.numTasks];

        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];

        for(int i = 0 ; i < instance.numMachines ; i++){
            for(int j = 0 ; j < instance.numJobs ; j++){


                int currentJob = ressourcesOrderTab[i][j].job;
                int currentTask = ressourcesOrderTab[i][j].task;
                System.out.println("_____");
                System.out.println(currentJob);
                System.out.println(currentTask);

                if(currentTask == 0){
                    startTimes[currentJob][currentTask] = 0;
                }else{
                    startTimes[currentJob][currentTask] = startTimes[currentJob][currentTask-1]+instance.duration(currentJob, currentTask-1);
                }


            }
        }

        return new Schedule(instance, startTimes);

    }



}