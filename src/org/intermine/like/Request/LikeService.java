package org.intermine.like.Request;

import java.io.IOException;
import java.util.ArrayList;

import org.intermine.like.Response.LikeResult;
import org.intermine.like.runTime.RunTime;

/**
 * Perform the query and print the rows of results.
 *
 * @author selma
 *
 */
public final class LikeService
{

    public LikeService() {
        // Don't.
    }

    /**
     * Does all the run time calculations: Perform the query and print the rows of results.
     *
     * @param reg delivers the gene IDs the user wants to search for in the database.
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
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static LikeResult search(LikeRequest reg) throws IOException, ClassNotFoundException {
        Integer[] searchedGenIDs = reg.getIDs();
        LikeResult res = RunTime.calculate(searchedGenIDs);
        return res;
    }
}
