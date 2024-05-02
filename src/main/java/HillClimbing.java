package main.java;

import java.util.*;
public class HillClimbing{
    InputData data;
    ReadInput read;
    Solution curr_best_sol;


    public HillClimbing(InputData data, ReadInput read, Solution input_solution){
        this.data = data;
        this.read = read;
        curr_best_sol = input_solution;

    }
    // Helper method to clone a 2D array
    public static int[][] fullClone(int[][] original) {
        int[][] clone = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            clone[i] = original[i].clone();
        }
        return clone;
    }
    //returns optimal solution
    public Solution getOptimalSol(){
        return curr_best_sol;
    }
    /**
     * @Brief runs either steepest hill climb or an alternate version of stochastic hill climb based on input variable
     *        it runs until an optimal solution has been reached or is on a plateau
     * @param varied_hillclimb if true allows stochastic hill climbing else is steepest hill climb
     * @param greater vals if false allows for neighbouring solutions with the same score as the current solution to be considered else only
     *        greater values are considered
     */
    public void hillClimb(boolean varied_hillclimb, boolean greater_vals,int max_plateau, boolean first_choice){
        Random random = new Random();
        List<Solution> solutionsList = new ArrayList<>();
        solutionsList.add(curr_best_sol);
        int plateau_count = 0;
        int rand_index = 0;
        while(!solutionsList.isEmpty() && plateau_count != max_plateau){
            //Clears list for the new neighbours
            solutionsList.clear();
            //Gets all neighbours of the current solution
            generateNeighbouringOptimalSolutions(curr_best_sol, solutionsList, true, first_choice);
            //Sorts the neighbours in descending order
            Collections.sort(solutionsList, new DescendingSolutionComparator());
            //if varied hillclimb is set stochastic hill climb occurs
            if(varied_hillclimb && !first_choice){
                //all neighbours outside of the margin of difference are removed from the neighbours list
                removeLeastOptimalNeighbours(curr_best_sol, solutionsList);
            }
            //if neighbours are generated
            if(!solutionsList.isEmpty()){
                if(varied_hillclimb){
                    rand_index = random.nextInt(solutionsList.size());
                }
                //if the current fitnessis greater than or equal to the current greatest neighbouring solution we are on a plateau
                if(solutionsList.get(0).getFitness() <= curr_best_sol.getFitness()){
                    plateau_count++;
                }else{
                    plateau_count = 0;
                    if(varied_hillclimb){
                        curr_best_sol = solutionsList.get(rand_index);
                    }else{
                        curr_best_sol = solutionsList.get(0);
                    }
                }
                
                
            }
        }
    }
    /**
     * @Brief removes all neighbours that ar outside of the margin of difference from the optimal solution
     * @param curr_sol is the current optimal solution
     * @param curr_solutions contains all current neighbours
     *  prerequisite is tha the curr solutions must be sorted in descending order
     */
    public void removeLeastOptimalNeighbours(Solution curr_sol, List<Solution> curr_solutions){
        if(!curr_solutions.isEmpty()){        
            double gain_margin = curr_sol.getFitness() * 0.1;
            for (int i = curr_solutions.size()-1; i > 0;i--){
                if((curr_sol.getFitness() - curr_solutions.get(i).getFitness()) > gain_margin){
                    curr_solutions.remove(i);
                }else{
                    break;
                }
         }
        }
    }
    /**
     * @Brief Generates all neigbouring solutions to the current solution and adds them to the passed list if greater values should only be accepted
     *        greater vals is set to true else greater and similar fitness solutions are chosen
     * @param curr_sol is the current greatest solution
     * @param curr_solutions is the list of neighbours
     * @param greater_vals if true only solutions with greater fitnesses are chosen
     */
    public void generateNeighbouringOptimalSolutions(Solution curr_sol, List<Solution> curr_solutions, boolean greater_vals, boolean first_choice){
        int tmp_fitness = 0;
        int[][] tmp_array;
        //goes through the current solution inverting each position and adding it as a neighbour if its a valid solution
        for(int i = 0; i < curr_sol.sol.length; i++){
            for(int j = 0; j<curr_sol.sol[i].length; j++){
                //Flips value to 0 or 1 based on current value to get all permutations from the current solution
                curr_sol.sol[i][j] = (curr_sol.sol[i][j] == 1? 0:1);  
                //verifies the new solution in the added row is valid before checing the entire array              
                if(read.isValidRow(curr_sol.sol[i], data)){
                    tmp_fitness = read.fitness(curr_sol.sol, data, false);
                //depending on if greater values is set solutions with only greater fitnesses may be chosen and added to the neighbours
                    if (greater_vals){    
                        if(tmp_fitness > curr_sol.getFitness()){
                                tmp_array = fullClone(curr_sol.sol);
                                curr_solutions.add(new Solution(tmp_array, tmp_fitness));  
                                if(first_choice){
                                    curr_sol.sol[i][j] = (curr_sol.sol[i][j] == 1? 0:1);
                                    return;
                                }                                       
                        }
                    }else{
                        if(tmp_fitness >= curr_sol.getFitness()){
                            tmp_array = fullClone(curr_sol.sol);
                            curr_solutions.add(new Solution(tmp_array, tmp_fitness));  
                            if(first_choice){
                                curr_sol.sol[i][j] = (curr_sol.sol[i][j] == 1? 0:1);
                                return;
                            }                                       
                    }
                    }
                    
                }
                //inverts back the position as to preserve the parent
                curr_sol.sol[i][j] = (curr_sol.sol[i][j] == 1? 0:1);
            }
       }
    }


}