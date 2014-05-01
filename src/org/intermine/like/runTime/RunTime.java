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

import org.intermine.like.Response.LikeResult;
import org.intermine.like.precalculation.utils.Coordinates;
import org.intermine.like.runTime.utils.Methods;
//import org.apache.log4j.Logger;

/**
 * Perform the query and print the rows of results.
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
     *
     * @param searchedGenIDs command line arguments
     * @return a list of the similar genes with their ratings. There is the total rating and
     * the pairwise ratings. E.g.:
     * searched gene IDs: 111, 222, 333
     * similar gene IDs:  total rating:  pairwise ratings:
     *         999              9         4 from 111; 3 from 222; 2 from 333
     *         888              8         4 from 111; 4 from 222;
     *         777              7         7 from 222;
     *         666              6         4 from 222; 2 from 333;
     *         000              0         has nothing in common with any of the searched genes.
     *
     * The list is ordered from highest to lowest regarding to the total rating.
     * This answers the question: How similar are the searched and the result genes?
     * Also the result contains the pairwise common item IDs.
     * This answers the question: Why are the searched and the result genes similar?
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static LikeResult calculate(Integer[] searchedGenIDs)
            throws IOException, ClassNotFoundException {
        long t1 = System.currentTimeMillis();
        // read properties
        Map<Coordinates, String> views = Methods.getProperties();
        long t2 = System.currentTimeMillis();
        System.out.print((t2 - t1) + "ms to read the property file" + "\n");

        // Add similarity matrices
        Map<Coordinates, Integer> addedMat = new HashMap<Coordinates, Integer>();
        for (int i = 0; i < views.size() / 4; i++) {
            long t3 = System.currentTimeMillis();
            // Read in file i
            File file2 = new File("build/SimilarityMatrix" + views.get(new Coordinates(i, 0)));
            FileInputStream f2 = new FileInputStream(file2);
            ObjectInputStream s2 = new ObjectInputStream(f2);
            HashMap<Coordinates, Integer> normMat = (HashMap<Coordinates, Integer>) s2.readObject();
            s2.close();
            long t4 = System.currentTimeMillis();
            System.out.print((t4 - t3) + "ms to read in the normMat " + i + "\n");

            System.out.print("\n");
            for (int k = 0; k < 40; k++) {
                for (int j = 0; j < 40; j++) {
                    System.out.print(normMat.get(new Coordinates(k, j)) + " ");
                }
                System.out.print("\n");
            }

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
//                System.out.print(addedMat.get(new Coordinates(i, j)) + " ");
//            }
//            System.out.print("\n");
//        }
        long t6 = System.currentTimeMillis();
        Map<Integer, Map<Integer, Map<Integer, Integer>>> similarSet =
                Methods.findSimilarSet(addedMat, searchedGenIDs);

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
        System.out.print((t7 - t6) + "ms to find the most similar genes to the searchedGenIDs\n");
        addedMat = new HashMap<Coordinates, Integer>();

        long t9 = 0;
        Map<Coordinates, ArrayList<Integer>> addedCommonMat =
                new HashMap<Coordinates, ArrayList<Integer>>();
        for (int i = 0; i < views.size() / 4; i++) {
            long t8 = System.currentTimeMillis();
            File file1 = new File("build/CommonItems" + views.get(new Coordinates(i, 0)));
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
                Methods.getCommonItems(addedCommonMat, searchedGenIDs);
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
        LikeResult result = new LikeResult(totalRatingSet, similarSet, commonItems);

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

        return result;
    }
}
