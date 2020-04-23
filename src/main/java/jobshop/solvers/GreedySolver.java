package jobshop.solvers;

import java.util.ArrayList;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.time.format.ResolverStyle;

public class GreedySolver implements Solver {

    ResourceOrder rso;

    int priorityRule;

    //1 = SPT = donne priorité à la tâche la plus courte
    //2 = LPT =  donne priorité à la tâche la plus longue
    //3 = SRPT = donne la priorité à la tâche appartenant au job ayant la plus petite durée restante
    //4 = LRPT =  donne la priorité à la tâche appartenantau job ayant la plus grande durée
    //5 = EST_SPT
    //6 = EST_LRPT


    //deadline = temps a pas depasser ?

    public void setPriority(int prio) {
        priorityRule = prio;
    }

    @Override
    public Result solve(Instance instance, long deadline) {

        ArrayList<Task> al_Task_Realisables = new ArrayList();

        deadline = deadline + System.currentTimeMillis();

        ResourceOrder rso = new ResourceOrder(instance);

        //Init
        for (int j = 0; j < instance.numJobs; j++) {
            for (int t = 1; t < instance.numTasks; t++) {
                if (t == 1) {
                    al_Task_Realisables.add(new Task(j, t - 1));
                }
            }
        }

        if (priorityRule == 1) { //SPT

            //Boucle
            while (al_Task_Realisables.size() > 0 && (deadline - System.currentTimeMillis() > 1)) {

                int index_shortest = 0;

                for (int k = 0; k < al_Task_Realisables.size(); k++) {

                    Task current = al_Task_Realisables.get(k);

                    Task shortest = al_Task_Realisables.get(index_shortest);

                    if (instance.duration(shortest) > instance.duration(current)) {
                        index_shortest = k;
                    }
                }

                Task shortest = al_Task_Realisables.get(index_shortest);

                int job = shortest.job;
                int task = shortest.task;

                int resource = instance.machine(job, task);

                rso.addTask(resource, job, task);

                al_Task_Realisables.remove(index_shortest);

                if (instance.numTasks - 1 > task) {
                    al_Task_Realisables.add(new Task(job, task + 1));
                }


            }

        } else if (priorityRule == 2) { //LTP

            //Boucle
            while (al_Task_Realisables.size() > 0 && (deadline - System.currentTimeMillis() > 1)) {

                int index_shortest = 0;

                for (int k = 0; k < al_Task_Realisables.size(); k++) {

                    Task current = al_Task_Realisables.get(k);

                    Task shortest = al_Task_Realisables.get(index_shortest);

                    if (instance.duration(shortest) < instance.duration(current)) {
                        index_shortest = k;
                    }
                }

                Task shortest = al_Task_Realisables.get(index_shortest);

                int job = shortest.job;
                int task = shortest.task;

                int resource = instance.machine(job, task);

                rso.addTask(resource, job, task);

                al_Task_Realisables.remove(index_shortest);

                if (instance.numTasks - 1 > task) {
                    al_Task_Realisables.add(new Task(job, task + 1));
                }

            }

        } else if (priorityRule == 3) { //SRPT

            int timeToEnd[] = new int[instance.numJobs];

            for (int i = 0; i < instance.numJobs; i++) {
                timeToEnd[i] = 0;
                for (int j = 0; j < instance.numTasks; j++) {
                    timeToEnd[i] += instance.duration(i, j);
                }
            }

            while (al_Task_Realisables.size() > 0 && (deadline - System.currentTimeMillis() > 1)) {

                int index_shortest = 0;

                for (int k = 0; k < al_Task_Realisables.size(); k++) {

                    Task current = al_Task_Realisables.get(k);
                    int jobCurrent = current.job;

                    Task shortest = al_Task_Realisables.get(index_shortest);
                    int jobShortest = shortest.job;

                    if (timeToEnd[jobCurrent] < timeToEnd[jobShortest]) {
                        index_shortest = k;
                    }
                }

                Task shortest = al_Task_Realisables.get(index_shortest);

                int job = shortest.job;
                int task = shortest.task;

                int resource = instance.machine(job, task);

                rso.addTask(resource, job, task);

                al_Task_Realisables.remove(index_shortest);
                timeToEnd[job] -= instance.duration(job, task);


                if (instance.numTasks - 1 > task) {
                    al_Task_Realisables.add(new Task(job, task + 1));
                }

            }

        } else if (priorityRule == 4) { //LRPT

            int timeToEnd[] = new int[instance.numJobs];

            for (int i = 0; i < instance.numJobs; i++) {
                timeToEnd[i] = 0;
                for (int j = 0; j < instance.numTasks; j++) {
                    timeToEnd[i] += instance.duration(i, j);
                }
            }


            //Boucle
            while (al_Task_Realisables.size() > 0 && (deadline - System.currentTimeMillis() > 1)) {

                int index_shortest = 0;

                for (int k = 0; k < al_Task_Realisables.size(); k++) {

                    Task current = al_Task_Realisables.get(k);
                    int jobCurrent = current.job;

                    Task shortest = al_Task_Realisables.get(index_shortest);
                    int jobShortest = shortest.job;

                    if (timeToEnd[jobCurrent] > timeToEnd[jobShortest]) {
                        index_shortest = k;
                    }
                }

                Task shortest = al_Task_Realisables.get(index_shortest);

                int job = shortest.job;
                int task = shortest.task;

                int resource = instance.machine(job, task);

                rso.addTask(resource, job, task);

                al_Task_Realisables.remove(index_shortest);
                timeToEnd[job] -= instance.duration(job, task);

                if (instance.numTasks - 1 > task) {
                    al_Task_Realisables.add(new Task(job, task + 1));
                }

            }

        } else if (priorityRule == 5) { //EST_SPT


            //CALCUL DES DATES DE DEBUT
            int startTimes[][] = new int[instance.numJobs][instance.numTasks];

            for (int i = 0; i < instance.numJobs; i++) {
                for (int j = 0; j < instance.numTasks; j++) {
                    if(j==0){
                        startTimes[i][j] = 0;
                    }else{
                        startTimes[i][j] = startTimes[i][j-1] + instance.duration(i,j-1);
                    }
                }
            }

            //Boucle
            while (al_Task_Realisables.size() > 0 && (deadline - System.currentTimeMillis() > 1)) {

                int index_shortest = 0;

                int startTime;

                int best_startTime = 0;

                //calcul du startTime le plus court
                for (int k = 0; k < al_Task_Realisables.size(); k++) {

                    Task current = al_Task_Realisables.get(k);
                    int currentJob = current.job;
                    int currentTask = current.task;

                    startTime = startTimes[currentJob][currentTask];

                    if(k==0){
                        best_startTime = startTime;
                    }else {
                        if (best_startTime > startTime) {
                            best_startTime = startTime;
                        }
                    }
                }

                for (int k = 0; k < al_Task_Realisables.size(); k++) {

                    Task current = al_Task_Realisables.get(k);
                    int currentJob = current.job;
                    int currentTask = current.task;

                    if(startTimes[currentJob][currentTask] == best_startTime){

                        Task shortest = al_Task_Realisables.get(index_shortest);

                        if (instance.duration(shortest) > instance.duration(current)) {
                            index_shortest = k;
                        }

                    }
                }

                Task shortest = al_Task_Realisables.get(index_shortest);

                int job = shortest.job;
                int task = shortest.task;

                int resource = instance.machine(job, task);

                rso.addTask(resource, job, task);

                al_Task_Realisables.remove(index_shortest);

                if (instance.numTasks - 1 > task) {
                    al_Task_Realisables.add(new Task(job, task + 1));
                }


            }

        }else if (priorityRule == 6) { //EST_LRPT

            //CALCUL DES DATES DE DEBUT
            int startTimes[][] = new int[instance.numJobs][instance.numTasks];

            for (int i = 0; i < instance.numJobs; i++) {
                for (int j = 0; j < instance.numTasks; j++) {
                    if (j == 0) {
                        startTimes[i][j] = 0;
                    } else {
                        startTimes[i][j] = startTimes[i][j - 1] + instance.duration(i, j - 1);
                    }
                }
            }
            int timeToEnd[] = new int[instance.numJobs];

            for (int i = 0; i < instance.numJobs; i++) {
                timeToEnd[i] = 0;
                for (int j = 0; j < instance.numTasks; j++) {
                    timeToEnd[i] += instance.duration(i, j);
                }
            }




            //Boucle
            while (al_Task_Realisables.size() > 0 && (deadline - System.currentTimeMillis() > 1)) {

                int index_shortest = 0;

                int startTime;

                int best_startTime = 0;

                //calcul du startTime le plus court
                for (int k = 0; k < al_Task_Realisables.size(); k++) {

                    Task current = al_Task_Realisables.get(k);
                    int currentJob = current.job;
                    int currentTask = current.task;

                    startTime = startTimes[currentJob][currentTask];

                    if(k==0){
                        best_startTime = startTime;
                    }else {
                        if (best_startTime > startTime) {
                            best_startTime = startTime;
                        }
                    }
                }

                for (int k = 0; k < al_Task_Realisables.size(); k++) {

                    Task current = al_Task_Realisables.get(k);
                    int currentJob = current.job;
                    int currentTask = current.task;

                    Task shortest = al_Task_Realisables.get(index_shortest);
                    int jobShortest = shortest.job;

                    if(startTimes[currentJob][currentTask] == best_startTime){

                        if (instance.duration(shortest) > instance.duration(current)) {
                            index_shortest = k;
                        }

                    }
                }

                Task shortest = al_Task_Realisables.get(index_shortest);

                int job = shortest.job;
                int task = shortest.task;

                int resource = instance.machine(job, task);

                rso.addTask(resource, job, task);

                al_Task_Realisables.remove(index_shortest);
                timeToEnd[job] -= instance.duration(job, task);

                if (instance.numTasks - 1 > task) {
                    al_Task_Realisables.add(new Task(job, task + 1));
                }

            }
        }

        return new Result(instance, rso.toSchedule(), Result.ExitCause.Blocked);
    }

}
