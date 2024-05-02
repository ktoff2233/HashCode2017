package main.java;


public class Solution{
    int[][] sol;
    int fitness;
    public Solution(int num_caches,int num_vids){
        sol = new int[num_caches][num_vids];
        fitness = 0;
    }
    public Solution(int[][] input_array,int fitness){
        sol = input_array;
        this.fitness = fitness;
    }
    public int getFitness(){
        return this.fitness;
    }

}
