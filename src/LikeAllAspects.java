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


public class LikeAllAspects {
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
        out.printf(format, query.getView().toArray());
        Iterator<List<Object>> rows = service.getRowListIterator(query);
        String tmp;
        int countRow = 0;
        int countColumn = 0;
        int firstColumn = 0;
        int firstRow = 0;
        String[][] matrixBuild = new String[service.getCount(query)][service.getCount(query)];
        
        while (rows.hasNext()) {
            List<Object> row = rows.next();
        	out.printf(format, row.toArray());
        	for (int i = 0; i < 4 ; i++){
        		tmp = row.get(i).toString();
        		if (tmp.contains("FBgn")){
        			if (firstRow == 0){
        				matrixBuild[0][0] = tmp;
        				firstRow += 1;
        			}
        			else {
        				if (!tmp.equals(matrixBuild[countRow][0])){
        					countRow += 1;
        					matrixBuild[countRow][0] = tmp;
        				}
        			}
        		}
        		if (tmp.contains("IPR0")){
        			if (firstColumn == 0){
        				matrixBuild[0][1] = tmp;
        				firstColumn += 1;
        				countColumn += 1;
        			}
        			else {
        				int tmpColumn = countColumn+1;
        				int saved = 0;
	        			for (int j = 0; j <= countRow; j++){
	        				for (int k = 0; k <= (tmpColumn-1); k++){
	        					if (tmp.equals(matrixBuild[j][k]) 
	        							&& saved == 0){
	        						matrixBuild[countRow][k] = tmp;
	        						saved += 1;
	        					}
	        					else if (j == (countRow) && k == (countColumn-1) 
	        							&& saved == 0){
	        						countColumn += 1;
	        	        			matrixBuild[countRow][countColumn] = tmp;
	        	        			saved += 1;
	        					}
	        				}
	        			}
        			}
        		}
        	}
        }
        
        String[][] matrix = new String[countRow+1][countColumn+1];
        for (int i = 0; i < countRow+1; i++){
        	for (int j = 0; j < countColumn+1; j++){
        		matrix[i][j] = matrixBuild[i][j];
        	}
        }
        
        ServiceFactory factory2 = new ServiceFactory(ROOT);
        Model model2 = factory2.getModel();
        PathQuery query2 = new PathQuery(model2);

     // Select the output columns:
        query2.addViews("Gene.primaryIdentifier",
                "Gene.symbol",
                "Gene.length",
                "Gene.transcripts.primaryIdentifier");

     // Add orderby
        query2.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);

        // Filter the results with the following constraints:
        query2.addConstraint(Constraints.eq("Gene.symbol", "z*"), "A");
        query2.addConstraint(Constraints.eq("Gene.organism.shortName", "D. melanogaster"), "B");
        // Specify how these constraints should be combined.
        query2.setConstraintLogic("A and B");

        QueryService service2 = factory2.getQueryService();
//        String format2 = "%-22.22s | %-22.22s | %-22.22s | %-22.22s\n";
//        out.printf(format2, query2.getView().toArray());
        Iterator<List<Object>> rows2 = service2.getRowListIterator(query2);
        String tmp2;
        int countRow2 = 0;
        int firstRow2 = 0;
        String[][] matrixBuild2 = new String[service.getCount(query2)][2];
    	int countTranscript2 = 0;
        
        while (rows2.hasNext()) {
            List<Object> row2 = rows2.next();
//        	out.printf(format2, row2.toArray());
        	for (int i = 0; i < 4 ; i++){
        		tmp2 = row2.get(i).toString();
        		if (tmp2.contains("FBgn0")){
        			if (firstRow2 == 0){
        				matrixBuild2[0][0] = tmp2;
        				firstRow2 += 1;
        				countTranscript2 += 1;
        				matrixBuild2[countRow2][1] = Integer.toString(countTranscript2);
        			}
        			else {
        				for (int l = 0; l <= countRow2; l++){
        					if (tmp2.equals(matrixBuild2[l][0])){
        						countTranscript2 += 1;
        						matrixBuild2[countRow2][1] = Integer.toString(countTranscript2);
        					}
        					else if (l == countRow2){
        						countRow2 += 1;
        						matrixBuild2[countRow2][0] = tmp2;
        						matrixBuild2[countRow2-1][1] = Integer.toString(countTranscript2);
        						countTranscript2 = 0;
        					}
        				}
        			}
        		}
        	}
        }
        
        String[][] matrix2 = new String[countRow2+1][2];
        for (int i = 0; i < countRow2+1; i++){
        	for (int j = 0; j < 2; j++){
        		matrix2[i][j] = matrixBuild2[i][j];
        	}
        }
        
        ServiceFactory factory3 = new ServiceFactory(ROOT);
        Model model3 = factory3.getModel();
        PathQuery query3 = new PathQuery(model3);

     // Select the output columns:
        query3.addViews("Gene.primaryIdentifier",
                "Gene.symbol",
                "Gene.length");

     // Add orderby
        query3.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);

        // Filter the results with the following constraints:
        query3.addConstraint(Constraints.eq("Gene.symbol", "z*"), "A");
        query3.addConstraint(Constraints.eq("Gene.organism.shortName", "D. melanogaster"), "B");
        // Specify how these constraints should be combined.
        query3.setConstraintLogic("A and B");

        QueryService service3 = factory3.getQueryService();
//        String format3 = "%-30.30s | %-30.30s | %-30.30s\n";
//        out.printf(format3, query3.getView().toArray());
        Iterator<List<Object>> rows3 = service3.getRowListIterator(query3);
        String tmp3;
        int countRow3 = 0;
        int firstRow3 = 0;
        String[][] matrixBuild3 = new String[service.getCount(query3)][2];
        
        while (rows3.hasNext()) {
            List<Object> row3 = rows3.next();
//        	out.printf(format3, row3.toArray());
        	for (int i = 0; i < 3 ; i++){
        		tmp3 = row3.get(i).toString();
        		if (tmp3.contains("FBgn")){
        			if (firstRow3 == 0){
        				matrixBuild3[0][0] = tmp3;
        				firstRow3 += 1;
        			}
        			else {
        				countRow3 += 1;
        				matrixBuild3[countRow3][0] = tmp3;
        				}
        			}
        		if (!tmp3.contains("[a-zA-Z]+")){
        			matrixBuild3[countRow3][1] = tmp3;
        		}
        	}
        }
        
        String[][] matrix3 = new String[countRow3+1][2];
        for (int i = 0; i < countRow3+1; i++){
        	for (int j = 0; j < 2; j++){
        		matrix3[i][j] = matrixBuild3[i][j];
        	}
        }
        
////////////////////////////////////////////////////////////////////////////////
        
        out.print("Protein Domains: \n");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                out.print(matrix[i][j] + " ");
            }
            out.print("\n");
        }
        String searchedGene = "FBgn0004606";
        
        String[][] simMat = calculateMatrix(matrix);
        out.print("Normalised Number of Protein Domains in common: \n");
        for (int i = 0; i < simMat.length; i++) {
            for (int j = 0; j < simMat[0].length; j++) {
                out.print(simMat[i][j] + " ");
            }
            out.print("\n");
        }
        
        String[][] normMat = normalize(simMat);
        out.print("Normalised Number of Protein Domains in common: \n");
        for (int i = 0; i < normMat.length; i++) {
            for (int j = 0; j < normMat[0].length; j++) {
                out.print(normMat[i][j] + " ");
            }
            out.print("\n");
        }
        
        String[][] transcriptsDif = calculateDifference(matrix2);
        out.print("\nNumber of Transcripts Differences: \n");
        for (int i = 0; i < transcriptsDif.length; i++) {
        	for (int j = 0; j < transcriptsDif[0].length; j++) {
                out.print(transcriptsDif[i][j] + " ");
            }
            out.print("\n");
        }
        String[][] normTranscriptsDif = normalizeAndReverse(matrix2,transcriptsDif);
        out.print("\nReversed and Normalized Number of Transcripts Differences: \n");
        for (int i = 0; i < normTranscriptsDif.length; i++) {
        	for (int j = 0; j < normTranscriptsDif[0].length; j++) {
                out.print(normTranscriptsDif[i][j] + " ");
            }
            out.print("\n");
        }
        
        String[][] lengthDif = calculateDifference(matrix3);
        out.print("\nLength Differences: \n");
        for (int i = 0; i < lengthDif.length; i++) {
        	for (int j = 0; j < lengthDif[0].length; j++) {
                out.print(lengthDif[i][j] + " ");
            }
            out.print("\n");
        }
//        
        String[][] normLengthDif = normalizeAndReverse(matrix3,lengthDif);
        out.print("\nReversed and Normalized Length Differences: \n");
        for (int i = 0; i < normLengthDif.length; i++) {
        	for (int j = 0; j < normLengthDif[0].length; j++) {
                out.print(normLengthDif[i][j] + " ");
            }
            out.print("\n");
        }
        
        String[][] kNearestNeighbours = findKNearestNeighbours(normMat, normTranscriptsDif, normLengthDif, searchedGene, 20);
        out.print("\nK-Nearest Neighbours of " + searchedGene +": \n");
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
    		simMat[m][m] = null;
    	}
    	return simMat;
    }	

    public static String[][] normalize(String[][] matrix){
    	String[][] normMatrix = new String[matrix.length][matrix.length];
    	int highestVal = 0;
    	int tmpI;
    	float tmpJ;
    	String normDif;
    	
    	for (int i = 0; i < matrix.length; i++){
    		normMatrix[0][i] = matrix[i][0];
    		normMatrix[i][0] = matrix[i][0];
    		for (int j = 1; j < matrix.length; j++){
    			if (i > 0 && i != j && Integer.parseInt(matrix[i][j]) > highestVal){
    				highestVal = Integer.parseInt(matrix[i][j]);
    			}
    		}
    	}
    	
    	for (int i = 1; i < matrix.length; i++){
    		for (int j = 1; j < matrix.length; j++){
    			if (i != j){
    				tmpI = Integer.parseInt(matrix[i][j]);
    				tmpJ = (float)tmpI/(float)highestVal;
    				normDif = Float.toString(tmpJ);
    				normMatrix[i][j] = normDif;
    			}
    		}
    	}
    	return normMatrix;
    }
    
    public static String[][] calculateDifference(String[][] matrix){
    	String[][] matrixDif = new String[matrix.length+1][matrix.length+1];
    	
    	for (int i = 0; i < matrix.length; i++){
    		matrixDif[0][i+1] = matrix[i][0];
    		matrixDif[i+1][0] = matrix[i][0];
    	}
    	
    	for (int i = 0; i < matrix.length; i++){
    		for (int j = 0; j < matrix.length; j++){
    			if (i != j){
    				matrixDif[i+1][j+1] = Integer.toString(Math.abs(Integer.parseInt(matrix[i][1])-Integer.parseInt(matrix[j][1])));
    			}
    		}
    	}
    	return matrixDif;
    }   
        
    public static String[][] orderByHighestValues(String[][] mat2Columns){
    	int[] allNeighbours = new int[mat2Columns.length];
    	int neighbour = Integer.parseInt(mat2Columns[0][1]);
    	allNeighbours[0] = neighbour;
    	String[] tmpNeighbour = new String[2];
    	
    	for (int j = 0; j < mat2Columns.length; j++){
    		for (int i = 0; i < mat2Columns.length; i++){
    			neighbour = Integer.parseInt(mat2Columns[i][1]);
    			allNeighbours[i] = neighbour;
    			if (allNeighbours[j] > allNeighbours[i]){
    				tmpNeighbour[0] = mat2Columns[j][0];
    				tmpNeighbour[1] = mat2Columns[j][1];
    				mat2Columns[j][0] = mat2Columns[i][0];
    				mat2Columns[i][0] = tmpNeighbour[0];
    				mat2Columns[j][1] = mat2Columns[i][1];
    				mat2Columns[i][1] = tmpNeighbour[1];
    			}
    		}
    	}
    	return mat2Columns;
    }

    public static String[][] normalizeAndReverse(String[][] mat2Columns, String[][] matrix){
    	String[][] normMatrix = new String[matrix.length][matrix.length];
    	String[][] orderedMat = orderByHighestValues(mat2Columns);
    	int highestVal = Integer.parseInt(orderedMat[0][1]);//-Integer.parseInt(orderedMat[orderedMat.length-1][1]);
    	
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
    
    public static String[][] findKNearestNeighbours(String[][] commonProteinDomains, String[][] numberTranscriptsDif,
    		String[][] lengthDif, String searchedGene, int kNearest){
    	// return the k-nearest neighbours for 1 gene
    	String[][] neighbours = new String[commonProteinDomains.length-1][2];
    	String[][] neighbours1 = new String[commonProteinDomains.length-1][2];
    	String[][] neighbours2 = new String[numberTranscriptsDif.length-1][2];
    	String[][] neighbours3 = new String[lengthDif.length-1][2];
    	float[] allNeighbours = new float[commonProteinDomains.length];
    	int tmp1 = -1;
    	int tmp2 = -1;
    	int tmp3 = -1;
    	
    	for (int i = 1; i < commonProteinDomains.length; i++){
    		if (tmp1 < 0 && commonProteinDomains[i][0].equals(searchedGene)){
    			tmp1 = i;
    		}
    	}
    	for (int i = 1; i < numberTranscriptsDif.length; i++){
    		if (tmp2 < 0 && numberTranscriptsDif[i][0].equals(searchedGene)){
    			tmp2 = i;
    		}
    	}
    	for (int i = 1; i < lengthDif.length; i++){
    		if (tmp3 < 0 && lengthDif[i][0].equals(searchedGene)){
    			tmp3 = i;
    		}
    	}
    	
    	for (int i = 1; i < commonProteinDomains.length; i++){
    		if (commonProteinDomains[tmp1][i] != null){
    			neighbours1[i-1][1] = commonProteinDomains[tmp1][i];
    		} 
    		if (numberTranscriptsDif[tmp2][i] != null){
    			neighbours2[i-1][1] = numberTranscriptsDif[tmp2][i];
    		} 
    		if (lengthDif[tmp3][i] != null){
    			neighbours3[i-1][1] = lengthDif[tmp3][i];
    		} 
    		neighbours1[i-1][0] = commonProteinDomains[0][i];
    		neighbours2[i-1][0] = numberTranscriptsDif[0][i];
    		neighbours3[i-1][0] = lengthDif[0][i];
    	}
    	
    	int count = 0;
    	for (int i = 0; i < commonProteinDomains.length-1; i ++){
    		if (neighbours1[i][1] == null) {
				count += 1;
    		}
    		else {
    		for (int j = 0; j < numberTranscriptsDif.length-1; j++){
    			for (int k = 0; k < lengthDif.length-1; k++){
    				if (neighbours1[i][0].equals(neighbours2[j][0])
    						&& neighbours1[i][0].equals(neighbours3[k][0])){
    					neighbours[i-count][0] = neighbours1[i][0];
    					neighbours[i-count][1] = Float.toString(Float.parseFloat(neighbours1[i][1])
    												+ Float.parseFloat(neighbours2[j][1])
    												+ Float.parseFloat(neighbours3[k][1]));
    				}
    			}
    		}
    		}
    	}
    	
    	float neighbour = Float.parseFloat(neighbours[0][1]);
    	allNeighbours[0] = neighbour;
    	String[] tmpNeighbour = new String[2];
    	
    	for (int j = 0; j < neighbours.length; j++){
    		for (int i = 0; i < neighbours.length; i++){
    			if (neighbours[i][0] != null
    						&& neighbours[i][1] != null){
	    			neighbour = Float.parseFloat(neighbours[i][1]);
	    			allNeighbours[i] = neighbour;
	    			if (allNeighbours[j] > allNeighbours[i]){
	    				tmpNeighbour[0] = neighbours[j][0];
	    				tmpNeighbour[1] = neighbours[j][1];
	    				neighbours[j][0] = neighbours[i][0];
	    				neighbours[i][0] = tmpNeighbour[0];
	    				neighbours[j][1] = neighbours[i][1];
	    				neighbours[i][1] = tmpNeighbour[1];
    			}
    			}
    		}
    	}
    	
    	System.out.print("\nneighbours: " + neighbours.length);
    	
    	String[][] kNearestNeighbours = new String[kNearest][2];
    	for (int i = 0; i < kNearest; i++){
    		if (i < neighbours.length){
    			kNearestNeighbours[i][0] = neighbours[i][0];
    			kNearestNeighbours[i][1] = neighbours[i][1];
    		}
    	}
//    	return neighbours;
    	return kNearestNeighbours;
    }

}