package org.intermine.like.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * To give the UI more than one result: total and pairwise rating and common items (e.g. pathways).
 *
 * @author selma
 *
 */
public class LikeResult
{
    private Integer[][] totalRatingSet;
    private Map<Integer, Map<Integer, Map<Integer, Integer>>> similarGenes;
    private Map<Integer, Map<Integer, ArrayList<Integer>>> commonItems;

    /**
     * A LikeResult can be empty at first and then filled with setters.
     */
    public LikeResult() {
    }

    /**
     *
     * @param totalRatingSet contains the result genes and their total ratings ordered from
     * highest to lowest
     * @param mostSimilarGenes contains the same information like totalRatingSet plus information
     * about pairwise similarities between searched and result genes:
     * "Map<resultGeneID, Map<totalRating< Map<searchedGeneID, pairwiseRating>>>"
     * @param commonItems contains the common items (e.g. pathway IDs), which the result genes
     * have in common with the searched genes (pairwise):
     * "Map<resultGeneID, Map<searchedGeneID, ListOfCommonItems>>"
     */
    public LikeResult(Integer[][] totalRatingSet,
            Map<Integer, Map<Integer, Map<Integer, Integer>>> mostSimilarGenes,
            Map<Integer, Map<Integer, ArrayList<Integer>>> commonItems) {
        this.totalRatingSet = totalRatingSet;
        this.similarGenes = mostSimilarGenes;
        this.commonItems = commonItems;
    }

    /**
     *
     * @param totalRatingSet contains the result genes and their total ratings ordered from
     * highest to lowest
     */
    public void setTotalRatingSet(Integer[][] totalRatingSet) {
        this.totalRatingSet = totalRatingSet;
    }

    /**
     *
     * @param mostSimilarGenes contains the same information like totalRatingSet plus information
     * about pairwise similarities between searched and result genes:
     * "Map<resultGeneID, Map<totalRating< Map<searchedGeneID, pairwiseRating>>>"
     */
    public void setMostsimilarGenes(
            Map<Integer, Map<Integer, Map<Integer, Integer>>> mostSimilarGenes) {
        this.similarGenes = mostSimilarGenes;
    }

    /**
     *
     * @param commonItems contains the common items (e.g. pathway IDs), which the result genes
     * have in common with the searched genes (pairwise):
     * "Map<resultGeneID, Map<searchedGeneID, ListOfCommonItems>>"
     */
    public void setCommonItems(Map<Integer, Map<Integer, ArrayList<Integer>>> commonItems) {
        this.commonItems = commonItems;
    }

    /**
     *
     * @return the result genes and their total ratings ordered from
     * highest to lowest
     */
    public Integer[][] getTotalRatingSet() {
        return totalRatingSet;
    }

    /**
     *
     * @return contains the same information like totalRatingSet plus information
     * about pairwise similarities between searched and result genes:
     * "Map<resultGeneID, Map<totalRating< Map<searchedGeneID, pairwiseRating>>>"
     */
    public Map<Integer, Map<Integer, Map<Integer, Integer>>> getsimilarGenes() {
        return similarGenes;
    }

    /**
     *
     * @return contains the common items (e.g. pathway IDs), which the result genes
     * have in common with the searched genes (pairwise):
     * "Map<resultGeneID, Map<searchedGeneID, ListOfCommonItems>>"
     */
    public Map<Integer, Map<Integer, ArrayList<Integer>>> getCommonItems() {
        return commonItems;
    }

    /**
     *
     * @param resultGene one specific gene ID of the result genes.
     * @return all searched genes, which are similar to the resultGene and their common items.
     */
    public Map<Integer, ArrayList<Integer>> getCommonItems(Integer resultGene) {
        Map<Integer, ArrayList<Integer>> why = new HashMap<Integer, ArrayList<Integer>>();

        for (Map.Entry<Integer, Map<Integer, ArrayList<Integer>>> entry : commonItems.entrySet()) {
            if (entry.getKey().equals(resultGene)) {
                why = entry.getValue();
            }
        }
        return why;
    }
}
