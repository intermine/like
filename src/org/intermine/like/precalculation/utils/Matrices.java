package org.intermine.like.precalculation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Matrices() is used for the pre-calculation of a matrix, that includes all items, that every
 * gene has in common with every other gene in the dataset (based on one single aspect).
 * Out of this another matrix is calculated, that contains the similarity between every gene.
 * The similarity is a rating from 0 to 100, where 0 (null) means "nothing in common"
 * and 100 means "these are the most similar genes in the dataset".
 *
 * The matrices are rectangular, where both the first row and the first column contains
 * all gene IDs. That is to simplify the run time calculations: If you want to get the
 * similar Genes for one specific gene, you just have to read out one row; the one where
 * the gene ID is in the first column.
 *
 * All calculations are based on one single aspect!
 *
 * @author selma
 */
public final class Matrices
{

    private Matrices() {
        // Don't.
    }

    /**
     * Overrides interface MatrixOperation.
     * Finds common related items between all genes.
     *
     * @param matrix containing all genes and their related items.
     * @return a rectangular matrix (HashMap with x- and y-coordinates as keys) containing all
     * gene IDs and the ArrayLists of related items, that genes have in common.
     */
    public static Map<Coordinates, ArrayList<Integer>> findCommonItems(
            final Map<Coordinates, Integer> matrix) {
        return commonMatrixLoop(matrix, new MatrixOperation() {

            @Override
            public void loopAction(Map<Coordinates, ArrayList<Integer>> newMatrix,
                    Map<Coordinates, Integer> matrix, Coordinates relationShip, Integer objId) {
                final Map<Integer, ArrayList<Integer>> count =
                        new HashMap<Integer, ArrayList<Integer>>();

                int objA = relationShip.getKey(),
                    objB = objId.intValue();

                for (Map.Entry<Coordinates, Integer> inner : matrix.entrySet()) {
                    if (inner.getKey().getKey() == objA) {
                        for (Map.Entry<Coordinates, Integer> inner2 : matrix.entrySet()) {
                            if (relationShip != inner2.getKey()
                                    && inner.getValue().equals(inner2.getValue())) {
                                ArrayList<Integer> commonItems;
                                if (!count.containsKey(inner2.getKey().getKey())) {
                                    commonItems = new ArrayList<Integer>();
                                    count.put(inner2.getKey().getKey(), commonItems);
                                    commonItems.add(inner2.getValue());
                                } else {
                                    commonItems = count.get(inner2.getKey().getKey());
                                    commonItems.add(inner2.getValue());
                                }
                            }
                        }
                    }
                }
                for (Map.Entry<Integer, ArrayList<Integer>> entry : count.entrySet()) {
                    newMatrix.put(new Coordinates(objA + 1, entry.getKey() + 1), entry.getValue());
                }

            }
        });
    }

    /**
     * Overrides interface MatrixOperation.
     * Does the same like method findCommonItems but for the type "presence".
     *
     * @param matrix containing all genes and their related items.
     * @return a rectangular matrix (HashMap with x- and y-coordinates as keys) containing all
     * gene IDs and the ArrayLists of related items, that genes have in common.
     */
    public static Map<Coordinates, ArrayList<Integer>> findCommonItemsPresence(
            final Map<Coordinates, Integer> matrix) {
        return commonMatrixLoop(matrix, new MatrixOperation() {

            @Override
            public void loopAction(Map<Coordinates, ArrayList<Integer>> newMatrix,
                    Map<Coordinates, Integer> matrix, Coordinates relationShip, Integer objId) {
                int objA = relationShip.getKey(),
                    objB = objId.intValue();


                for (final Map.Entry<Coordinates, Integer> inner : matrix.entrySet()) {
                    if (inner.getKey().getValue() != 0 && inner.getKey().getKey() == objA) {
                        ArrayList<Integer> commonItems;
                        if (!newMatrix.containsKey(new Coordinates(objA + 1, objA + 1))) {
                            commonItems = new ArrayList<Integer>();
                            newMatrix.put(new Coordinates(objA + 1, objA + 1), commonItems);
                            commonItems.add(inner.getValue());
                        }
                        else {
                            commonItems = newMatrix.get(new Coordinates(objA + 1, objA + 1));
                            commonItems.add(inner.getValue());
                        }
                    }
                }

            }
        });
    }

    /**
     * Calculates the result for findCommonItems and findCommonItemsPresence.
     * Performs the outer loop and saves the gene IDs in the first column and row.
     *
     * @param matrix containing all genes and their related items.
     * Format: Its first column contains
     * the gene IDs, the other columns contain the related items (1 column for each unique item).
     * @param operation containing the overridden loopAction code
     * @return a rectangular matrix (HashMap with x- and y-coordinates as keys) containing all
     * gene IDs and the ArrayLists of related items, that genes have in common.
     * Format: The first row and the first column contain the gene IDs, whereas coordinates (0,1)
     * and (1,0) are the same ID (also (0,2) and (2,0), and so on). The other rows and columns
     * contain the ArrayLists of the common related items. E.g. ArrayList of (3,5) contains common
     * related items of the genes (3,0) and (0,5).
     */
    private static Map<Coordinates, ArrayList<Integer>> commonMatrixLoop(
            Map<Coordinates, Integer> matrix, MatrixOperation operation) {
        Map<Coordinates, ArrayList<Integer>> commonMat =
                new HashMap<Coordinates, ArrayList<Integer>>();
        for (final Map.Entry<Coordinates, Integer> outer : matrix.entrySet()) {
            if (outer.getKey().getValue() == 0) {
                ArrayList<Integer> geneInColumn = new ArrayList<Integer>();
                ArrayList<Integer> geneInRow = new ArrayList<Integer>();
                commonMat.put(new Coordinates(0, outer.getKey().getKey() + 1), geneInColumn);
                commonMat.put(new Coordinates(outer.getKey().getKey() + 1, 0), geneInRow);
                geneInColumn.add(matrix.get(new Coordinates(outer.getKey().getKey(), 0)));
                geneInRow.add(matrix.get(new Coordinates(outer.getKey().getKey(), 0)));

                operation.loopAction(commonMat, matrix, outer.getKey(), outer.getValue());
            }
        }
        return commonMat;
    }

    /**
     * Calculates the similarity ratings pairwise and for one aspect for the type "category".
     *
     * @param commonMat a rectangular matrix (HashMap with x- and y-coordinates as keys) containing
     * all gene IDs and the ArrayLists of related items, that genes have in common.
     * @return a rectangular matrix (HashMap with x- and y-coordinates as keys) containing all
     * gene IDs and pairwise similarity ratings between the genes.
     */
    public static Map<Coordinates, Integer> countCommonItemsCategory(
            Map<Coordinates, ArrayList<Integer>> commonMat) {
        Map<Coordinates, Integer> simMat = new HashMap<Coordinates, Integer>();

        for (Map.Entry<Coordinates, ArrayList<Integer>> entry : commonMat.entrySet()) {
            if (entry.getKey().getKey() == 0 || entry.getKey().getValue() == 0) {
                simMat.put(entry.getKey(), entry.getValue().get(0));
            }
            else {
                simMat.put(entry.getKey(), entry.getValue().size());
            }
        }
        return simMat;
    }

    /**
     * Calculates the similarity ratings pairwise and for one aspect for the type "count".
     *
     * @param matrix containing all genes and their related items.
     * @return a rectangular matrix (HashMap with x- and y-coordinates as keys) containing all
     * gene IDs and pairwise similarity ratings between the genes.
     */
    public static Map<Coordinates, Integer> findSimilarityCount(Map<Coordinates, Integer> matrix) {
        Map<Coordinates, Integer> difMat = new HashMap<Coordinates, Integer>();
        Map<Coordinates, Integer> simMat = new HashMap<Coordinates, Integer>();

        for (Map.Entry<Coordinates, Integer> entry : matrix.entrySet()) {
            if (entry.getKey().getValue() == 0) {
                difMat.put(entry.getKey(), entry.getValue());
                int count = 0;
                for (Map.Entry<Coordinates, Integer> entry2 : matrix.entrySet()) {
                    if (entry.getKey().getKey() == entry2.getKey().getKey()) {
                        count += 1;
                    }
                }
                difMat.put(new Coordinates(entry.getKey().getKey(), 1), count - 1);
            }
        }

        for (Map.Entry<Coordinates, Integer> entry : difMat.entrySet()) {
            if (entry.getKey().getValue() == 0) {
                simMat.put(new Coordinates(0, entry.getKey().getKey() + 1),
                        difMat.get(new Coordinates(entry.getKey().getKey(), 0)));
                simMat.put(new Coordinates(entry.getKey().getKey() + 1, 0),
                        difMat.get(new Coordinates(entry.getKey().getKey(), 0)));
            }
            else {
                int rating;
                for (Map.Entry<Coordinates, Integer> entry2 : difMat.entrySet()) {
                    if (entry2.getKey().getValue() == 1 && entry.getValue() != 0
                            && entry2.getValue() != 0) {
                        rating = Math.abs(100 * entry2.getValue()) / entry.getValue();
                        if (rating > 100) {
                            rating = 100;
                        }
                        simMat.put(new Coordinates(entry.getKey().getKey() + 1,
                                entry2.getKey().getKey() + 1), rating);
                    }
                }
            }
        }
        return simMat;
    }

    /**
     * Calculates the similarity ratings pairwise and for one aspect for the type "presence".
     *
     * @param matrix containing all genes and their related items.
     * @return a rectangular matrix (HashMap with x- and y-coordinates as keys) containing all
     * gene IDs and pairwise similarity ratings between the genes.
     */
    public static Map<Coordinates, Integer> findSimilarityPresence(
            Map<Coordinates, Integer> matrix) {
        Map<Coordinates, Integer> hasMat = new HashMap<Coordinates, Integer>();
        Map<Coordinates, Integer> simMat = new HashMap<Coordinates, Integer>();

        for (Map.Entry<Coordinates, Integer> entry : matrix.entrySet()) {
            if (entry.getKey().getValue() == 0){
                hasMat.put(entry.getKey(), entry.getValue());
                if (matrix.get(new Coordinates(entry.getKey().getKey(), 1)) == null) {
                    hasMat.put(new Coordinates(entry.getKey().getKey(), 1), 0);
                }
                else {
                    hasMat.put(new Coordinates(entry.getKey().getKey(), 1), 1);
                }
            }
        }

        System.out.print("\nsmallResult (correct result): \n");
        for (int k = 0; k < 5; k++) {
            for (int j = 0; j < 5; j++) {
                Integer val = hasMat.get(new Coordinates(k, j));
                System.out.print(val + " ");
            }
            System.out.print("\n");
        }

        for (Map.Entry<Coordinates, Integer> entry : hasMat.entrySet()) {
            if (entry.getKey().getValue() == 0) {
                simMat.put(new Coordinates(0, entry.getKey().getKey() + 1),
                        hasMat.get(new Coordinates(entry.getKey().getKey(), 0)));
                simMat.put(new Coordinates(entry.getKey().getKey() + 1, 0),
                        hasMat.get(new Coordinates(entry.getKey().getKey(), 0)));
            }
            else {
                for (Map.Entry<Coordinates, Integer> entry2 : hasMat.entrySet()) {
                    if (entry2.getKey().getValue() == 1) {
                        if (entry2.getValue().equals(entry.getValue())) {
                            simMat.put(new Coordinates(entry.getKey().getKey() + 1,
                                    entry2.getKey().getKey() + 1), 100);
                        }
                        else {
                            simMat.put(new Coordinates(entry.getKey().getKey() + 1,
                                    entry2.getKey().getKey() + 1), 0);
                        }
                    }
                }
            }
        }
        return simMat;
    }

    /**
     * Normalises the input matrix column-wise.
     * E.g. given matrix:        ->   normalised matrix:
     *   -   gene1 gene2 gene3          -   gene1 gene2 gene3
     * gene1   5     2   null         gene1  5/5   2/5  null
     * gene2   2     2    1           gene2  2/2   2/2  1/2
     * gene3  null   1    3           gene3  null  1/3  3/3
     *
     * @param matrix containing all gene IDs and pairwise similarity ratings between the genes.
     * @return the input matrix normalised.
     */
    public static Map<Coordinates, Integer> normalise(Map<Coordinates, Integer> matrix) {
        Map<Coordinates, Integer> normMat = new HashMap<Coordinates, Integer>();
        for (Map.Entry<Coordinates, Integer> entry : matrix.entrySet()) {
            if (entry.getKey().getValue() == 0) {
                normMat.put(new Coordinates(0, entry.getKey().getKey()),
                        matrix.get(new Coordinates(entry.getKey().getKey(), 0)));
                normMat.put(new Coordinates(entry.getKey().getKey(), 0),
                        matrix.get(new Coordinates(entry.getKey().getKey(), 0)));
            }
            if (entry.getKey().getKey() != 0 && entry.getKey().getValue() != 0) {
                normMat.put(new Coordinates(entry.getKey().getKey(), entry.getKey().getValue()),
                        entry.getValue() * 100 / matrix.get(
                                new Coordinates(entry.getKey().getKey(), entry.getKey().getKey())));
            }
        }
        return normMat;
    }

    /**
     * Used in commonMatrixLoop.
     *
     * @author selma
     *
     */
    interface MatrixOperation
    {
        /**
        * Which parameter are needed in the inner loop.
        *
        * @param newMatrix a rectangular matrix (HashMap with x- and y-coordinates as keys)
        * containing gene IDs and the ArrayLists of related items, that genes have in common.
        * @param matrix containing all genes and their related items.
        * @param relationShip coordinates of a gene ID
        * @param objId corresponding gene ID
        */
        void loopAction(Map<Coordinates, ArrayList<Integer>> newMatrix,
                Map<Coordinates, Integer> matrix, Coordinates relationShip, Integer objId);

    }
}
