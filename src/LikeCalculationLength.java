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


public class LikeCalculationLength {
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

     // Select the output columns:
        query.addViews("Gene.primaryIdentifier",
                "Gene.symbol",
                "Gene.length");

        // Add orderby
        query.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);

        // Filter the results with the following constraints:
        query.addConstraint(Constraints.eq("Gene.organism.shortName", "D. melanogaster"), "A");
        query.addConstraint(Constraints.eq("Gene.symbol", "z*"), "B");
        // Specify how these constraints should be combined.
        query.setConstraintLogic("A and B");

        QueryService service = factory.getQueryService();
        PrintStream out = System.out;
        String format = "%-30.30s | %-30.30s | %-30.30s\n";
        out.printf(format, query.getView().toArray());
        Iterator<List<Object>> rows = service.getRowListIterator(query);
        String tmp;
        int countRow = 0;
        int firstRow = 0;
        int uniqueGene = 33; // !!!! how many unique genes exist in the query !!!!
        String[][] matrix = new String[uniqueGene][2];
        
        while (rows.hasNext()) {
            List<Object> row = rows.next();
        	out.printf(format, row.toArray());
        	for (int i = 0; i < 3 ; i++){
        		tmp = row.get(i).toString();
        		if (tmp.contains("FBgn")){
        			if (firstRow == 0){
        				matrix[0][0] = tmp;
        				firstRow += 1;
        			}
        			else {
        				countRow += 1;
        				matrix[countRow][0] = tmp;
        				}
        			}
        		
        		if (!tmp.contains("[a-zA-Z]+")){
        			matrix[countRow][1] = tmp;
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

        String[][] transcriptsDif = calculateLengthDifference(matrix);
        out.print("Number of Length Differences: \n");
        for (int i = 0; i < transcriptsDif.length; i++) {
        	for (int j = 0; j < transcriptsDif[0].length; j++) {
                out.print(transcriptsDif[i][j] + " ");
            }
            out.print("\n");
        }
        String[][] normLengthDif = normalizeAndReverse(matrix,transcriptsDif);
        out.print("Normalized and Reversed Number of Length Differences: \n");
        for (int i = 0; i < normLengthDif.length; i++) {
        	for (int j = 0; j < normLengthDif[0].length; j++) {
                out.print(normLengthDif[i][j] + " ");
            }
            out.print("\n");
        }
		
        
//        String[][] numberOfTranscripts = orderByLength(matrix);
//        out.print("numberOfTranscripts: \n");
//        for (int i = 0; i < numberOfTranscripts.length; i++) {
//        	for (int j = 0; j < numberOfTranscripts[0].length; j++) {
//                out.print(numberOfTranscripts[i][j] + " ");
//            }
//            out.print("\n");
//        }
}

    public static String[][] calculateLengthDifference(String[][] matrix){
    	String[][] matrixDif = new String[matrix.length+1][matrix.length+1];
    	
    	for (int i = 0; i < matrix.length; i++){
    		matrixDif[0][i+1] = matrix[i][0];
    		matrixDif[i+1][0] = matrix[i][0];
    	}
    	
    	for (int i = 0; i < matrix.length; i++){
    		for (int j = 0; j < matrix.length; j++){
    			matrixDif[i+1][j+1] = Integer.toString(Math.abs(Integer.parseInt(matrix[i][1])-Integer.parseInt(matrix[j][1])));
    		}
    	}
    	
    	return matrixDif;
    }
    
    public static String[][] normalizeAndReverse(String[][] mat2Columns, String[][] matrix){
    	String[][] normMatrix = new String[matrix.length][matrix.length];
    	String[][] orderedMat = orderByLength(mat2Columns);
    	int highestVal = Integer.parseInt(orderedMat[0][1])-Integer.parseInt(orderedMat[orderedMat.length-1][1]);
    	
    	for (int i = 0; i < matrix.length; i++){
    		normMatrix[0][i] = matrix[i][0];
    		normMatrix[i][0] = matrix[i][0];
    	}
    	
    	for (int i = 1; i < matrix.length; i++){
    		for (int j = 1; j < matrix.length; j++){
    			if (i != j){
    				normMatrix[i][j] = Float.toString((float)1 - (float)Integer.parseInt(matrix[i][j])/(float)highestVal);
    			}
    		}
    	}
    	return normMatrix;
    }

    public static String[][] orderByLength(String[][] matrix){
	int[] allNeighbours = new int[matrix.length];
	int neighbour = Integer.parseInt(matrix[0][1]);
	allNeighbours[0] = neighbour;
	String[] tmpNeighbour = new String[2];
	
	for (int j = 0; j < matrix.length; j++){
		for (int i = 0; i < matrix.length; i++){
			neighbour = Integer.parseInt(matrix[i][1]);
			allNeighbours[i] = neighbour;
			if (allNeighbours[j] > allNeighbours[i]){
				tmpNeighbour[0] = matrix[j][0];
				tmpNeighbour[1] = matrix[j][1];
				matrix[j][0] = matrix[i][0];
				matrix[i][0] = tmpNeighbour[0];
				matrix[j][1] = matrix[i][1];
				matrix[i][1] = tmpNeighbour[1];
			}
		}
	}
	return matrix;
}
}
