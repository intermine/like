package org.intermine.like.runTime.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.intermine.like.precalculation.utils.Coordinates;

/**
 *
 * @author selma
 *
 */
public class Methods
{

    private Methods() {
        // Don't.
    }

    /**
    * Methods() prepare the precalculated matrices regarding to the users request.     *
    *
    * An example:
    * <pre>String greeting = "Hello!";</pre>
    *
    * @param getProperties: none,
    *           addMatrices: its own output, the matrix from the loop before and
    *                         the precalculated matrix, that shall be added to it,
    *           findSimilarSet: the output of addMatrices() and the users set of genes,
    *           order: the output of findSimilarSet(),
    *           addCommonMat: its own output, the matrix from the loop before and
    *                         the precalculated matrix, that shall be added to it,
    *           getCommonItems: the output of addCommonMat(), the useres set of genes and
    *           the output of findSimilarSet()
    * @return getProperties: matrix containing the configuration file information,
    *            addMatrices: matrix containing the information of both inputs,
    *            findSimilarSet: array containing similar genes (IDs) to the users genes and
    *            their similarity rating,
    *            order: the ordered input from highest to lowest,
    *            addCommonMat: matrix containing the information of both inputs,
    *            getCommonItems: matrix containing all common items of the similar genes
    *            to the users genes
    *
    * @throws IOException
    */

    public static Map<Coordinates, String> getProperties() throws IOException {
        Map<Coordinates, String> views = new HashMap<Coordinates, String>();

        Properties prop = new Properties();

        String configFileName = "like.properties";
        ClassLoader classLoader = Methods.class.getClassLoader();
        InputStream configStream = classLoader.getResourceAsStream(configFileName);
        prop.load(configStream);

        int countViews = 0;
        for (int i = 0; i < prop.size() / 3; i++) {
            if (prop.getProperty("query." + i + ".required") != null
                    && "yes".equals(prop.getProperty("query." + i + ".required"))) {
                views.put(new Coordinates(countViews, 0), prop.getProperty("query." + i + ".number"));
                views.put(new Coordinates(countViews, 1), prop.getProperty("query." + i + ".id"));
                views.put(new Coordinates(countViews, 2), prop.getProperty("query." + i + ".type"));
                countViews += 1;
            }
        }
        return views;
    }

    /**
     *
     * @param addedMat
     * @param matrix
     * @return
     */
    public static Map<Coordinates, Integer> addMatrices(Map<Coordinates, Integer> addedMat,Map<Coordinates,
            Integer> matrix) {
        if (addedMat.isEmpty()) {
            for (Map.Entry<Coordinates, Integer> entry : matrix.entrySet()) {
                addedMat.put(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()),
                        entry.getValue());
            }
        }
        else {
            for (Map.Entry<Coordinates, Integer> entry : matrix.entrySet()) {
                if (entry.getKey().getKey() == 0 || entry.getKey().getValue() == 0) {
                    addedMat.put(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()),
                            entry.getValue());
                }
                else if (addedMat.get(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()))
                        != null) {
                    addedMat.put(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()),
                            addedMat.get(new Coordinates(entry.getKey().getKey(),
                                    entry.getKey().getValue())) + entry.getValue());
                }
                else {
                    addedMat.put(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()),
                            entry.getValue());
                }
            }
        }
        return addedMat;
    }

    /**
     *
     * @param addedMat
     * @param testSet
     * @return
     */
    public static Map<Integer, Map<Integer, Map<Integer, Integer>>> findSimilarSet(
            Map<Coordinates, Integer> addedMat, Integer[] testSet) {
        Map<Integer, Map<Integer, Map<Integer, Integer>>> similarSet =
                new HashMap<Integer, Map<Integer, Map<Integer, Integer>>>();

        for (Map.Entry<Coordinates, Integer> entry : addedMat.entrySet()) {
            if (entry.getKey().getValue() == 0) {
                for (int i = 0; i < testSet.length; i++) {

                    if (entry.getValue().equals(testSet[i])) {
                        for (Map.Entry<Coordinates, Integer> entry2 : addedMat.entrySet()) {
                            if (entry2.getKey().getValue() != 0
                                    && entry2.getKey().getKey() == entry.getKey().getKey()) {
                                if (!entry2.getKey().getKey().equals(entry2.getKey().getValue())) {
                                    Map<Integer, Integer> pairRating =
                                            new HashMap<Integer, Integer>();
                                    Map<Integer, Map<Integer, Integer>> totalRating =
                                            new HashMap<Integer, Map<Integer, Integer>>();

                                    pairRating.put(testSet[i], entry2.getValue());

                                    if (similarSet.get(addedMat.get(
                                            new Coordinates(0, entry2.getKey().getValue()))) != null) {
                                        totalRating = similarSet.get(addedMat.get(
                                                new Coordinates(0, entry2.getKey().getValue())));
                                        Integer addRating = entry2.getValue();
                                        Map<Integer, Integer> pairRatingTmp =
                                                new HashMap<Integer, Integer>();
                                        for (Map.Entry<Integer, Map<Integer, Integer>>
                                        ratings : totalRating.entrySet()) {
                                            addRating = addRating + ratings.getKey();
                                            pairRatingTmp = ratings.getValue();
                                            pairRatingTmp.put(testSet[i], entry2.getValue());
                                            totalRating.remove(ratings.getKey());
                                        }
                                        totalRating.put(addRating, pairRatingTmp);
                                    }
                                    else {
                                        totalRating.put(entry2.getValue(), pairRating);
                                    }
                                    similarSet.put(addedMat.get(
                                            new Coordinates(0, entry2.getKey().getValue())), totalRating);
                                }
                            }
                        }
                    }
                }
            }
        }
        return similarSet;
    }

    /**
     *
     * @param similarSet
     * @return
     */
    public static Integer[][] getTotalRating(
            Map<Integer, Map<Integer, Map<Integer, Integer>>> similarSet) {
        Integer[][] totalRatingSet = new Integer[similarSet.size()][2];
        int count = 0;
        for (Map.Entry<Integer, Map<Integer, Map<Integer, Integer>>>
        entry : similarSet.entrySet()) {
            totalRatingSet[count][0] = entry.getKey();
            Map<Integer, Map<Integer, Integer>> tmp = entry.getValue();
            for (Map.Entry<Integer, Map<Integer, Integer>> entry2 : tmp.entrySet()) {
                totalRatingSet[count][1] = entry2.getKey();
            }
            count += 1;
        }
        return totalRatingSet;
    }

    /**
     *
     * @param mat2Columns
     * @return
     */
    public static Integer[][] order(Integer[][] mat2Columns) {
        Arrays.sort(mat2Columns, new Comparator<Integer[]>() {
            @Override
            public int compare(final Integer[] entry1, final Integer[] entry2) {
                final Integer time1 = entry1[1];
                final Integer time2 = entry2[1];
                return time2.compareTo(time1);
            }
        });
        return mat2Columns;
    }

    /**
     *
     * @param addedMat
     * @param commonMat
     * @return
     */
    public static Map<Coordinates, ArrayList<Integer>> addCommonMat(Map<Coordinates,
            ArrayList<Integer>> addedMat, Map<Coordinates, ArrayList<Integer>> commonMat) {
        if (addedMat.isEmpty()) {
            return commonMat;
        }
        else {
            for (Map.Entry<Coordinates, ArrayList<Integer>> entry : commonMat.entrySet()) {
                if (entry.getKey().getKey() == 0 || entry.getKey().getValue() == 0) {
                    addedMat.put(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()),
                            entry.getValue());
                }
                else if (addedMat.get(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()))
                        != null) {
                    ArrayList<Integer> tmp = addedMat.get(new Coordinates(entry.getKey().getKey(),
                            entry.getKey().getValue()));
                    tmp.addAll(entry.getValue());
                    addedMat.put(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()), tmp);
                }
                else {
                    addedMat.put(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()),
                            entry.getValue());
                }
            }
            return addedMat;
        }
    }

    /**
     *
     * @param commonMat
     * @param testSet
     * @return
     */
    public static Map<Integer, Map<Integer, ArrayList<Integer>>>
    getCommonItems(Map<Coordinates, ArrayList<Integer>> commonMat, Integer[] testSet) {

        Map<Integer, Map<Integer, ArrayList<Integer>>> commonItems =
                new HashMap<Integer, Map<Integer, ArrayList<Integer>>>();

        for (Map.Entry<Coordinates, ArrayList<Integer>> entry : commonMat.entrySet()) {
            if (entry.getKey().getValue() == 0) {
                for (int i = 0; i < testSet.length; i++) {
                    if (entry.getValue().get(0).equals(testSet[i])) {
                        for (Map.Entry<Coordinates, ArrayList<Integer>> entry2 : commonMat.entrySet()) {
                            if (entry2.getKey().getValue() != 0
                                    && entry2.getKey().getKey() == entry.getKey().getKey()) {
                                Map<Integer, ArrayList<Integer>> commonItemsSingle =
                                        new HashMap<Integer, ArrayList<Integer>>();
                                if (commonItems.containsKey(commonMat.get(
                                        new Coordinates(0, entry2.getKey().getValue())).get(0))) {
                                    commonItemsSingle = commonItems.get(commonMat.get(
                                            new Coordinates(0, entry2.getKey().getValue())).get(0));
                                    commonItemsSingle.put(testSet[i], entry2.getValue());
                                }
                                else {
                                    commonItemsSingle.put(commonMat.get(
                                            new Coordinates(0, entry2.getKey().getValue())).get(0),
                                            commonMat.get(new Coordinates(entry2.getKey().getValue(),
                                                    entry2.getKey().getValue())));
                                    commonItemsSingle.put(testSet[i], entry2.getValue());
                                }
                                commonItems.put(commonMat.get(new Coordinates(0,
                                        entry2.getKey().getValue())).get(0), commonItemsSingle);
                            }
                        }
//                        break;
                    }
                }
            }
        }
        return commonItems;
    }

}
