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


public class LikeFingerprint {
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
        // TODO make outer joins
        // Select the output columns:
        query.addViews("Gene.primaryIdentifier",
                "Gene.symbol",
                "Gene.proteins.proteinDomains.primaryIdentifier",
                "Gene.proteins.proteinDomains.shortName",
                "Gene.transcripts.primaryIdentifier",
                "Gene.length");
        
//        query.setOuterJoinStatus("Gene.transcripts", OuterJoinStatus.OUTER);
//        query.setOuterJoinStatus("Gene.proteins", OuterJoinStatus.OUTER);
        
        // Add orderby
        query.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);

        // Filter the results with the following constraints:
        query.addConstraint(Constraints.eq("Gene.organism.name", "Drosophila melanogaster"), "A");
        query.addConstraint(Constraints.eq("Gene.symbol", "z*"), "B");
        // Specify how these constraints should be combined.
        query.setConstraintLogic("A and B");

        QueryService service = factory.getQueryService();
        PrintStream out = System.out;
//        String format = "%-13.13s | %-13.13s | %-13.13s | %-13.13s | %-13.13s | %-13.13s\n";
//        out.printf(format, query.getView().toArray());
        
        // Calculation for the Common Protein Domains
        Iterator<List<Object>> rows = service.getRowListIterator(query);
        String tmp;
        int countRow = 0;
        int countColumn = 0;
        int countColumnT = 0;
        int firstColumn = 0;
        int firstColumnT = 0;
        int firstRow = 0;
        String[][] matrixBuild = new String[service.getCount(query)][service.getCount(query)];
        String[][] matrixBuild2 = new String[service.getCount(query)][service.getCount(query)];
        String[][] matrixBuild3 = new String[service.getCount(query)][2];
        
        while (rows.hasNext()) {
            List<Object> row = rows.next();
//        	out.printf(format, row.toArray());            
        	for (int i = 0; i < 6; i++){
        		tmp = row.get(i).toString();
        		if (tmp.contains("FBgn")){
        			if (firstRow == 0){
        				matrixBuild[0][0] = tmp;
        				matrixBuild2[0][0] = tmp;
        				matrixBuild3[0][0] = tmp;
        				firstRow += 1;
        			}
        			else {
        				int saved = 0;
	        			for (int j = 0; j <= countRow; j++){
	        					if (tmp.equals(matrixBuild[j][0]) 
	        							&& saved == 0){
	        						saved += 1;
	        					}
	        					else if (j == countRow && saved == 0){
	        						countRow += 1;
	        	        			matrixBuild[countRow][0] = tmp;
	        	        			matrixBuild2[countRow][0] = tmp;
	        	        			matrixBuild3[countRow][0] = tmp;
	        	        			saved += 1;
	        					}
	        			}
        			}
        		}
        		
        		if (tmp.contains("IPR")){
        			if (firstColumn == 0){
        				matrixBuild[0][1] = tmp;
        				firstColumn = 1;
        				countColumn += 1;
        			}
        			else {
        				int tmpColumn = countColumn+1;
        				int saved = 0;
	        			for (int j = 0; j <= countRow+1; j++){
	        				for (int k = 0; k <= (tmpColumn); k++){
	        					if (tmp.equals(matrixBuild[j][k]) 
	        							&& saved == 0){
	        						matrixBuild[countRow][k] = tmp;
	        						saved += 1;
	        					}
	        					else if (j == (countRow+1) && k == (countColumn) 
	        							&& saved == 0){
	        						countColumn += 1;
	        	        			matrixBuild[countRow][countColumn] = tmp;
	        	        			saved += 1;
	        					}
	        				}
	        			}
        			}
        		}
        		
        		if (tmp.contains("FBtr")){
        			if (firstColumnT == 0){
        				matrixBuild2[0][1] = tmp;
        				firstColumnT = 1;
        				countColumnT += 1;
        			}
        			else {
        				int tmpColumn = countColumnT+1;
        				int saved = 0;
	        			for (int j = 0; j <= countRow+1; j++){
	        				for (int k = 0; k <= (tmpColumn); k++){
	        					if (tmp.equals(matrixBuild2[j][k]) 
	        							&& saved == 0){
	        						matrixBuild2[countRow][k] = tmp;
	        						saved += 1;
	        					}
	        					else if (j == (countRow+1) && k == (countColumnT) 
	        							&& saved == 0){
	        						countColumnT += 1;
	        	        			matrixBuild2[countRow][countColumnT] = tmp;
	        	        			saved += 1;
	        					}
	        				}
	        			}
        			}
        		}
        		
        		if (tmp.matches("[0-9]+")){
        			matrixBuild3[countRow][1] = tmp;
        		}
        	}
        }
        
        String[][] matrix = new String[countRow+1][countColumn+1];
        for (int i = 0; i < countRow+1; i++){
        	for (int j = 0; j < countColumn+1; j++){
        		matrix[i][j] = matrixBuild[i][j];
        	}
        }
        
        String[][] matrix2 = new String[countRow+1][countColumnT+1];
        for (int i = 0; i < countRow+1; i++){
        	for (int j = 0; j < countColumnT+1; j++){
        		matrix2[i][j] = matrixBuild2[i][j];
        	}
        }
        
        String[][] matrix3 = new String[countRow+1][2];
        for (int i = 0; i < countRow+1; i++){
        	for (int j = 0; j < 2; j++){
        		matrix3[i][j] = matrixBuild3[i][j];
        	}
        }
        
//        // Outputs of data storehouse results
//        out.print("\nData about Protein Domains: \n");
//        for (int i = 0; i < matrix.length; i++) {
//            for (int j = 0; j < matrix[0].length; j++) {
//                out.print(matrix[i][j] + " ");
//            }
//            out.print("\n");
//        }
//        out.print("\nData about Transcripts: \n");
//        for (int i = 0; i < matrix2.length; i++) {
//            for (int j = 0; j < matrix2[0].length; j++) {
//                out.print(matrix2[i][j] + " ");
//            }
//            out.print("\n");
//        }
//        out.print("\nData about Gene Length: \n");
//        for (int i = 0; i < matrix3.length; i++) {
//            for (int j = 0; j < matrix3[0].length; j++) {
//                out.print(matrix3[i][j] + " ");
//            }
//            out.print("\n");
//        }

        // Similarity Calculations
        String[][] simMat = calculateMatrix(matrix);
        out.print("\nNumber of Protein Domains in common: \n");
        for (int i = 0; i < simMat.length; i++) {
            for (int j = 0; j < simMat[0].length; j++) {
                out.print(simMat[i][j] + " ");
            }
            out.print("\n");
        }
//        String[][] normMat = normalize(simMat);
//        out.print("Normalised Number of Protein Domains in common: \n");
//        for (int i = 0; i < normMat.length; i++) {
//            for (int j = 0; j < normMat[0].length; j++) {
//                out.print(normMat[i][j] + " ");
//            }
//            out.print("\n");
//        }
        
        String[][] simMat2 = calculateMatrix2(matrix2);
        String[][] transcriptsDif = calculateDifference(simMat2);
        out.print("\nNumber of Transcripts Differences: \n");
        for (int i = 0; i < transcriptsDif.length; i++) {
        	for (int j = 0; j < transcriptsDif[0].length; j++) {
                out.print(transcriptsDif[i][j] + " ");
            }
            out.print("\n");
        }
//        String[][] normTranscriptsDif = normalizeAndReverse(simMat2,transcriptsDif);
//        out.print("\nReversed and Normalized Number of Transcripts Differences: \n");
//        for (int i = 0; i < normTranscriptsDif.length; i++) {
//        	for (int j = 0; j < normTranscriptsDif[0].length; j++) {
//                out.print(normTranscriptsDif[i][j] + " ");
//            }
//            out.print("\n");
//        }
        
        String[][] lengthDif = calculateDifference(matrix3);
        out.print("\nLength Differences: \n");
        for (int i = 0; i < lengthDif.length; i++) {
        	for (int j = 0; j < lengthDif[0].length; j++) {
                out.print(lengthDif[i][j] + " ");
            }
            out.print("\n");
        }
        
//        String[][] normLengthDif = normalizeAndReverse(matrix3,lengthDif);
//        out.print("\nReversed and Normalized Length Differences: \n");
//        for (int i = 0; i < normLengthDif.length; i++) {
//        	for (int j = 0; j < normLengthDif[0].length; j++) {
//                out.print(normLengthDif[i][j] + " ");
//            }
//            out.print("\n");
//        }
        
        String searchedGene = "FBgn0004053";
        String searchedGene2 = "FBgn0004606";
        int kNearest = 11;
//        String[][] kNearestNeighbours = findKNearestNeighbours(normMat, normTranscriptsDif, normLengthDif, searchedGene, kNearest);
//        out.print("\nK-Nearest Neighbours of " + searchedGene +": \n");
//        for (int i = 0; i < kNearestNeighbours.length; i++) {
//        	for (int j = 0; j < kNearestNeighbours[0].length; j++) {
//                out.print(kNearestNeighbours[i][j] + " ");
//            }
//            out.print("\n");
//        }
        
        String[][] fingerprint1 = fingerprint(matrix, simMat2, matrix3, simMat, transcriptsDif, lengthDif, searchedGene, kNearest);
        out.print("\nFingerprint of " + searchedGene + ": \n");
        for (int i = 0; i < fingerprint1.length; i++) {
            for (int j = 0; j < fingerprint1[0].length; j++) {
                out.print(fingerprint1[i][j] + " ");
            }
            out.print("\n");
        }
        
        String[][] fingerprint2 = fingerprint(matrix, simMat2, matrix3, simMat, transcriptsDif, lengthDif, searchedGene2, kNearest);
        out.print("\nFingerprint of " + searchedGene2 + ": \n");
        for (int i = 0; i < fingerprint2.length; i++) {
            for (int j = 0; j < fingerprint2[0].length; j++) {
                out.print(fingerprint2[i][j] + " ");
            }
            out.print("\n");
        }
        
        String[][] comparison = compareFingerprints(fingerprint1, fingerprint2);
        out.print("\nOverlapping of " + searchedGene + " and " + searchedGene2 + ": \n");
        for (int i = 0; i < comparison.length; i++) {
            for (int j = 0; j < comparison[0].length; j++) {
                out.print(comparison[i][j] + " ");
            }
            out.print("\n");
        }
    }
    
//    LOG.info("Found " + chromosomes.size() + " chromosomes with sequence, took "
//            + (System.currentTimeMillis() - startTime) + " ms."); 

    
    public static String[][] calculateMatrix(String[][] matrix) {
    	// Compare matrix matrix to all other Genes
    	
    	String[][] simMat = new String[matrix.length+1][matrix.length+1];
    	for (int i = 0; i < matrix.length; i++){
    		simMat[0][i+1] = matrix[i][0];
    		simMat[i+1][0] = matrix[i][0];
    	}
    	
    	for (int i = 0; i < matrix.length; i++){
    		for (int j = 0; j < matrix.length; j++){
    			int count = 0;
    			for (int k = 1; k < matrix[0].length; k++){
    				for (int l = 1; l < matrix[0].length; l++){
    					if (matrix[i][k] != null && matrix[i][k].equals(matrix[j][l])){
    						count += 1;
    					}
    				}
    			}
    			String simil = Integer.toString(count);
    			simMat[i+1][j+1] = simil;
    		}				
    	}		
    	for (int m = 0; m <= matrix.length; m++){
    		simMat[m][m] = null;
    	}
    	return simMat;
    }	

    public static String[][] calculateMatrix2(String[][] matrix2){
    	String[][] simMat2 = new String[matrix2.length][2];
    	
    	for (int i = 0; i < matrix2.length; i++){
    		simMat2[i][0] = matrix2[i][0];
    		int count = 0;
    		for (int j = 1; j < matrix2[i].length; j++){
    			if (matrix2[i][j] != null){
    				count += 1;
    			}
    		}
    		simMat2[i][1] = Integer.toString(count);
    	}
    	
    	return simMat2;
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
    	String[] tmpNeighbour = new String[2];
    	
    	for (int i = 0; i < mat2Columns.length; i++){
    		allNeighbours[i] = Integer.parseInt(mat2Columns[i][1]);
    	}
    	
    	for (int i = 0; i < mat2Columns.length; i++){
    		for (int j = i; j < mat2Columns.length; j++){
    			if (allNeighbours[i] < allNeighbours[j]){
    				tmpNeighbour[0] = mat2Columns[j][0];
    				tmpNeighbour[1] = mat2Columns[j][1];
    				allNeighbours[j] = allNeighbours[i];
    				allNeighbours[i] = Integer.parseInt(mat2Columns[j][1]);
    				mat2Columns[j][0] = mat2Columns[i][0];
    				mat2Columns[j][1] = mat2Columns[i][1];
    				mat2Columns[i][0] = tmpNeighbour[0];
    				mat2Columns[i][1] = tmpNeighbour[1];
    			}
    		}
    	}
    	return mat2Columns;
    }
    
    public static String[][] orderByLowestValues(String[][] mat2Columns){
    	int[] allNeighbours = new int[mat2Columns.length];
    	int neighbour = Integer.parseInt(mat2Columns[0][1]);
    	allNeighbours[0] = neighbour;
    	String[] tmpNeighbour = new String[2];
    	
    	for (int j = 0; j < mat2Columns.length; j++){
    		for (int i = 0; i < mat2Columns.length; i++){
    			neighbour = Integer.parseInt(mat2Columns[i][1]);
    			allNeighbours[i] = neighbour;
    			if (allNeighbours[j] < allNeighbours[i]){
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
    
    public static String[][] orderByTotalHighestDifference(String[][] tmpPD, String[][] tmpPDtotal){
    	// order tmpPD after tmpPDtotal
    			int[] countEqual = new int[tmpPD.length];
    			int countRow = 0;
    	    	int countTmp = 1;
    	    	int countItems = 0;
    	   		for (int l = 0; l < tmpPD.length-1; l++){
    	   			if (tmpPD[l][1].equals(tmpPD[l+1][1])){
    					countTmp += 1;
    					countItems += 1;
    					countEqual[countRow] = countTmp;
    				}
    				else {
    					countEqual[countRow] = countTmp;
    					countTmp = 1;
    					countRow += 1;
    					countItems += 1;
    				}
//    				else {
//    	   				countEqual[countRow+1] = 1;
//    	   			}
    	    	}
    	   		if (countItems < tmpPD.length){
    	   			countEqual[countRow] = 1;
    	   		}
    	   		int countE = 0;
    	   		for (int i = 0; i < countEqual.length; i ++){
    	   			if (countEqual[i] != 0){
    	   				countE += 1;
    	   			}
    	   		}
    	   		int countSub = 0;
    	   		String[][] tmpPDtotal2 = new String[tmpPDtotal.length][2];
    	   		for (int i = 0; i < countE; i++){
    	   			String[][] subStringPDtotal = new String[countEqual[i]][2];
    	   			int countSubTmp = countSub;
    	   			for (int j = 0; j < countEqual[i]; j++){
    	   				subStringPDtotal[j][0] = tmpPDtotal[countSubTmp+j][0];
    	   				subStringPDtotal[j][1] = tmpPDtotal[countSubTmp+j][1];
    	   				countSub += 1;
    	   			}
    	   			for (int j = 0; j < subStringPDtotal.length; j++){
    	   	   			subStringPDtotal[j][1] = Integer.toString((Integer.parseInt(subStringPDtotal[j][1])
    	   	   					-Integer.parseInt(tmpPDtotal[0][1])));
    	   			}
    	   			subStringPDtotal = orderByHighestValues(subStringPDtotal);
    	   			for (int j = countSubTmp; j < subStringPDtotal.length+countSubTmp; j++){
    	   				for (int k = 0; k < tmpPDtotal.length; k ++){
    	   					if (subStringPDtotal[j-countSubTmp][0].equals(tmpPDtotal[k][0])){
    	   						tmpPDtotal2[j][1] = tmpPDtotal[k][1];
    	   					}
    	   				}
    		   			tmpPDtotal2[j][0] = subStringPDtotal[j-countSubTmp][0];
    	   			}
    	   		}
    	    	tmpPDtotal = tmpPDtotal2;
    	    	
    	    	return tmpPDtotal;
    }
    
    public static String[][] orderByTotalLowestDifference(String[][] tmpPD, String[][] tmpPDtotal){
    	// order tmpPD after tmpPDtotal
    			int[] countEqual = new int[tmpPD.length];
    			int countRow = 0;
    	    	int countTmp = 1;
    	   		for (int l = 0; l < tmpPD.length-1; l++){
    	   			if (tmpPD[l][1].equals(tmpPD[l+1][1])){
    					countTmp += 1;
    					countEqual[countRow] = countTmp;
    				}
//    	   			else if (l == tmpPD.length-1){
//    	   				countEqual[countRow] = 1;
//    	   			}
    				else {
    					countEqual[countRow] = countTmp;
    					countTmp = 1;
    					countRow += 1;
    				}
    	    	}
    	   		int countE = 0;
    	   		for (int i = 0; i < countEqual.length; i ++){
    	   			if (countEqual[i] != 0){
    	   				countE += 1;
    	   			}
    	   		}
    	   		int countSub = 0;
    	   		String[][] tmpPDtotal2 = new String[tmpPDtotal.length][2];
    	   		for (int i = 0; i < countE; i++){
    	   			String[][] subStringPDtotal = new String[countEqual[i]][2];
    	   			int countSubTmp = countSub;
    	   			for (int j = 0; j < countEqual[i]; j++){
    	   				subStringPDtotal[j][0] = tmpPDtotal[countSubTmp+j][0];
    	   				subStringPDtotal[j][1] = tmpPDtotal[countSubTmp+j][1];
    	   				countSub += 1;
    	   			}
    	   			for (int j = 0; j < subStringPDtotal.length; j++){
    	   	   			subStringPDtotal[j][1] = Integer.toString((Integer.parseInt(subStringPDtotal[j][1])
    	   	   					-Integer.parseInt(tmpPDtotal[0][1])));
    	   			}
    	   			subStringPDtotal = orderByLowestValues(subStringPDtotal);
    	   			for (int j = countSubTmp; j < subStringPDtotal.length+countSubTmp; j++){
    	   				for (int k = 0; k < tmpPDtotal.length; k ++){
    	   					if (subStringPDtotal[j-countSubTmp][0].equals(tmpPDtotal[k][0])){
    	   						tmpPDtotal2[j][1] = tmpPDtotal[k][1];
    	   					}
    	   				}
    		   			tmpPDtotal2[j][0] = subStringPDtotal[j-countSubTmp][0];
    	   			}
    	   		}
    	    	tmpPDtotal = tmpPDtotal2;
    	    	
    	    	return tmpPDtotal;
    }
    
    public static String[][] normalizeAndReverse(String[][] mat2Columns, String[][] matrix){
    	String[][] normMatrix = new String[matrix.length][matrix.length];
    	String[][] mat2Columns2 = new String[mat2Columns.length][2];
    	
    	for (int i = 0; i < matrix.length-1; i++){
    		normMatrix[0][i+1] = matrix[i+1][0];
    		normMatrix[i+1][0] = matrix[i+1][0];
    		for (int j = 0; j < 2; j ++){
    			mat2Columns2[i][j] = mat2Columns[i][j];
    		}
    	}
    	
    	String[][] orderedMat = orderByHighestValues(mat2Columns2);
    	int highestVal = Integer.parseInt(orderedMat[0][1]);
    	
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
    
    public static String[][] fingerprint(String[][] matrix, String[][] simMat2, String[][] matrix3,
    							String[][] simMat, String[][] transcriptsDif,
    							String[][] lengthDif, String searchedGene, int kNearest){
    	// generate a fingerprint
    	String[][] fingerprint = new String[matrix.length+1][15];
    	String[][] tmpPD = new String[simMat.length-2][2];
    	String[][] tmpT = new String[transcriptsDif.length-2][2];
    	String[][] tmpL = new String[lengthDif.length-2][2];
    	String[][] tmpPDtotal = new String[fingerprint.length-2][2];
    	String[][] tmpTtotal = new String[fingerprint.length-2][2];
    	String[][] tmpLtotal = new String[fingerprint.length-2][2];
    	
    	int tmp = -1;
    	int flag = 0;
    	
    	for (int i = 1; i < simMat.length; i++){
    		if (tmp < 0 && simMat[i][0].equals(searchedGene)){
    			tmp = i;
    		}
    	}
    	
    	for (int i = 1; i < simMat.length; i++){
    		if (i == tmp){
    			flag = -1;
    		}
    		else {
	    		tmpPD[i-1+flag][0] = simMat[0][i];
	    		tmpPD[i-1+flag][1] = simMat[tmp][i];
	    		tmpT[i-1+flag][0] = transcriptsDif[0][i];
	    		tmpT[i-1+flag][1] = transcriptsDif[tmp][i];
	    		tmpL[i-1+flag][0] = lengthDif[0][i];
	    		tmpL[i-1+flag][1] = lengthDif[tmp][i];
    		}
    	}
    	
    	tmpPD = orderByHighestValues(tmpPD);
    	tmpT = orderByLowestValues(tmpT);
    	tmpL = orderByLowestValues(tmpL);
    	
    	for (int i = 0; i < fingerprint.length-2; i++){
    		for (int j = 0; j < fingerprint.length-1; j++){
    			if (tmpPD[i][0].equals(matrix[j][0])){
    				int count = 0;
    				for (int k = 1; k < matrix[j].length; k++){
    					if (matrix[j][k] != null){
    						count += 1;
    					}
    				}
    				tmpPDtotal[i][0] = matrix[j][0];
    				tmpPDtotal[i][1] = Integer.toString(count);
    			}
    			if (tmpT[i][0].equals(simMat2[j][0])){
    				tmpTtotal[i][0] = simMat2[j][0];
    				tmpTtotal[i][1] = simMat2[j][1];
    			}
    			if (tmpL[i][0].equals(matrix3[j][0])){
    				tmpLtotal[i][0] = matrix3[j][0];
    				tmpLtotal[i][1] = matrix3[j][1];
    			}
    		}
    	}
    	
    	tmpPDtotal = orderByTotalLowestDifference(tmpPD, tmpPDtotal);
    	tmpTtotal = orderByTotalHighestDifference(tmpT, tmpTtotal);
    	tmpLtotal = orderByTotalHighestDifference(tmpL, tmpLtotal);
    	
    	int count = 0;
    	for (int i = 1; i < matrix.length; i++){
    		if (matrix[tmp-1][i] != null){
    			count += 1;
    		}
    	}
    	
    	String[][] normMat = normalize(simMat);
    	String[][] normTranscriptsDif = normalizeAndReverse(simMat2,transcriptsDif);
    	String[][] normLengthDif = normalizeAndReverse(matrix3,lengthDif);
    	String[][] kNearestNeighbours = findKNearestNeighbours(normMat, normTranscriptsDif, normLengthDif, searchedGene, kNearest);
    	
    	fingerprint[0][0] = "# Common Protein Domains     | ";
        fingerprint[0][4] = "# Transcripts Difference  | ";
        fingerprint[0][8] = "Length Difference              | ";
        fingerprint[0][12] = "All Aspects Combined";
        for (int i = 1; i < fingerprint.length; i++){
        	if (i==1||i==2||i==3||i==5||i==6||i==7||i==9||i==10||i==11||i==13||i==14){
        		fingerprint[0][i] = "";
        	}
        	if (i==1||i==5||i==9||i==13){
        		fingerprint[1][i] = "with";
        	}
        }
        
        fingerprint[1][0] = searchedGene;
        fingerprint[1][2] = Integer.toString(count);
        fingerprint[1][3] = "(total " + count + ") | ";
        fingerprint[1][4] = searchedGene;
        fingerprint[1][6] = "0";
        fingerprint[1][7] = "(total " + simMat2[tmp-1][1] + ") | ";
        fingerprint[1][8] = searchedGene;
        fingerprint[1][10] = "0";
        fingerprint[1][11] = "(total " + matrix3[tmp-1][1] + ") | ";
        fingerprint[1][12] = searchedGene;
        fingerprint[1][14] = "3.0";
        for (int i = 0; i < kNearest; i++){
        	fingerprint[i+2][0] = tmpPDtotal[i][0];
        	fingerprint[i+2][1] = "with";
        	fingerprint[i+2][2] = tmpPD[i][1];
        	fingerprint[i+2][3] = "(total " + tmpPDtotal[i][1] + ") | ";
        	fingerprint[i+2][4] = tmpTtotal[i][0];
        	fingerprint[i+2][5] = "with";
        	fingerprint[i+2][6] = tmpT[i][1];
        	fingerprint[i+2][7] = "(total " + tmpTtotal[i][1] + ") | ";
        	fingerprint[i+2][8] = tmpLtotal[i][0];
        	fingerprint[i+2][9] = "with";
        	fingerprint[i+2][10] = tmpL[i][1];
        	fingerprint[i+2][11] = "(total " + tmpLtotal[i][1] + ") | ";
        	fingerprint[i+2][12] = kNearestNeighbours[i][0];
        	fingerprint[i+2][13] = "with";
        	fingerprint[i+2][14] = kNearestNeighbours[i][1];
        }
        
        String[][] kNearestNghbs = new String[kNearest+2][fingerprint[0].length];
    	for (int i = 0; i < kNearest+2; i++){
    		for (int j = 0; j < fingerprint[0].length; j++){
	    		if (i < fingerprint.length){
	    			kNearestNghbs[i][j] = fingerprint[i][j];
	    		}
    		}
    	}
        
    	return kNearestNghbs;
    }

    public static String[][] compareFingerprints(String[][] fingerprint1, String[][] fingerprint2){
    	String[][] overlapping = new String[fingerprint1.length+fingerprint2.length-2][fingerprint1[0].length];
    	String[][] rating1 = new String[fingerprint1.length][fingerprint1[0].length];
    	String[][] rating2 = new String[fingerprint2.length][fingerprint1[0].length];
    	int[] count1 = new int[fingerprint1[0].length];
    	int[] count2 = new int[fingerprint2[0].length];
    	
    	// giving a rating
    	for (int i = 0; i < fingerprint1.length; i++){
    		for (int j = 0; j < fingerprint1[0].length; j++){
    			if (i < 2||j==0||j==1||j==4||j ==5||j==8||j==9||j==12||j==13){
    				rating1[i][j] = fingerprint1[i][j];
    				rating2[i][j] = fingerprint2[i][j];
    			}
    			else if (j==3||j==7||j==11){
    				rating1[i][j] = fingerprint1[i][j-1];
    				rating2[i][j] = fingerprint2[i][j-1];
    			}
    			else {
    				if (i > 2 && fingerprint1[i][j].equals(fingerprint1[i-1][j])){
    					rating1[i][j] = rating1[i-1][j];
    				}
    				else {
    					count1[j] += 1;
    					rating1[i][j] = Integer.toString(count1[j]);
    				}
    				if(i > 2 && fingerprint2[i][j].equals(fingerprint2[i-1][j])){
    					rating2[i][j] = rating2[i-1][j];
    				}
    				else {
    					count2[j] += 1;
    					rating2[i][j] = Integer.toString(count2[j]);
    				}
    			}
    		}
    	}
    	System.out.print("\nrating1: \n");
        for (int i = 0; i < rating1.length; i++) {
            for (int j = 0; j < rating1[0].length; j++) {
            	System.out.print(rating1[i][j] + " ");
            }
            System.out.print("\n");
        }
    	System.out.print("\nrating2: \n");
        for (int i = 0; i < rating2.length; i++) {
            for (int j = 0; j < rating2[0].length; j++) {
            	System.out.print(rating2[i][j] + " ");
            }
            System.out.print("\n");
        }
    	
        // check overlapping and add ratings and order // Not finished!!!!!!
    	for (int i = 0; i < fingerprint1[0].length; i++){
    		overlapping[0][i] = fingerprint1[0][i];
    		for (int j = 0; j < fingerprint1.length; j++){
    			overlapping[j][i] = rating1[j][i];
    		}
    	}
    	for (int i = 0; i < 1; i+=4){
    		int[] count = new int[fingerprint1[0].length];
			
    		for (int j = 2; j < fingerprint2.length; j++){
    			int saved = 0;
    	   		for (int k = 2; k < fingerprint1.length; k++){
	    			if (saved==0 && rating2[j][i].equals(overlapping[k][i])){//||i==4||i==8||i==12
	    				overlapping[k][2] = Integer.toString(Integer.parseInt(overlapping[k][2]) 
	    									+ Integer.parseInt(rating2[j][2]));
	    				saved = 1;
	    			}
	    			else if (saved == 0 && k == fingerprint1.length-1){
	    				for (int l = i; l < i+4; l++){
	    					overlapping[fingerprint1.length+count[i]][l] = rating2[j][l];
	    				}
	    				count[i] += 1;
	    				saved = 1;
	    			}
    	   		}
    		}
    	}
    
    	System.out.print("\noverlapping: \n");
        for (int p = 0; p < overlapping.length; p++) {
            for (int j = 0; j < overlapping[0].length; j++) {
            	System.out.print(overlapping[p][j] + " ");
            }
            System.out.print("\n");
        }
    	
    	return overlapping;
    }
    
}