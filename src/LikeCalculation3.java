import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.intermine.metadata.Model;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;


public class LikeCalculation3 {
	private static final String ROOT = "http://beta.flymine.org/beta/service";

    /**
     * Perform the query and print the rows of results.
     * @param args command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ServiceFactory factory = new ServiceFactory(ROOT);
        Model model = factory.getModel();
        PathQuery query = new PathQuery(model);

//        // Select the output columns:
//        query.addViews("Protein.genes.primaryIdentifier",
//                "Protein.genes.symbol",
//                "Protein.proteinDomains.shortName",
//                "Protein.proteinDomains.primaryIdentifier");
//
//        // Add orderby
//        query.addOrderBy("Protein.genes.symbol", OrderDirection.ASC);
//
//        // Filter the results with the following constraints:
//        query.addConstraint(Constraints.eq("Protein.organism.shortName", "D. melanogaster"), "A");
//        query.addConstraint(Constraints.eq("Protein.genes.symbol", "*ze*"), "B");
//        // Specify how these constraints should be combined.
//        query.setConstraintLogic("A and B");
        
//     // Select the output columns:
//        query.addViews("Protein.genes.primaryIdentifier",
//                "Protein.genes.symbol",
//                "Protein.proteinDomains.shortName",
//                "Protein.proteinDomains.primaryIdentifier");
//
//        // Add orderby
//        query.addOrderBy("Protein.genes.symbol", OrderDirection.ASC);
//
//        // Filter the results with the following constraints:
//        query.addConstraint(Constraints.eq("Protein.organism.shortName", "D. melanogaster"), "A");
//        query.addConstraint(Constraints.eq("Protein.genes.symbol", "z*"), "B");
//        // Specify how these constraints should be combined.
//        query.setConstraintLogic("A and B");
     // Select the output columns:
        query.addViews("Gene.primaryIdentifier",
                "Gene.symbol",
                "Gene.proteins.proteinDomains.primaryIdentifier",
                "Gene.proteins.proteinDomains.shortName");

        // Add orderby
        query.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);

        // Filter the results with the following constraints:
        query.addConstraint(Constraints.eq("Gene.symbol", "z*"), "A");
        query.addConstraint(Constraints.eq("Gene.organism.shortName", "D. melanogaster"), "B");
        // Specify how these constraints should be combined.
        query.setConstraintLogic("A and B");


        QueryService service = factory.getQueryService();
        PrintStream out = System.out;
        String format = "%-22.22s | %-22.22s | %-22.22s | %-22.22s\n";
//     // Select the output columns:
//        query.addViews("Gene.primaryIdentifier",
//                "Gene.symbol",
//                "Gene.proteins.proteinDomains.primaryIdentifier",
//                "Gene.proteins.proteinDomains.shortName",
//                "Gene.length");
//
//        // Add orderby
//        query.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);
//
//        // Filter the results with the following constraints:
//        query.addConstraint(Constraints.eq("Gene.symbol", "z*"), "A");
//        query.addConstraint(Constraints.eq("Gene.organism.shortName", "D. melanogaster"), "B");
//        // Specify how these constraints should be combined.
//        query.setConstraintLogic("A and B");
//
//        QueryService service = factory.getQueryService();
//        PrintStream out = System.out;
//        String format = "%-17.17s | %-17.17s | %-17.17s | %-17.17s | %-17.17s\n";
//     // Select the output columns:
//        query.addViews("Gene.primaryIdentifier",
//                "Gene.symbol",
//                "Gene.proteins.proteinDomains.primaryIdentifier",
//                "Gene.proteins.proteinDomains.shortName",
//                "Gene.length",
//                "Gene.transcripts.primaryIdentifier");
//
//        // Add orderby
//        query.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);
//
//        // Filter the results with the following constraints:
//        query.addConstraint(Constraints.eq("Gene.symbol", "z*"), "A");
//        query.addConstraint(Constraints.eq("Gene.organism.shortName", "D. melanogaster"), "B");
//        // Specify how these constraints should be combined.
//        query.setConstraintLogic("A and B");
//
//        QueryService service = factory.getQueryService();
//        PrintStream out = System.out;
//        String format = "%-13.13s | %-13.13s | %-13.13s | %-13.13s | %-13.13s | %-13.13s\n";

        out.printf(format, query.getView().toArray());
        Iterator<List<Object>> rows = service.getRowListIterator(query);
        String tmp;
        int countRow = 0;
        int countColumn = 0;
        int firstColumn = 0;
        int firstRow = 0;
        int uniqueProDom = 150; // !!!! how many unique protein domains exist in the query !!!!
        int uniqueGene = 31; // !!!! how many unique genes exist in the query !!!!
        String[][] matrix = new String[uniqueGene][uniqueProDom+1];
        
        while (rows.hasNext()) {
            List<Object> row = rows.next();
        	out.printf(format, row.toArray());
        	for (int i = 0; i < 4 ; i++){
        		tmp = row.get(i).toString();
        		if (tmp.contains("FBgn")){
        			if (firstRow == 0){
        				matrix[0][0] = tmp;
        				firstRow += 1;
        			}
        			else {
        				for (int l = 0; l <= countRow; l++){
        					if (!tmp.equals(matrix[countRow][0])){
        						countRow += 1;
        						matrix[countRow][0] = tmp;
        					}
        				}
        			}
        		}
        		if (tmp.contains("IPR0")){
        			if (firstColumn == 0){
        				matrix[0][1] = tmp;
        				firstColumn += 1;
        				countColumn += 1;
        			}
        			else {
        				int tmpColumn = countColumn+1;
        				int saved = 0;
	        			for (int j = 0; j <= countRow; j++){
	        				for (int k = 0; k <= (tmpColumn-1); k++){
	        					if (tmp.equals(matrix[j][k]) 
	        							&& saved == 0){
	        						matrix[countRow][k] = tmp;
	        						saved += 1;
	        					}
	        					else if (j == (countRow) && k == (countColumn-1) 
	        							&& saved == 0){
	        						countColumn += 1;
	        	        			matrix[countRow][countColumn] = tmp;
	        	        			saved += 1;
	        					}
	        				}
	        			}
        			}
        		}
        	}
        }
      
        out.printf("%d rows\n", service.getCount(query));
        
        out.print("\nData about Protein Domains: \n");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                out.print(matrix[i][j] + " ");
            }
            out.print("\n");
        }
        
        out.print("service.getCount(query): " + service.getCount(query) + "\nmatrixSumUp.length:" + matrix.length + "\n");
        
 // return similar genes using algorithm
    
    // String comparison
    String[][] simMat = calculateMatrix(matrix);
    out.print("Similarity matrix: \n");
    for (int i = 0; i < simMat.length; i++) {
        for (int j = 0; j < simMat[0].length; j++) {
            out.print(simMat[i][j] + " ");
        }
        out.print("\n");
    }
    
    String searchedGene = "FBgn0004606";
    String[][] kNearestNeighbours = calculateKNearestNeighbours(simMat, 10, searchedGene);
    out.print("K-Nearest Neighbours of "+ searchedGene + ": \n");
    for (int i = 0; i < kNearestNeighbours.length; i++) {
    	for (int j = 0; j < kNearestNeighbours[0].length; j++) {
            out.print(kNearestNeighbours[i][j] + " ");
        }
        out.print("\n");
    }
}


public static String[][] calculateMatrix(String[][] geneMatSumUp) {
	// Compare geneMatSumUp matrix to all other Genes
	
	String[][] simMat = new String[geneMatSumUp.length+1][geneMatSumUp.length+1];
	for (int i = 0; i < geneMatSumUp.length; i++){
		simMat[0][i+1] = geneMatSumUp[i][0];
		simMat[i+1][0] = geneMatSumUp[i][0];
	}
	
	for (int i = 0; i < geneMatSumUp.length; i++){
		for (int j = 0; j < geneMatSumUp.length; j++){
			int count = 0;
			for (int k = 1; k < geneMatSumUp[0].length; k++){
				for (int l = 1; l < geneMatSumUp[0].length; l++){
					if (geneMatSumUp[i][k] != null && geneMatSumUp[i][k].equals(geneMatSumUp[j][l])){
						count += 1;
					}
				}
			}
			String simil = Integer.toString(count);
			simMat[i+1][j+1] = simil;
		}				
	}		
	for (int m = 0; m <= geneMatSumUp.length; m++){
		simMat[m][m] = "-";
	}
	return simMat;
}	


public static String[][] calculateKNearestNeighbours(String[][] simMat, int kNearest, String SearchedGene){
	// return the k-nearest neighbours for 1 gene
	String[][] kNearestNeighbours = new String[simMat.length-1][2];
	int[] allNeighbours = new int[simMat.length];
	int tmp = -1;
	
	for (int i = 0; i < simMat.length; i++){
		if (tmp < 0 && simMat[i][0].equals(SearchedGene)){
			tmp = i;
		}
	}
	
	for (int i = 1; i < simMat.length; i++){
		if (!simMat[tmp][i].equals("-")){
			kNearestNeighbours[i-1][1] = simMat[tmp][i];
		} 
		else {
			kNearestNeighbours[i-1][1] = "100";
		}
		kNearestNeighbours[i-1][0] = simMat[0][i];
	}
	
	int neighbour = Integer.parseInt(kNearestNeighbours[0][1]);
	allNeighbours[0] = neighbour;
	String[] tmpNeighbour = new String[2];
	
	for (int j = 0; j < kNearestNeighbours.length; j++){
		for (int i = 0; i < kNearestNeighbours.length; i++){
			neighbour = Integer.parseInt(kNearestNeighbours[i][1]);
			allNeighbours[i] = neighbour;
			if (allNeighbours[j] > allNeighbours[i]){
				tmpNeighbour[0] = kNearestNeighbours[j][0];
				tmpNeighbour[1] = kNearestNeighbours[j][1];
				kNearestNeighbours[j][0] = kNearestNeighbours[i][0];
				kNearestNeighbours[i][0] = tmpNeighbour[0];
				kNearestNeighbours[j][1] = kNearestNeighbours[i][1];
				kNearestNeighbours[i][1] = tmpNeighbour[1];
			}
		}
	}
	
	String[][] outputKNearest = new String[kNearest][2];
	for (int i = 0; i < kNearest; i++){
		if (i < kNearestNeighbours.length){
			outputKNearest[i][0] = kNearestNeighbours[i][0];
			outputKNearest[i][1] = kNearestNeighbours[i][1];
		}
	}
	
//	return kNearestNeighbours;
	return outputKNearest;
}

}
