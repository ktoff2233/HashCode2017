package main.java;

import java.io.*;
import java.util.*;



public class ReadInput {
    List<Solution> samples; 
    List<Solution> greatest_solutions;
    public ReadInput() {
        samples = new ArrayList<>();
        greatest_solutions = new ArrayList<>();
    }
    /**
     * @Brief   Modifies the samples to create a list of randomly generated initials samples for a total of the specified values
     *          Allow for the choice of an all zeroes solution
     *          
     * @param data: holds information on input file
     * @param read: is used for fitness and modifies its samples array
     * @param zeroes: dictates whether an all zeroes solution should be added
     */
    public void generateSamplePop(InputData data, ReadInput read,int total_samples, boolean zeroes){
        Random rand = new Random();
        int totalSamples = 0;
        if (zeroes){
            int[][] all_zero = new int[data.num_caches][data.num_videos];
            for(int i = 0; i< data.num_caches;i++){
                for(int j =0; j< data.num_videos;j++){
                    all_zero[i][j] = 0;
                }
            }
            samples.add(new Solution(all_zero, 0));
            totalSamples++;
        }
        
        while(totalSamples < total_samples){
            Solution sample = new Solution(data.num_caches, data.num_videos);
            for(int i = 0; i < sample.sol.length; i++){
                for(int j = 0 ; j< sample.sol[0].length; j++){
                    sample.sol[i][j] = rand.nextInt(2);
                    if(!isValidRow(sample.sol[i], data)){
                        sample.sol[i][j] = (sample.sol[i][j] == 1? 0: 1);
                        break;
                    }
                }
            }
            sample.fitness = read.fitness(sample.sol, data, false);
            totalSamples++;
            samples.add(sample);
        }
    }
    /**
     * @Brief checks if the values in a row exceed the cache limit
     * @return boolean, true if valid else false
     */
    public boolean isValidRow(int[] single_cache, InputData data){
        if(single_cache == null || single_cache.length == 0){
            return false;
        }
        int sum_of_cache_vids;
        sum_of_cache_vids =0;
            for(int i = 0; i< single_cache.length; i++){
                if(single_cache[i] == 1){
                    sum_of_cache_vids += data.video_size_desc[i];
                }
                if(sum_of_cache_vids > data.cache_size){
                    return false;
                }
            }
        return true;
    }
    /**
     * @Brief checks an entire 2d solution array to see if any cache size has videos that exceed the size
     * @param solution_cache is the passed 2d array to check if its a valid solution
     * @param data
     * @return boolean, true if valid else false
     */
    public boolean isValidSolution(int[][] solution_cache,InputData data ){
        if(solution_cache == null || solution_cache.length == 0){
            return false;
        }
        //loops through each row checking if its valid
        for(int i = 0; i < solution_cache.length; i++){
            if(!isValidRow(solution_cache[i], data)){
                return false;
            }
        }
        return true;
    }

    /**
     * @brief   Calculates the fitness of a solution based on the latency saved from getting a video from
     *          the cache versus data center
     *          
     *          
     * @param solution_caches
     * @param data
     * @checkSolution dictates whether the passed solution should be checked for validity 
     * @return the fitness of the solution
     */
    public int fitness(int[][] solution_caches, InputData data, boolean checkSolution) {
        if(solution_caches == null || (checkSolution && !isValidSolution(solution_caches, data))){
            return -1;
        }        
        int endpoint_to_dc_latency;
        double num_of_requests =0;
        double sum_of_gains = 0;
        int current_requests = 0;
        int smallest_cache = 0;
        List<Integer> cache_list_latency;
        List<Integer> caches;
        //goes through each video
        for (Map.Entry<String,String> vid_req: data.video_ed_request.entrySet()){
            //get the current amount of requests and latency
            current_requests = Integer.parseInt(vid_req.getValue());
            String[] ids_str = vid_req.getKey().split(",");
            int[] ids = {Integer.parseInt(ids_str[0]), Integer.parseInt(ids_str[1])};
            //adds to the total amount of requests
            num_of_requests += current_requests;
            endpoint_to_dc_latency = data.ep_to_dc_latency.get(ids[1]);

            cache_list_latency = data.ep_to_cache_latency.get(ids[1]);
            caches = data.ed_cache_list.get(ids[1]);
            //gets the smallest latency from all caches connected to the endpoint
            smallest_cache = endpoint_to_dc_latency;
            for(int i = 0; i < solution_caches.length; i++){
                if(solution_caches[i][ids[0]] == 1 && caches.contains(i)){
                    smallest_cache = Math.min(smallest_cache, cache_list_latency.get(i));
                }
            }
            //calculates the gain for that video
            double gains = (endpoint_to_dc_latency - smallest_cache) * current_requests;
            sum_of_gains += gains;
        }
        double out = (sum_of_gains/num_of_requests) * 1000;
        return (int) out;
        
    }
    /**
     * @Brief   reads an input file and places the appropriate information into the InputData class
     * @param filename
     * @param storage
     * @throws IOException
     */
    public void readGoogle(String filename, InputData storage) throws IOException {
             
        BufferedReader fin = new BufferedReader(new FileReader(filename));
    
        // gets the amount of each type
        String system_desc = fin.readLine();
        //System.out.println(system_desc);
        String[] system_desc_arr = system_desc.split(" ");
        int number_of_videos = Integer.parseInt(system_desc_arr[0]);
        int number_of_endpoints = Integer.parseInt(system_desc_arr[1]);
        int number_of_requests = Integer.parseInt(system_desc_arr[2]);
        int number_of_caches = Integer.parseInt(system_desc_arr[3]);
        int cache_size = Integer.parseInt(system_desc_arr[4]);
    
        Map<String, String> video_ed_request = new HashMap<String, String>();
        // gets the video sizes
        String video_size_desc_str = fin.readLine();
        //System.out.println(video_size_desc_str);
        String[] video_size_desc_arr = video_size_desc_str.split(" ");
        int[] video_size_desc = new int[video_size_desc_arr.length];
        for (int i = 0; i < video_size_desc_arr.length; i++) {
            video_size_desc[i] = Integer.parseInt(video_size_desc_arr[i]);
        }
        //holds the caches for an endpoint
        List<List<Integer>> ed_cache_list = new ArrayList<List<Integer>>();
        //holds the latency from an endpoint to a data center
        List<Integer> ep_to_dc_latency = new ArrayList<Integer>();
        //holds the latency from an endpoin to a cache
        List<List<Integer>> ep_to_cache_latency = new ArrayList<List<Integer>>();

        
        for (int i = 0; i < number_of_endpoints; i++) {
            ep_to_dc_latency.add(0);
            ep_to_cache_latency.add(new ArrayList<Integer>());
    
            String[] endpoint_desc_arr = fin.readLine().split(" ");
            int dc_latency = Integer.parseInt(endpoint_desc_arr[0]);
            int number_of_cache_i = Integer.parseInt(endpoint_desc_arr[1]);
            ep_to_dc_latency.set(i, dc_latency);
    
            for (int j = 0; j < number_of_caches; j++) {
                ep_to_cache_latency.get(i).add(0);
            }
    
            List<Integer> cache_list = new ArrayList<Integer>();
            for (int j = 0; j < number_of_cache_i; j++) {
                String[] cache_desc_arr = fin.readLine().split(" ");
                int cache_id = Integer.parseInt(cache_desc_arr[0]);
                int latency = Integer.parseInt(cache_desc_arr[1]);
                cache_list.add(cache_id);
                ep_to_cache_latency.get(i).set(cache_id, latency);
            }
            ed_cache_list.add(cache_list);
        }
    
        for (int i = 0; i < number_of_requests; i++) {
            String[] request_desc_arr = fin.readLine().split(" ");
            String video_id = request_desc_arr[0];
            String ed_id = request_desc_arr[1];
            String requests = request_desc_arr[2];
            video_ed_request.put(video_id + "," + ed_id, requests);
        }
        storage.num_videos = number_of_videos;
        storage.num_endpoints =number_of_endpoints;
        storage.num_requests = number_of_requests;
        storage.num_caches = number_of_caches;
        storage.cache_size = cache_size;
        storage.video_size_desc = video_size_desc;
        storage.ep_to_dc_latency = ep_to_dc_latency;
        storage.ep_to_cache_latency = ep_to_cache_latency;
        storage.ed_cache_list = ed_cache_list;
        storage.video_ed_request = video_ed_request;

        fin.close();
     
     }
    /**
     * @Brief   runs hill climbing from multiple initial points an returns the greatest fitness from all runs allows
     *          for the choice of simulated annealing
     * @param storage
     * @param greater_vals allows for the choice of if neighbouring solutions greater than should only be used or greater and equal
     * @param max_plateau allows for the choice of when to stop if the same fitness is continuously being attained
     */
    public void randomRestartHillClimbing(InputData storage, boolean varied_hillclimb, boolean greater_vals,int max_plateau, boolean first_choice){
        Solution best_sol = new Solution(storage.cache_size, storage.num_videos);
        for(Solution sample: samples){
            HillClimbing state = new HillClimbing(storage, this, sample);
            state.hillClimb(varied_hillclimb, greater_vals, max_plateau, first_choice);
            
            if(state.curr_best_sol.getFitness() > best_sol.getFitness()){
                best_sol.fitness = state.curr_best_sol.getFitness();
                best_sol = state.curr_best_sol;
            }
        }
        greatest_solutions.add(best_sol);
 
    }
   public String printSols(Solution cur){
        
        String result = "";
            for(int i =0; i < cur.sol.length; i++){
                for(int j = 0; j<cur.sol[i].length; j++){
                    result += cur.sol[i][j] + ", ";
                }
                result += "\n";
            }
            result += "\n\n";
        
        return result;
    }
    public static void main(String[] args) throws IOException {  
        ReadInput read = new ReadInput();
        InputData data_storage = new InputData();
        read.readGoogle("input/me_at_the_zoo.in", data_storage);
        
        read.generateSamplePop(data_storage, read, 50,true);
        long hillclimb_start = System.nanoTime();
        read.randomRestartHillClimbing(data_storage, false,true, 10, false);
        long hillclimb_end = System.nanoTime();
        System.out.println(read.printSols(read.greatest_solutions.get(0)));
        long hillclimb_runtime = (hillclimb_end - hillclimb_start)/1000000;
        System.out.println(read.greatest_solutions.get(0).getFitness());
        System.out.println("Time taken for hillclimbing to run: " + hillclimb_runtime+"ms\n\n");
        read.samples.clear();
        
        read.generateSamplePop(data_storage, read, 320,false);        
        Genetic gen = new Genetic(data_storage, read, 0, new Solution(data_storage.num_caches, data_storage.num_videos));
        long genetic_start = System.nanoTime();
        gen.runGenetic(read.samples, 468);
        long genetic_end = System.nanoTime();
        read.greatest_solutions.add(read.samples.get(0));
        System.out.println(read.printSols(read.greatest_solutions.get(read.greatest_solutions.size()-1)));
        long genetic_runtime = (genetic_end - genetic_start)/1000000;
        System.out.println(read.greatest_solutions.get(read.greatest_solutions.size()-1).getFitness());
        System.out.println("Time taken for genetic to run: " + genetic_runtime+"ms");
        read.samples.clear();
        


    }
}
