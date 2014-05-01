package org.intermine.like.runTime.utils;

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
 * Methods() prepares the pre-calculated matrices regarding to the users request.
 * It is used from the RunTime.java.
 *
 * @author selma
 *
 */
public final class Methods
{

    private Methods() {
        // Don't.
    }

    /**
     * reads the property file.
     *
     * @return matrix containing the configuration file information: which aspects shall be
     * calculated and how shall they be calculated
     * @throws IOException
     */
    public static Map<Coordinates, String> getProperties() throws IOException {
        Map<Coordinates, String> views = new HashMap<Coordinates, String>();

        Properties prop = new Properties();

        String configFileName = "like_config.properties";
        ClassLoader classLoader = Methods.class.getClassLoader();
        InputStream configStream = classLoader.getResourceAsStream(configFileName);
        prop.load(configStream);

        int countViews = 0;
        for (int i = 0; i < prop.size() / 4; i++) {
            if (prop.getProperty("query." + i + ".required") != null
                    && "yes".equals(prop.getProperty("query." + i + ".required"))) {
                views.put(new Coordinates(countViews, 0),
                        prop.getProperty("query." + i + ".number"));
                views.put(new Coordinates(countViews, 1), prop.getProperty("query." + i + ".id"));
                views.put(new Coordinates(countViews, 2),
                        prop.getProperty("query." + i + ".constraint"));
                views.put(new Coordinates(countViews, 3), prop.getProperty("query." + i + ".type"));
                countViews += 1;
            }
        }
        return views;
    }

    /**
     * This method is called for each aspect once, while at the second time it has it's own result
     * as input. This is to add all aspects together.
     *
     * @param addedMat its own output from the iteration before (is empty for the first iteration)
     * @param matrix a rectangular pre-calculated matrix. The first row and column contain the
     * gene IDs. The rest contains the similarity ratings for one single aspect.
     * @return matrix containing the information of both inputs
     */
    public static Map<Coordinates, Integer> addMatrices(Map<Coordinates, Integer> addedMat,
            Map<Coordinates, Integer> matrix) {
        if (addedMat.isEmpty()) {
            for (Map.Entry<Coordinates, Integer> entry : matrix.entrySet()) {
                addedMat.put(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()),
                        entry.getValue());
            }
        }
        else {
            for (Map.Entry<Coordinates, Integer> entry : matrix.entrySet()) {
                if (entry.getKey().getKey() == 0 || entry.getKey().getValue() == 0) {
                    addedMat.put(new Coordinates(entry.getKey().getKey(),
                            entry.getKey().getValue()), entry.getValue());
                }
                else if (addedMat.get(new Coordinates(entry.getKey().getKey(),
                        entry.getKey().getValue())) != null) {
                    addedMat.put(new Coordinates(entry.getKey().getKey(),
                            entry.getKey().getValue()), addedMat.get(
                                    new Coordinates(entry.getKey().getKey(),
                                    entry.getKey().getValue())) + entry.getValue());
                }
                else {
                    addedMat.put(new Coordinates(entry.getKey().getKey(),
                            entry.getKey().getValue()), entry.getValue());
                }
            }
        }
        return addedMat;
    }

    /**
     * Iterate through the total similarity matrix to find the searched genes and their similar
     * genes.
     *
     * @param addedMat a rectangular matrix. The first row and column contain the gene IDs. The
     * rest contains the similarity ratings added together for all aspects.
     * @param searchedGenes is a list of InterMine gene IDs the users wants to search for in the
     * database.
     * @return array containing similar to the users genes gene IDs and their total similarity
     * rating.
     */
    public static Map<Integer, Map<Integer, Map<Integer, Integer>>> findSimilarSet(
            Map<Coordinates, Integer> addedMat, Integer[] searchedGenes) {
        Map<Integer, Map<Integer, Map<Integer, Integer>>> similarSet =
                new HashMap<Integer, Map<Integer, Map<Integer, Integer>>>();

        for (Map.Entry<Coordinates, Integer> entry : addedMat.entrySet()) {
            if (entry.getKey().getValue() == 0) {
                for (int i = 0; i < searchedGenes.length; i++) {

                    if (entry.getValue().equals(searchedGenes[i])) {
                        for (Map.Entry<Coordinates, Integer> entry2 : addedMat.entrySet()) {
                            if (entry2.getKey().getValue() != 0
                                    && entry2.getKey().getKey() == entry.getKey().getKey()) {
                                if (!entry2.getKey().getKey().equals(entry2.getKey().getValue())) {
                                    Map<Integer, Integer> pairRating =
                                            new HashMap<Integer, Integer>();
                                    Map<Integer, Map<Integer, Integer>> totalRating =
                                            new HashMap<Integer, Map<Integer, Integer>>();

                                    pairRating.put(searchedGenes[i], entry2.getValue());

                                    if (similarSet.get(addedMat.get(
                                            new Coordinates(0, entry2.getKey().getValue())))
                                            != null) {
                                        totalRating = similarSet.get(addedMat.get(
                                                new Coordinates(0, entry2.getKey().getValue())));
                                        Integer addRating = entry2.getValue();
                                        Map<Integer, Integer> pairRatingTmp =
                                                new HashMap<Integer, Integer>();
                                        for (Map.Entry<Integer, Map<Integer, Integer>>
                                        ratings : totalRating.entrySet()) {
                                            addRating = addRating + ratings.getKey();
                                            pairRatingTmp = ratings.getValue();
                                            pairRatingTmp.put(searchedGenes[i], entry2.getValue());
                                            totalRating.remove(ratings.getKey());
                                        }
                                        totalRating.put(addRating, pairRatingTmp);
                                    }
                                    else {
                                        totalRating.put(entry2.getValue(), pairRating);
                                    }
                                    similarSet.put(addedMat.get(new Coordinates(0,
                                            entry2.getKey().getValue())), totalRating);
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
     * Extracts the information about the result genes with their similar ratings out of the
     * similarSet. The 2D Integer can be ordered from highest to lowest by rating.
     *
     * @param similarSet contains the result genes and their total ratings as well as information
     * about pairwise similarities between searched and result genes:
     * "Map<resultGeneID, Map<totalRating< Map<searchedGeneID, pairwiseRating>>>"
     * @return 2D Integer containing the result gene IDs and their total ratings
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
     * Orders a 2D Integer from highest to lowest in the second column.
     *
     * @param mat2Columns 2D Integer where the first column contains gene IDs and the second the
     * corresponding total rating.
     * @return the ordered input from highest to lowest
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
     * This method is called for each aspect once, while at the second time it has it's own result
     * as input. This is to add all aspects together.
     *
     * @param addedCommonMat its own output from the iteration before (is empty for the first
     * iteration)
     * @param commonMat a rectangular pre-calculated matrix. The first row and column contain the
     * gene IDs. The rest contains Lists of common items
     * @return matrix containing the information of both inputs
     */
    public static Map<Coordinates, ArrayList<Integer>> addCommonMat(Map<Coordinates,
            ArrayList<Integer>> addedCommonMat, Map<Coordinates, ArrayList<Integer>> commonMat) {
        if (addedCommonMat.isEmpty()) {
            return commonMat;
        }
        else {
            for (Map.Entry<Coordinates, ArrayList<Integer>> entry : commonMat.entrySet()) {
                if (entry.getKey().getKey() == 0 || entry.getKey().getValue() == 0) {
                    addedCommonMat.put(new Coordinates(entry.getKey().getKey(),
                            entry.getKey().getValue()), entry.getValue());
                }
                else if (addedCommonMat.get(new Coordinates(entry.getKey().getKey(),
                        entry.getKey().getValue())) != null) {
                    ArrayList<Integer> tmp = addedCommonMat.get(new Coordinates(
                            entry.getKey().getKey(), entry.getKey().getValue()));
                    tmp.addAll(entry.getValue());
                    addedCommonMat.put(new Coordinates(entry.getKey().getKey(),
                            entry.getKey().getValue()), tmp);
                }
                else {
                    addedCommonMat.put(new Coordinates(entry.getKey().getKey(),
                            entry.getKey().getValue()),
                            entry.getValue());
                }
            }
            return addedCommonMat;
        }
    }

    /**
     * Calculates a Map of the common items (e.g. pathway IDs), that the result genes have in
     * common with the searched genes (pairwise)
     *
     * @param commonMat a rectangular matrix. The first row and column contain the gene IDs. The
     * rest contains Lists of common items.
     * @param searchedGenes is a list of InterMine gene IDs the users wants to search for in the
     * database.
     * @return matrix containing all common items of the similar genes to the users genes (pairwise)
     * "Map<resultGeneID, Map<searchedGeneID, ListOfCommonItems>>"
     */
    public static Map<Integer, Map<Integer, ArrayList<Integer>>> getCommonItems(
            Map<Coordinates, ArrayList<Integer>> commonMat, Integer[] searchedGenes) {

        Map<Integer, Map<Integer, ArrayList<Integer>>> commonItems =
                new HashMap<Integer, Map<Integer, ArrayList<Integer>>>();

        for (Map.Entry<Coordinates, ArrayList<Integer>> entry : commonMat.entrySet()) {
            if (entry.getKey().getValue() == 0) {
                for (int i = 0; i < searchedGenes.length; i++) {
                    if (entry.getValue().get(0).equals(searchedGenes[i])) {
                        for (Map.Entry<Coordinates, ArrayList<Integer>>
                        entry2 : commonMat.entrySet()) {
                            if (entry2.getKey().getValue() != 0
                                    && entry2.getKey().getKey() == entry.getKey().getKey()) {
                                Map<Integer, ArrayList<Integer>> commonItemsSingle =
                                        new HashMap<Integer, ArrayList<Integer>>();
                                if (commonItems.containsKey(commonMat.get(
                                        new Coordinates(0, entry2.getKey().getValue())).get(0))) {
                                    commonItemsSingle = commonItems.get(commonMat.get(
                                            new Coordinates(0, entry2.getKey().getValue())).get(0));
                                    commonItemsSingle.put(searchedGenes[i], entry2.getValue());
                                }
                                else {
                                    commonItemsSingle.put(commonMat.get(
                                            new Coordinates(0, entry2.getKey().getValue())).get(0),
                                            commonMat.get(
                                                    new Coordinates(entry2.getKey().getValue(),
                                                    entry2.getKey().getValue())));
                                    commonItemsSingle.put(searchedGenes[i], entry2.getValue());
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
