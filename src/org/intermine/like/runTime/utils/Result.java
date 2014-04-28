package org.intermine.like.runTime.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * To give the UI more than one result: total and pairwise rating and common items.
 *
 * @author selma
 *
 */
public class Result
{
    private Integer[][] totalRatingSet;
    private Map<Integer, Map<Integer, Map<Integer, Integer>>> mostSimilarGenes;
    private Map<Integer, Map<Integer, ArrayList<Integer>>> commonItems;

    /**
     *
     */
    public Result() {
    }

    /**
     *
     * @param totalRatingSet
     * @param mostSimilarGenes
     * @param commonItems
     */
    public Result(Integer[][] totalRatingSet,
            Map<Integer, Map<Integer, Map<Integer, Integer>>> mostSimilarGenes,
            Map<Integer, Map<Integer, ArrayList<Integer>>> commonItems) {
        this.totalRatingSet = totalRatingSet;
        this.mostSimilarGenes = mostSimilarGenes;
        this.commonItems = commonItems;
    }

    /**
     *
     * @param totalRatingSet
     */
    public void setTotalRatingSet(Integer[][] totalRatingSet){
        this.totalRatingSet = totalRatingSet;
    }

    /**
     *
     * @param mostSimilarGenes
     */
    public void setMostsimilarGenes(
            Map<Integer, Map<Integer, Map<Integer, Integer>>> mostSimilarGenes) {
        this.mostSimilarGenes = mostSimilarGenes;
    }

    /**
     *
     * @param commonItems
     */
    public void setCommonItems(Map<Integer,Map<Integer,ArrayList<Integer>>> commonItems) {
        this.commonItems = commonItems;
    }

    public Integer[][] getTotalRatingSet() {
        return totalRatingSet;
    }

    /**
     *
     * @return
     */
    public Map<Integer, Map<Integer, Map<Integer, Integer>>> getMostSimilarGenes() {
        return mostSimilarGenes;
    }

    /**
     *
     * @return
     */
    public Map<Integer,Map<Integer,ArrayList<Integer>>> getCommonItems() {
        return commonItems;
    }

    /**
     *
     * @param searchedGene
     * @return
     */
    public Map<Integer, ArrayList<Integer>> getCommonItems(Integer searchedGene) {
        Map<Integer, ArrayList<Integer>> why = new HashMap<Integer, ArrayList<Integer>>();

        for (Map.Entry<Integer, Map<Integer, ArrayList<Integer>>> entry : commonItems.entrySet()) {
            if (entry.getKey().equals(searchedGene)) {
                why = entry.getValue();
            }
        }
        return why;
    }

}
