package main.java;
import java.util.Comparator;

public class AscendingSolutionComparator implements Comparator<Solution>{
    @Override
    public int compare(Solution sol1, Solution sol2){
        return Integer.compare(sol1.getFitness(), sol2.getFitness());
    }
}