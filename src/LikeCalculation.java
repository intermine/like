
import java.io.IOException;
import java.io.PrintStream;
//import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.intermine.metadata.Model;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;

import java.lang.Object;
//import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;


public final class LikeCalculation {
	
		private static final String ROOT = "http://beta.flymine.org/beta/service";

	    /**
	     * Perform the query and print the rows of results.
	     * @param args command line arguments
	     * @throws IOException
	     */

		public static void main(String[] args) throws IOException {
	    	
	    	// prompt for gene symbol?
	    	
	        ServiceFactory factory = new ServiceFactory(ROOT);
	        Model model = factory.getModel();
	        PathQuery query = new PathQuery(model);

	     // Select the output columns:
	        query.addViews("Protein.genes.primaryIdentifier",
	                "Protein.genes.symbol",
	                "Protein.proteinDomains.shortName",
	                "Protein.proteinDomains.primaryIdentifier");

	        // Add orderby
	        query.addOrderBy("Protein.genes.symbol", OrderDirection.ASC);

	        // Filter the results with the following constraints:
	        query.addConstraint(Constraints.eq("Protein.organism.shortName", "D. melanogaster"), "A");
	        query.addConstraint(Constraints.eq("Protein.genes.symbol", "z*"), "B");
	        // Specify how these constraints should be combined.
	        query.setConstraintLogic("A and B");
	        
//	        // Select the output columns:
//	        query.addViews("Gene.primaryIdentifier",
//	                "Gene.symbol",
//	                "Gene.proteins.proteinDomains.name",
//	                "Gene.proteins.proteinDomains.primaryIdentifier");

	        /*
	         * Gene.primaryIdentifier: unique so use for calculations
	         * Gene.symbol: human readable
	         * 
	         *  ProteinDomain.name: human readable
	         *  ProteinDomain.primaryIdentifier: unique
	         */
	        
	        // Add orderby
//	        query.addOrderBy("Gene.secondaryIdentifier", OrderDirection.ASC);

	        // Filter the results with the following constraints:
//	        query.addConstraint(Constraints.lookup("Gene", "runt", ""), "A");
//	        query.addConstraint(Constraints.lookup("Gene", "Mad", ""), "A");
//	        query.addConstraint(Constraints.eq("Gene.organism.name", "Drosophila melanogaster"));
	        
//	        query.addConstraint(Constraints.eq("Protein.organism.shortName", "D. melanogaster"), "A");
//	        query.addConstraint(Constraints.eq("Protein.genes.symbol", "*ze*"), "B");
//	        // Specify how these constraints should be combined.
//	        query.setConstraintLogic("A and B");
	        
	        // Specify how these constraints should be combined.
//	        query.setConstraintLogic("A and B");

	        QueryService service = factory.getQueryService();
	        PrintStream out = System.out;  	        
	        String format = "%-22.22s | %-22.22s | %-22.22s | %-22.22s\n";
	        out.printf(format, query.getView().toArray());
	        Iterator<List<Object>> rows = service.getRowListIterator(query);
	        String tmp;
	        String mat = "";
	        String[][] matrix = new String[service.getCount(query)][4];
	        String[][] proDomainMat = new String[service.getCount(query)][2];
	        
	        while (rows.hasNext()) {
	        	List<Object> row = rows.next();
	        	out.printf(format, row.toArray());
	        	for (int i = 0; i < 4 ; i++){
	        		tmp = row.get(i).toString();
	    	        mat = mat + "$" + tmp;
	        	}
	        	
	        	// the gene and the domain ==> matrix
	        }
	        
	        // simplify matrix to 2 columns: Gene.primaryIdentifier & ProteinDomain.primaryIdentifier
	        int[] matCut = new int[matrix.length*matrix[0].length+1]; // !!!!! OutOfBoundary for 0 rows !!!!!
	        matCut[0] = mat.indexOf("$");
	        for (int j = 1; j < (matrix.length*matrix[0].length); j++){
        		matCut[j] = mat.indexOf("$", matCut[j-1] + 1);
        	}
	        matCut[matCut.length-1] = mat.length();
	        
	        int countCut = 0;
	        for ( int k = 0; k < proDomainMat.length; k++){
	        	for (int l = 0; l < proDomainMat[0].length/2; l++){
	        		proDomainMat[k][l] = mat.substring(matCut[countCut]+l+1, matCut[countCut+1]);
	        		countCut += 3;
	        		proDomainMat[k][l+1] = mat.substring(matCut[countCut]+l+1, matCut[countCut+1]);
	        		countCut +=1;
	        	}
	        }
	        
	        out.printf("%d rows\n", service.getCount(query));
	        
	        int uniqueProDom = 102; // !!! find length of geneMat !!!!
	        String[][] geneMat = new String[service.getCount(query)][uniqueProDom];
	        for (int i = 0; i < service.getCount(query); i++){
	        	for (int j = 0; j < query.getView().size()/2; j++) {
	        		geneMat[i][j] = proDomainMat[i][j];
	        	}
	        }
	        
	        // a single column for unique protein domains
	        for (int i = 0; i < service.getCount(query); i++){
	        	int countProDomain = 1;
	        	for (int j = i; j < service.getCount(query); j++){
	        		if (!proDomainMat[i][1].equals(proDomainMat[j][1]) 
	        				&& geneMat[j][countProDomain] != null
	        				){
	        			int count = 0;
		        		countProDomain += 1;
		        		for (int k = j; k < service.getCount(query); k++){
			        		geneMat[j+count][countProDomain] = geneMat[j+count][countProDomain-1];
			        		geneMat[j+count][countProDomain-1] = null;
			        		count += 1;
		        		}
	        		}
	        		else {
	        			int count = 0;
	        			for (int k = 0; k < j; k++){
	        				for (int l = 1; l < uniqueProDom; l++){
	        					if (count == 0 && proDomainMat[j][1].equals(geneMat[k][l])){
	        						for (int m = 1; m < uniqueProDom; m ++){
	        							geneMat[j][m] = null;
	        						}
	        						geneMat[j][l] = proDomainMat[j][1];
	        						count += 1;
	        					}
	        				}
	        			}
	        		}
	        	}
	        }
	        
	        // write all protein domains of 1 gene in 1 row
	        for (int i = 0; i < service.getCount(query); i++){
	        	for (int j = i+1; j < service.getCount(query); j++){
	        		if (geneMat[i][0] != null && geneMat[i][0].equals(geneMat[j][0])){
	        			// transfer equal protein domain
	        			for (int k = 1; k < uniqueProDom; k++){
	        				if (geneMat[j][k] != null && geneMat[i][k] == null){
			        			geneMat[i][k] = geneMat[j][k];
			        			geneMat[j][k] = null;
		        				geneMat[j][0] = null;
        					}
        				}
	        		}
	        	}
	        }
	        
	        // cut all the empty rows
	        int countNotNull = 0;
	        for (int i = 0; i < service.getCount(query); i++){
	        	if (geneMat[i][0] != null){
	        		countNotNull += 1;
	        	}
	        }
	        
	        String[][] geneMatSumUp = new String[countNotNull][uniqueProDom];
	        int countRows = 0;
	        for (int i = 0; i < service.getCount(query); i++){
	        	if (geneMat[i][0] != null){
	        		for (int j = 0; j < uniqueProDom; j++){
	        			geneMatSumUp[countRows][j] = geneMat[i][j];
	        		}
	        		countRows += 1;
	        	}
	        }
	        
	     // prompt for gene symbol (dummy for the UI - normally user would be on a report page for zen)
	       
	        out.print("\nData about Protein Domains: \n");
	        for (int i = 0; i < geneMatSumUp.length; i++) {
	            for (int j = 0; j < geneMatSumUp[0].length; j++) {
	                out.print(geneMatSumUp[i][j] + " ");
	            }
	            out.print("\n");
	        }
	        
	        out.print("service.getCount(query): " + service.getCount(query) + "\ngeneMatSumUp.length:" + geneMatSumUp.length + "\n");
	       
// return similar genes using algorithm
	        
	        // String comparison
	        String[][] simMat = calculate1(geneMatSumUp);
	        out.print("Similarity matrix: \n");
	        for (int i = 0; i < simMat.length; i++) {
	            for (int j = 0; j < simMat[0].length; j++) {
	                out.print(simMat[i][j] + " ");
	            }
	            out.print("\n");
	        }
	    }
		
		
		public static String[][] calculate1(String[][] geneMatSumUp) {
			// Compare geneMatSumUp matrix to all other Genes
			String[][] simMat = new String[geneMatSumUp.length][geneMatSumUp.length];
			
//			String[][] simMat = new String[geneMatSumUp.length+1][geneMatSumUp.length+1];
//			for (int i = 0; i < geneMatSumUp.length; i++){
//				simMat[0][i+1] = geneMatSumUp[i][0];
//				simMat[i+1][0] = geneMatSumUp[0][i];
//			}
			
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
//					simMat[i+1][j+1] = simil;
					simMat[i][j] = simil;
				}				
			}		
			for (int m = 0; m < geneMatSumUp.length; m++){
				simMat[m][m] = "-";
			}
			return simMat;
		}
}
