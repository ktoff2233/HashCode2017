package main.java;

import java.util.*;

public class Genetic {
    InputData data;
    ReadInput read;
    int best_fit;
    Solution best_sol;
    
    public Genetic(InputData data,ReadInput read,int best_fit,Solution best_sol){
        this.data = data;
        this.read = read;
        this.best_fit = best_fit;
        this.best_sol = best_sol;
        
    }
    
    /**
     * @Brief function modifies an inputted wheel array to assign probabilities of obtaining the index at that position
     *      requires the curr solutions to be sorted in reverse order before being called
     * @param curr_solutions: is the list of all solutions for the current generation
     * @param wheel: is the array to which probabilities will be written to
     */
    public void generateRouletteWheelProb(List<Solution> curr_solutions, double[] wheel){
        double total_fitness = 0;


        for (Solution solution: curr_solutions){
            total_fitness += solution.getFitness();
        }
        
        double sum = 0;
        int temp_fit = 0;
        // The arraylist is in reverse order so we assign the lowest probability starting from the end of the list
        for(int i =wheel.length - 1; i >= 0; i--){
            temp_fit = curr_solutions.get(i).getFitness();
            wheel[i] = (sum + ( temp_fit/ total_fitness));
            sum += (temp_fit/ total_fitness);           
        }
    }
    
    /**
     * @Brief This modifies the solutions of the current generation by crossing over different solutions.
     *        The different solutions are chosen at random with the probability wheel dictating the index it lands on
     *        Multiple solutions are chosen until we have enough such that each solution provides a row for one child
     *        Said child is created as an amalgamation of its parents for a total of 50 childrens 
     * @param curr_solutions: is the list of all solutions for the current genration
     * @param parents_size: is the array to which probabilities will be written to
     * prerequisite is curr_solutions must be sorted in reverse order
     */
    public void diagonalCrossover(List<Solution> curr_solutions,int parents_size){
        int row_size = data.num_caches;
        int col_size = data.num_videos;
        double[] wheel = new double[curr_solutions.size()];
        //assigns a probability to each index in the solutions list
        generateRouletteWheelProb(curr_solutions, wheel);
        Random rand = new Random();
        List<Integer> chosen_indexes = new ArrayList<>();
        double rand_index = 0;
        //loops until another parents_size has been added
        while(curr_solutions.size() != (parents_size * 2)){
            chosen_indexes.clear();
            //Gets the random indexes to be chosen for the new child
            while(chosen_indexes.size() != row_size){
                rand_index = rand.nextDouble();
                //increases the probability to exclude the lowest fitness values
                while(rand_index < 0.3){
                    rand_index += 0.1;
                }
                //gets the relavent index based on the probability
                for(int i = 0; i < wheel.length; i++){
                    if(rand_index >= wheel[i]){                        
                        chosen_indexes.add(i);
                        break;
                    }
                }


            }
            //creates the child by getting the random parent dictated by the chosen indexes and then clones the ith parent
            //row to the ith child row
            int[][] child = new int[row_size][col_size];
            for(int i = 0; i < row_size; i++){
                child[i] = curr_solutions.get(chosen_indexes.get(i)).sol[i].clone();
            }
            Solution temp = new Solution(child, read.fitness(child,data, false));
            curr_solutions.add(temp);

            
        }  
    }
    /**
     * @Brief converts an integer array to a string
     * @param int_array
     * @return a string of the passed array 
     */
    public String intArrayToStr(int[] int_array){
        StringBuilder sb = new StringBuilder();
        for(int val: int_array){
            sb.append(val);
        }
        return sb.toString();
    }
   /**
    * @Brief applies the string mutation back to the integer array
    * @param curr
    * @param new_row_vals is the mutated string
    * @param row_index is the row to insert the mutated string
    * @return if the mutation was successfully carried out
    */
    public boolean applyMutation(Solution curr, String new_row_vals, int row_index){
        if(curr == null || new_row_vals.length() < curr.sol[0].length){
            return false;
        }
        //copies the entire string to the solution row
        for(int i = 0; i < new_row_vals.length(); i++){
            curr.sol[row_index][i] = new_row_vals.charAt(i) - '0';
        }
        return true;
    }
    /**
     * @Brief calculates the maximum optimal size of videos that can be mutated
     * @return the length of optimal substring size
     */
    public int calculateSubstringLength(){
        double total = 0; 
        for(int i = 0; i < this.data.video_size_desc.length;i++){
            total += this.data.video_size_desc[i];
        }
        double avg = total/this.data.video_size_desc.length;
        return (int)(data.cache_size/avg);
    }
    /**
     * @Brief places the mutated substring in the whole string 
     * @param orig_string
     * @param substring
     * @param insert_index
     * @param substring_pos
     * @param substring_length
     * @return mutated string is returned
     */
    public String shiftedSubString(String orig_string,String substring, int insert_index, int substring_pos, int substring_length){
        String out = "";
        if(insert_index <= substring_pos){
            out = orig_string.substring(0, insert_index) + substring 
                +orig_string.substring(insert_index, substring_pos) + 
                orig_string.substring(substring_pos + substring_length,orig_string.length());
        }else{
            out = orig_string.substring(0, substring_pos) + orig_string.substring(substring_pos + substring_length, insert_index)
                    + substring + orig_string.substring(insert_index, orig_string.length());
        }
        return out;
    }
    /**
     * @Brief performs inverted displacement mutation on the specified row and returns if it was a vaild mutation
     * @param curr_sol
     * @param row_index is the row on which the mutation occurs
     * @param substring_length is the max size a substring can be
     * @return if the mutation is possible and valid true is returned else false
     */
    public boolean invertedDisplacementMutation(Solution curr_sol, int row_index, int substring_length){
        Random rand = new Random();
        if(substring_length > curr_sol.sol[0].length){
            return false;
        }
        //gets the solution row as a string
        String row_string = intArrayToStr(curr_sol.sol[row_index]);
        String to_shift_substring = "";

        if(substring_length != curr_sol.sol[0].length){
            String temp_str = "";
            //gets the position to get the substring from
            int substring_pos = rand.nextInt(row_string.length() - substring_length);
            temp_str = row_string.substring(substring_pos, substring_pos + substring_length);
            //reverses the substring
            temp_str = new StringBuilder(temp_str).reverse().toString();
            
            int rand_index = 0;
            //gets an index that is outside of the substring position if the random index is within
            do{
                rand_index = rand.nextInt(row_string.length()+1);
                //System.out.println(rand_index);
            }while(rand_index >= substring_pos && rand_index < substring_pos + substring_length);
            //gets the new string with the shift and mutation
            to_shift_substring = shiftedSubString(row_string, temp_str, rand_index, substring_pos, substring_length);
        }else{
            //if the size of the array is the size of the recommended substring length then we just reverse all the elements
            to_shift_substring = new StringBuilder(row_string).reverse().toString();
        }
        applyMutation(curr_sol, to_shift_substring, row_index);
        //checks whether the mutation was valid 
        return read.isValidRow(curr_sol.sol[row_index], data);
          
    }
    /**
     * @Brief peforms inverted displacement mutation on the current solutions sub population
     * @param curr_solutions
     * @param idm_prob sets the probability that the mutation occurs
     * @param child_start_pos gets the positon to start the idm on 
     * @param child_size gets the size of the population idm should be performed
     * @param substring_length is the size of the optimal mutation length
     */
    public void childPopulationIDM(List<Solution> curr_solutions, double idm_prob,int child_start_pos, int child_size, int substring_length){
        Random rand = new Random();
        double mutation_prob = 0;
        //loops through the size of a subset of the population
        for(int i = 0; i<child_size;i++){
            //loops through each cache
            for(int j = 0; j< data.num_caches;j++){
                mutation_prob = rand.nextDouble();
                //runs IDM if mutation probabilit is met
                if(mutation_prob <= idm_prob){
                    int[] tmp_array = curr_solutions.get(i + child_start_pos).sol[j].clone();
                    boolean valid_mutation = invertedDisplacementMutation(curr_solutions.get(i + child_start_pos), j, substring_length);
                    //checks if the idm was valid and if it was the fitness is checked 
                    if (valid_mutation){
                        int new_fit = read.fitness(curr_solutions.get(i+child_start_pos).sol, data, false);
                        if(new_fit > 0){
                            curr_solutions.add(new Solution(HillClimbing.fullClone(curr_solutions.get(i+child_start_pos).sol), new_fit));
                        }
                    }
                    curr_solutions.get(i+child_start_pos).sol[j] = tmp_array;
                }
                
            }
        }
    }
    /**
     * @Brief runs basic inversion mutation on an input array and mutates each position based on if the probability is met
     * @param input_arr is the solution to modify
     * @param mutation_prob is the probability to invert a bit
     * @param parent_fit 
     * @return if the mutated solution has a different valid fitness it is returned else 0 or -1 is returned 
     */
    public int basicMutation(int[][] input_arr, double mutation_prob, int parent_fit){
        Random rand = new Random();
        boolean mutated = false;
        //Loops through each cache
        for(int i = 0; i < input_arr.length; i++){
            //loops through each video in the array 
            for(int j = 0; j < input_arr[0].length; j++){
                //if the random function is less than the probability we invert the value
                if(rand.nextDouble() < mutation_prob){
                    //inverts the position
                    input_arr[i][j] = (input_arr[i][j] == 1? 0:1);
                    //checks if the change lead to an invalid solution
                    if(!read.isValidRow(input_arr[i], data)){
                        return -1;
                    }
                    mutated = true;
                }
                
            }
        }
        int tmp_fitness = 0;
        //checks if mutated and gets fitness if is
        if(mutated){
            tmp_fitness = read.fitness(input_arr, data, false);
        }
        //if the fitness is equal to that of the current solution a 0 is returned
        return (parent_fit == tmp_fitness?0:tmp_fitness);

    }
    /**
     * @Brief runs basic inversion mutation on the specified position of the solutions array for the specified size
     * @param curr_solutions is the current generation
     * @param mutation_prob is the probability a mutation occurs
     * @param parent_start_pos dictates where the parents are in the list
     * @param parent_size dictates all the parents to operate on
     */
    public void parentPopulationMutation(List<Solution> curr_solutions, double mutation_prob, int parent_start_pos, int parent_size){
        int parent_fit;
        //Goes through the entire specified section of the solutions
        for(int i = parent_start_pos; i< parent_size;i++){
            //clones the solution to a local array 
            int[][] temp = HillClimbing.fullClone(curr_solutions.get(i).sol);
            parent_fit = curr_solutions.get(i).getFitness();
            //attempts to apply mutation
            int tmp_fitness = basicMutation(temp, mutation_prob, parent_fit);
            //if the mutation is different from the current solution and is different it is added to the list
            if(tmp_fitness > 0){
                curr_solutions.add(new Solution(temp, tmp_fitness));
            }
        }
    }
    /**
     * @Brief culls the current least optimal solutions by removing all values not in the population size to keep
     * @param curr_generation is the generation to operate on
     * @param population_to_keep dictates the size of a generation
     */
    public void removeLeastOptimalSolutions(List<Solution> curr_generation, int population_to_keep){
        //sorts the current generation in descending order
        Collections.sort(curr_generation, new DescendingSolutionComparator());
        //removes each value from the end until the population to keep is reached
        for(int i = curr_generation.size()-1; i >=population_to_keep;i--){
            curr_generation.remove(i);
        }
    }
    /**
     * @Brief runs the genetic algorithm with diagonal crossover and then idm on the child population and inversion mutation on the parent population
     * @param sample_pop
     * @param max_generations dictates the max amount of generations
     */
    public void runGenetic(List<Solution> sample_pop,int max_generations){
        int generation_count = 0;
        int parent_size = sample_pop.size();
        double basic_mutation_prob = 0.0057;
        double IDM_prob = 0.623;
        
        //calculates the recommended substring length
        int substring_length = calculateSubstringLength();
        //sorts the list reverse descending order
        Collections.sort(sample_pop, new DescendingSolutionComparator());
        //loops through until the max generatiosn is reached
        while(generation_count < max_generations){
            //performs diagonal crossover
            diagonalCrossover(sample_pop, parent_size);
            //applies invesion mutation to the parent populatin and adds it to the solutins
            parentPopulationMutation(sample_pop, basic_mutation_prob, 0, parent_size);
            //applies inverted displacement mutation to the child population
            childPopulationIDM(sample_pop, IDM_prob, parent_size, parent_size, substring_length);
            //culls the current generation
            removeLeastOptimalSolutions(sample_pop, parent_size);
            generation_count++;
        }

    }

}
