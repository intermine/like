package org.intermine.like.Request;

import java.util.ArrayList;

/**
 * Setting and Getting the gene IDs the users wants to search for in the database.
 *
 * @author selma
 *
 */
public class LikeRequest
{
    Integer[] searchedGenes;

    /**
     * a LikeRequest can be empty
     */
    public LikeRequest() {
    }

    /**
     * Set the gene IDs the users wants to search for in the database.
     * @param searchedGenes is a list of InterMine gene IDs.
     */
    public void setIDs(Integer... searchedGenes) {
        this.searchedGenes = searchedGenes;
    }

    /**
     *
     * @return the gene IDs the users wants to search for in the database.
     */
    public Integer[] getIDs() {
        return searchedGenes;
    }
}
