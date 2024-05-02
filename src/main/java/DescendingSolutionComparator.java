package main.java;
import java.util.Comparator;

public class DescendingSolutionComparator implements Comparator<Solution>{
    @Override
    public int compare(Solution sol1, Solution sol2){
        return Integer.compare(sol2.getFitness(), sol1.getFitness());
    }
}