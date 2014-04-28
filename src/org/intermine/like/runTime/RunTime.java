package org.intermine.like.runTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.intermine.like.precalculation.utils.Coordinates;
import org.intermine.like.runTime.utils.Methods;
//import org.apache.log4j.Logger;
import org.intermine.like.runTime.utils.Result;

/**
 *
 * @author selma
 *
 */
public final class RunTime
{

//    private static final Logger LOG = Logger.getLogger(RunTime.class);

    private RunTime() {
        // Don't.
    }

    /**
     * Perform the query and print the rows of results.
     * @param args command line arguments
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
// Input //
        // Search for similar genes to the testSet
        Integer[] testSet = {1007396, 1204707, 1035230};
        long t1 = System.currentTimeMillis();
        // read properties
        Map<Coordinates, String> views = Methods.getProperties();
        long t2 = System.currentTimeMillis();
        System.out.print((t2 - t1) + "ms to read the property file" + "\n");

        // Add similarity matrices
        Map<Coordinates, Integer> addedMat = new HashMap<Coordinates, Integer>();
        for (int i = 0; i < views.size() / 3; i++) {
            long t3 = System.currentTimeMillis();
            // Read in file i
            File file2 = new File("SimilarityMatrix" + views.get(new Coordinates(i, 0)));
            FileInputStream f2 = new FileInputStream(file2);
            ObjectInputStream s2 = new ObjectInputStream(f2);
            HashMap<Coordinates, Integer> normMat = (HashMap<Coordinates, Integer>) s2.readObject();
            s2.close();
            long t4 = System.currentTimeMillis();
            System.out.print((t4 - t3) + "ms to read in the normMat " + i + "\n");

            // Add similarity matrices
            addedMat = Methods.addMatrices(addedMat, normMat);
            long t5 = System.currentTimeMillis();
            System.out.print((t5 - t4) + "ms to add the matrices " + i + "\n");
            normMat = new HashMap<Coordinates, Integer>();

//            File similarityMatrix = new File("addedMat" + views.get(new Pair(i,0)));
//            FileOutputStream f = new FileOutputStream(similarityMatrix);
//            ObjectOutputStream s = new ObjectOutputStream(f);
//            s.writeObject(addedMat);
//            s.close();
        }

//        System.out.print("\n");
//        for (int i = 0; i < 40; i++) {
//            for (int j = 0; j < 40; j++) {
//                System.out.print(addedMat.get(new Pair(i, j)) + " ");
//            }
//            System.out.print("\n");
//        }
        long t6 = System.currentTimeMillis();
        Map<Integer, Map<Integer, Map<Integer, Integer>>> similarSet =
                Methods.findSimilarSet(addedMat, testSet);

//         File similarityMatrix = new File("similarSet");
//        FileOutputStream f = new FileOutputStream(similarityMatrix);
//        ObjectOutputStream s = new ObjectOutputStream(f);
//        s.writeObject(similarSet);
//        s.close();

        Integer[][] totalRatingSet = Methods.getTotalRating(similarSet);

        totalRatingSet = Methods.order(totalRatingSet);

//         File order1 = new File("order");
//        FileOutputStream f1 = new FileOutputStream(order1);
//        ObjectOutputStream s1 = new ObjectOutputStream(f1);
//        s1.writeObject(similarSet);
//        s1.close();

        long t7 = System.currentTimeMillis();
        System.out.print((t7 - t6) + "ms to find the most similar genes to the testSet\n");
        addedMat = new HashMap<Coordinates, Integer>();

        long t9 = 0;
        Map<Coordinates, ArrayList<Integer>> addedCommonMat = new HashMap<Coordinates, ArrayList<Integer>>();
        for (int i = 0; i < views.size() / 3; i++) {
            long t8 = System.currentTimeMillis();
            File file1 = new File("CommonItems" + views.get(new Coordinates(i, 0)));
            FileInputStream f2 = new FileInputStream(file1);
            ObjectInputStream s2 = new ObjectInputStream(f2);
            Map<Coordinates, ArrayList<Integer>> commonMat =
                    (Map<Coordinates, ArrayList<Integer>>) s2.readObject();
            s2.close();
            t9 = System.currentTimeMillis();
            System.out.print((t9 - t8) + "ms to read in the commonItems file " + i + "\n");

            addedCommonMat = Methods.addCommonMat(addedCommonMat, commonMat);

//            File similarityMatrix3 = new File("addedCommonMat" + views.get(new Pair(i,0)));
//            FileOutputStream f3 = new FileOutputStream(similarityMatrix3);
//            ObjectOutputStream s3 = new ObjectOutputStream(f3);
//            s3.writeObject(addedCommonMat);
//            s3.close();

            commonMat = new HashMap<Coordinates, ArrayList<Integer>>();
        }

        Map<Integer, Map<Integer, ArrayList<Integer>>> commonItems =
                Methods.getCommonItems(addedCommonMat, testSet);
        long t10 = System.currentTimeMillis();
        System.out.print((t10 - t9) + "ms to find the common items " + "\n");

//        File similarityMatrix3 = new File("commonItems");
//        FileOutputStream f3 = new FileOutputStream(similarityMatrix3);
//        ObjectOutputStream s3 = new ObjectOutputStream(f3);
//        s3.writeObject(commonItems);
//        s3.close();

        long t11 = System.currentTimeMillis();
        System.out.print("\n-> " + (t11 - t1) + "ms for the run time calculations" + "\n");

// Output //
        Result result = new Result(totalRatingSet, similarSet, commonItems);

//        Integer[][] resultGenes = result.getMostSimilarGenes();
//        File similarityMatrix2 = new File("resultGenes");
//        FileOutputStream f2 = new FileOutputStream(similarityMatrix2);
//        ObjectOutputStream s2 = new ObjectOutputStream(f2);
//        s2.writeObject(resultGenes);
//        s2.close();
//
//        Map<Integer, Map<Integer, ArrayList<Integer>>> resultItems = result.getCommonItems();
//        File similarityMatrix3 = new File("resultItems");
//        FileOutputStream f3 = new FileOutputStream(similarityMatrix3);
//        ObjectOutputStream s3 = new ObjectOutputStream(f3);
//        s3.writeObject(resultItems);
//        s3.close();

        System.out.print("\ntotalRatingSet:\n");
        for (int i = 0; i < totalRatingSet.length; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.print(totalRatingSet[i][j] + " ");
            }
            System.out.print("\n");
        }

        System.out.print("\nsimilarSet:\n");
        for (int i = 0; i < totalRatingSet.length; i++) {
//            System.out.print(entry.getKey() + " ");
            Map<Integer, Map<Integer, Integer>> tmp = similarSet.get(totalRatingSet[i][0]);
            for (Map.Entry<Integer, Map<Integer, Integer>> entry2 : tmp.entrySet()) {
                System.out.print(entry2.getKey() + " because: ");
                Map<Integer, Integer> tmp2 = entry2.getValue();
                for (Map.Entry<Integer, Integer> entry3 : tmp2.entrySet()) {
                    System.out.print(entry3.getKey() + " with rating " + entry3.getValue() + "; ");
                }
            }
            System.out.print("\n");
        }

//        System.out.print("\nsimilarSet: " + similarSet[0].length + " " + similarSet.length );
        Map<Integer, ArrayList<Integer>> result1 = result.getCommonItems(1038571);
        System.out.print("\nklick on 1038571: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result1.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result2 = result.getCommonItems(1112303);
        System.out.print("\nklick on 1112303: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result2.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result3 = result.getCommonItems(1069318);
        System.out.print("\nklick on 1069318: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result3.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result4 = result.getCommonItems(1204707);
        System.out.print("\nklick on 1204707: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result4.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result5 = result.getCommonItems(1007396);
        System.out.print("\nklick on 1007396: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result5.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result6 = result.getCommonItems(1037459);
        System.out.print("\nklick on 1037459: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result6.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result7 = result.getCommonItems(1079460);
        System.out.print("\nklick on 1079460: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result7.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result8 = result.getCommonItems(1355928);
        System.out.print("\nklick on 1355928: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result8.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result9 = result.getCommonItems(1079057);
        System.out.print("\nklick on 1079057: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result9.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result10 = result.getCommonItems(1018517);
        System.out.print("\nklick on 1018517: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result10.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result11 = result.getCommonItems(1014174);
        System.out.print("\nklick on 1014174: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result11.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result12 = result.getCommonItems(1021573);
        System.out.print("\nklick on 1021573: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result12.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
        Map<Integer, ArrayList<Integer>> result13 = result.getCommonItems(1076450);
        System.out.print("\nklick on 1076450: \n");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : result13.entrySet()) {
            System.out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
    }
}
