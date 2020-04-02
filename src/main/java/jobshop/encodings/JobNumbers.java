package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.util.Arrays;

/** Représentation par numéro de job. */
public class JobNumbers extends Encoding {

    /** A numJobs * numTasks array containing the representation by job numbers. */
    public final int[] jobs;

    /** In case the encoding is only partially filled, indicates the index of first
     * element of `jobs` that has not been set yet. */
    public int nextToSet = 0;

    public JobNumbers(Instance instance) {
        super(instance);

        jobs = new int[instance.numJobs * instance.numMachines];
        Arrays.fill(jobs, -1);
    }

    @Override
    public Schedule toSchedule() {
        // time at which each machine is going to be freed
        int[] nextFreeTimeResource = new int[instance.numMachines];

        // for each job, the first task that has not yet been scheduled
        int[] nextTask = new int[instance.numJobs];

        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];

        // compute the earliest start time for every task of every job
        for(int job : jobs) {
            int task = nextTask[job];
            int machine = instance.machine(job, task);
            // earliest start time for this task
            int est = task == 0 ? 0 : startTimes[job][task-1] + instance.duration(job, task-1);
            est = Math.max(est, nextFreeTimeResource[machine]);

            startTimes[job][task] = est;
            nextFreeTimeResource[machine] = est + instance.duration(job, task);
            nextTask[job] = task + 1;
        }

        return new Schedule(instance, startTimes);
    }

    public JobNumbers fromSchedule(Schedule sched) {

        JobNumbers jbn = new JobNumbers(this.instance);

        int JobOrder[][] = new int[instance.numMachines][instance.numJobs];

        for (int m = 0 ; m < instance.numMachines; m++) {
            for (int j = 0; j < instance.numJobs; j++) {
                //jbn.jobs[jbn.nextToSet++] = k;
                int t = instance.task_with_machine(j,m);
                JobOrder[m][j] =j;
            }
        }

        for (int m = 0 ; m < instance.numMachines; m++) {
            for (int j = 0; j < instance.numJobs-1; j++) {
                //jbn.jobs[jbn.nextToSet++] = k;
                int j2 = j+1;
                int t = instance.task_with_machine(j,m);
                int t2 = instance.task_with_machine(j2,m);
                if(sched.startTime(j,t) > sched.startTime(j+1,t2)){
                    JobOrder[m][j] = j2;
                    JobOrder[m][j2] = j;
                }
            }
        }

        for (int m = 0 ; m < instance.numMachines; m++) {
            for (int j = 0; j < instance.numJobs; j++) {
                jbn.jobs[jbn.nextToSet++] = JobOrder[m][j];
            }
        }

        return jbn;

    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOfRange(jobs,0, nextToSet));
    }
}
