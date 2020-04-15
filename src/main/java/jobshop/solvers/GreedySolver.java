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


    //deadline = temps a pas depasser ?

    @Override
    public Result solve(Instance instance, long deadline) {

        ArrayList<Task> al_Task_Realisables = new ArrayList();

        ResourceOrder rso = new ResourceOrder(instance);

        //Init
        for(int j = 0 ; j<instance.numJobs ; j++) {
            for(int t = 1 ; t<instance.numTasks ; t++) {
                if(t == 1){
                    al_Task_Realisables.add(new Task(j,t-1));
                }
            }
        }

        //Boucle
        while(al_Task_Realisables.size() > 0) {

            System.out.println("___Tableau___");
            for(int s = 0 ; s < al_Task_Realisables.size() ; s++) {

                System.out.println(al_Task_Realisables.get(s));
                System.out.println(instance.duration(al_Task_Realisables.get(s)));


            }
            System.out.println("__________");

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

            int resource = instance.machine(job,task);

            System.out.println("--> shortest is");
            System.out.println("job : " + job);
            System.out.println("task : " + task);
            System.out.println("ressource : " + resource);

            rso.addTask(resource,job,task);

            al_Task_Realisables.remove(index_shortest);

            if(instance.numTasks-1 > task){
                al_Task_Realisables.add(new Task(job,task+1));
            }

            System.out.println("\n\n__NEXT LOOP___\n\n");


        }

        System.out.println(rso);
        System.out.println(rso.toSchedule());

        return new Result(instance, rso.toSchedule(), Result.ExitCause.Blocked);
    }

}
