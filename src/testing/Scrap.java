package testing;

import java.util.ArrayList;

public class Scrap {

    public static void main(String[] args) {
        ArrayList<int[]> hello = new ArrayList<int[]>();
        hello.add(new int[]{1, 2});
        hello.add(new int[]{1, 2});
        
        int[][] sup = (int[][]) hello.toArray();
    }

}
