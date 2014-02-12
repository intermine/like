import java.awt.Point;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.intermine.metadata.Model;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;

public class Benchmarking2 {

	private static final String ROOT = "http://beta.flymine.org/beta/service";

    /**
     * Perform the query and print the rows of results.
     * @param args command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	
    	long t1 = System.currentTimeMillis();
        
        // Build the query
        ServiceFactory factory1 = new ServiceFactory(ROOT);
        Model model1 = factory1.getModel();
        PathQuery query1 = new PathQuery(model1);
        
        // Select the output columns:
        query1.addViews("Gene.primaryIdentifier",
                "Gene.proteins.proteinDomains.shortName",
                "Gene.proteins.proteinDomains.primaryIdentifier");

        // Add orderby
        query1.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);

        // Filter the results with the following constraints:
        query1.addConstraint(Constraints.eq("Gene.organism.name", "Drosophila melanogaster"), "A");
        query1.addConstraint(Constraints.eq("Gene.symbol", "z*"), "B");
        // Specify how these constraints should be combined.
        query1.setConstraintLogic("A and B");
        
        QueryService service = factory1.getQueryService();
        PrintStream out = System.out;
        
        long t2 = System.currentTimeMillis();
        
        // Generate the matrices out of the query
        Iterator<List<Object>> rows = service.getRowListIterator(query1);
        String tmp;
        int countRow = 0;
        int countColumnPD = 0;
//        out.print(service.getCount(query) + "\n");
        	
        Map<Point, String> matrixPD = new HashMap<Point, String>();

        while (rows.hasNext()) {
            List<Object> row = rows.next();
        	for (int i = 0; i < 3; i++){
        		tmp = row.get(i).toString();
        		if (i==0){
	   				if (!matrixPD.containsValue(tmp)){
	       				matrixPD.put(new Point(countRow, 0), tmp);
	       				countRow += 1;
	   				}
        		}
        		
        		// Protein domains
        		if (i==2 && tmp != "null"){
        			int saved = 0;
        			Point p = new Point(countRow-1, countColumnPD+1);
        			for (Map.Entry<Point, String> entry : matrixPD.entrySet()){
        				if (saved == 0 && tmp.equals(entry.getValue())){
        					p.y = entry.getKey().y;
        					saved += 1;
        				}
        			}
        			matrixPD.put(p, tmp);
        			if (p.y == countColumnPD+1){
        				countColumnPD += 1;
        			}
        		}
        	}
        }
        
        // Output of the data storehouse results
        out.print("\nData about Protein Domains: \n");
        for ( int i = 0; i < countRow; i++ ) {
            for ( int j = 0; j < countColumnPD+1; j++ ) { 
               String val = matrixPD.get(new Point(i, j));
               out.print(val + " ");
            }
            out.print("\n");
        }
        out.print("matrixPD.size: " + matrixPD.size() + "\ncountRow: " + countRow + "\ncountColumnPD: " + (countColumnPD+1));
        long t3 = System.currentTimeMillis();
        
///////////////////////////////////////////////////////////////////////////////////////////////////////
        
        // Build the query
        ServiceFactory factory2 = new ServiceFactory(ROOT);
        Model model2 = factory2.getModel();
        PathQuery query2 = new PathQuery(model2);
        
        // Select the output columns:
        query2.addViews("Gene.primaryIdentifier",
        		"Gene.pathways.name",
                "Gene.pathways.identifier");
        
        // Add orderby
        query2.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);
        
        // Filter the results with the following constraints:
        query2.addConstraint(Constraints.eq("Gene.organism.name", "Drosophila melanogaster"), "A");
        query2.addConstraint(Constraints.eq("Gene.symbol", "z*"), "B");
        // Specify how these constraints should be combined.
        query2.setConstraintLogic("A and B");
        
        QueryService service2 = factory2.getQueryService();
            
        long t4 = System.currentTimeMillis();
        
        // Generate the matrices out of the query
        Iterator<List<Object>> rows2 = service2.getRowListIterator(query2);
        int countRow2 = 0;
        int countColumnP = 0;
//            out.print(service.getCount(query) + "\n");
        
        Map<Point, String> matrixP = new HashMap<Point, String>();
        	
        while (rows2.hasNext()) {
        	List<Object> row = rows2.next();
        	for (int i = 0; i < 3; i++){
        		tmp = row.get(i).toString();
        		if (i==0){
        			if (!matrixP.containsValue(tmp)){
        				matrixP.put(new Point(countRow2, 0), tmp);
        				countRow2 += 1;
    	   			}
            	}
            		
        		// Pathways
        		if (i==2 && tmp != "null"){
        			int saved = 0;
        			Point p = new Point(countRow2-1, countColumnP+1);
        			for (Map.Entry<Point, String> entry : matrixP.entrySet()){
        				if (saved == 0 && tmp.equals(entry.getValue())){
        					p.y = entry.getKey().y;
        					saved += 1;
            			}
            		}
        			matrixP.put(p, tmp);
        			if (p.y == countColumnP+1){
        				countColumnP += 1;
            		}
            	}
            }
        }
            
        out.print("\nData about Pathways: \n");
        for ( int i = 0; i < countRow2; i++ ) {
        	for ( int j = 0; j < countColumnP+1; j++ ) { 
        		String val = matrixP.get(new Point(i, j));
        		out.print(val + " ");
        	}
        	out.print("\n");
        }
        
        long t5 = System.currentTimeMillis();
        
  	
/////*** Generate the similarity matrices ***/////
    Map<Point,String> simMatPD = new HashMap<Point,String>();
    Map<Point,String> normWeightedMatPD = new HashMap<Point,String>();
    Map<Point,String> simMatP = new HashMap<Point,String>();
    Map<Point,String> normWeightedMatP = new HashMap<Point,String>();
    //        for (int i = 2; i < countViews; i++){
//    if (saveViews[i] == 2){ // PD
    simMatPD = calculateMatrix(matrixPD);
  	out.print("\nNumber of Protein Domains in common: \n");
  	for (int j = 0; j < countRow+1; j++) {
  		for (int k = 0; k < countRow+1; k++) {
  			String val = simMatPD.get(new Point(j, k));
               out.print(val + " ");
  		}
  		out.print("\n");
  	}	      
    long t6 = System.currentTimeMillis();
    out.print("\n" + service.getCount(query1) + " rows1," +  service.getCount(query2) + " rows2, protein domains and pathways, 2 query, 0 outer joins: \n"
//  		+ views[0] + ", \n" + views[2] + ", \n" + views[4] + ", \n" + views[6] + ", \n" + views[8] + ":\n"
  		+ (t2 - t1) + "ms for the query settings for the protein domains\n"
  		+ (t3 - t2) + "ms to generate protein domains matrix out of the query\n"
  		+ (t4 - t3) + "ms for the query settings for the protein domains\n"
  		+ (t5 - t4) + "ms to generate pathways matrix out of the query\n"
  		+ (t6 - t5) + "ms to calculate the pathways similarity matrix\n"
  		+ (t6 - t1) + "ms all together");
  	out.print("\n");
  	Map<Point,String> normMatPD = normalize(simMatPD, countRow);
    normWeightedMatPD = addWeigth(normMatPD, countRow, 1);
    		//...Float.parseFloat(prop.getProperty("recommendation.engine.query.2.weight")));
    out.print("\nNormalised Number of Protein Domains in common: \n");
    for (int j = 0; j < countRow+1; j++) {
        for (int k = 0; k < countRow+1; k++) {
            out.print(normWeightedMatPD.get(new Point(j,k)) + " ");
        }
        out.print("\n");
    }
  	
//}
//if (saveViews[i] == 4){ // P
	simMatP = calculateMatrix(matrixP);
    out.print("\nNumber of Pathways in common: \n");
    for (int j = 0; j < countRow2+1; j++) {
        for (int k = 0; k < countRow2+1; k++) {
            out.print(simMatP.get(new Point(j,k)) + " ");
        }
        out.print("\n");
    }
	Map<Point,String> normMatP = normalize(simMatP, countRow2);
    normWeightedMatP = addWeigth(normMatP, countRow2, 1);
    		//....Float.parseFloat(prop.getProperty("recommendation.engine.query.3.weight")));
    out.print("\nNormalised Number of Pathways in common: \n");
    for (int j = 0; j < countRow2+1; j++) {
        for (int k = 0; k < countRow2+1; k++) {
            out.print(normWeightedMatP.get(new Point(j,k)) + " ");
        }
        out.print("\n");
    }
//}
//    }
  	
 // Merge the matrices
    Map<Point,String> addedMat = new HashMap<Point,String>();
//  for (int i = 2; i < countViews; i++){
//      if (saveViews[i] == 2){
      	addedMat = addMatrizes(addedMat,normWeightedMatPD,0,countRow);
//      }
      	out.print("\nMerged Protein Domains and Pathways: \n");
      	for (int j = 0; j < countRow+1; j++) {
      		for (int k = 0; k < countRow+1; k++) {
                out.print(addedMat.get(new Point(j,k)) + " ");
            }
            out.print("\n");
        }
//      if (saveViews[i] == 4){
      	addedMat = addMatrizes(addedMat,normWeightedMatP,countRow,countRow2);
//      }
//    }
      	out.print("\nMerged Protein Domains and Pathways: \n");
      	for (int j = 0; j < countRow+1; j++) {
      		for (int k = 0; k < countRow+1; k++) {
                out.print(addedMat.get(new Point(j,k)) + " ");
            }
            out.print("\n");
        }
    }

    
    public static Map<Point,String> builtMatrix(Map<Point, String> matrix){
//    	Map<Point,String> matrix = new HashMap<Point,String>();
    	
    	return matrix;
    }
    
    public static Map<Point,String> calculateMatrix(Map<Point,String> matrix) {
    	// Compare matrix to all other Genes
    	Map<Point,String> simMat = new HashMap<Point,String>();
		for (Map.Entry<Point, String> entry : matrix.entrySet()){
			if (entry.getKey().y == 0){
		    	Map<Integer,Integer> count = new HashMap<Integer,Integer>();
				simMat.put(new Point(0,entry.getKey().x+1),matrix.get(new Point(entry.getKey().x,0)));
	    		simMat.put(new Point(entry.getKey().x+1,0),matrix.get(new Point(entry.getKey().x,0)));
	    		for (Map.Entry<Point, String> entry2 : matrix.entrySet()){
	    			if (entry2.getKey().x == entry.getKey().x){
	    				for (Map.Entry<Point, String> entry3 : matrix.entrySet()){
	    					if (entry.getKey() != entry3.getKey()
	    							&& entry2.getValue().equals(entry3.getValue())){
	    						if (count.get(entry3.getKey().x) != null){
	    							count.put(entry3.getKey().x, count.get(entry3.getKey().x) + 1);
	    						}    						
	    						else {
	    							count.put(entry3.getKey().x, 1);
	    						}
	    					}
	    				}
	    			}
				}
	    		for (Map.Entry<Integer, Integer> entry5 : count.entrySet()){
	    			simMat.put(new Point(entry.getKey().x+1, entry5.getKey()+1), Integer.toString(entry5.getValue()));
	    		}
			}
		}
    	return simMat;
    }	

    public static Map<Point,String> calculateMatrixT(Map<Point,String> matrixT, int rows){
    	Map<Point,String> simMatT = new HashMap<Point,String>();
    	
    	for (int i = 0; i < rows+1; i++){
    		simMatT.put(new Point(i,0),matrixT.get(new Point(i,0)));
    		int count = 0;
    		for (int j = 1; j < rows+1; j++){
    			if (matrixT.get(new Point(i,j)) != null){
    				count += 1;
    			}
    		}
    		simMatT.put(new Point(i,1),Integer.toString(count));
    	}
    	
    	return simMatT;
    }
    
    public static String[][] calculateMatrixDif(String[][] matrix2Col){
    	String[][] simMat = new String[matrix2Col.length+1][matrix2Col.length+1];
    	
    	for (int i = 0; i < matrix2Col.length; i++){
    		simMat[0][i+1] = matrix2Col[i][0];
    		simMat[i+1][0] = matrix2Col[i][0];
    	}
    	
    	for (int i = 0; i < matrix2Col.length; i++){
    		for (int j = 0; j < matrix2Col.length; j++){
    			if (i != j){
    				simMat[i+1][j+1] = Integer.toString(Math.abs(Integer.parseInt(matrix2Col[i][1])-Integer.parseInt(matrix2Col[j][1])));
    			}
    		}
    	}
    	return simMat;
    
    }   
    
    public static Map<Point,String> normalize(Map<Point,String> matrix, int rows){
    	Map<Point,String> normMatrix = new HashMap<Point,String>();
    	int highestVal = 0;
    	int tmpI;
    	float tmpJ;
    	String normDif;
    	
    	for (int i = 0; i < rows+1; i++){
    		normMatrix.put(new Point(i,0),matrix.get(new Point(i,0)));
    		normMatrix.put(new Point(0,i),matrix.get(new Point(i,0)));
    		for (int j = 1; j < rows+1; j++){
    			if (i > 0 && i != j 
    					&& matrix.get(new Point(i,j)) != null
    					&& Integer.parseInt(matrix.get(new Point(i,j))) > highestVal){
    				highestVal = Integer.parseInt(matrix.get(new Point(i,j)));
    			}
    		}
    	}
    	
    	for (int i = 1; i < rows+1; i++){
    		for (int j = 1; j < rows+1; j++){
    			if (i != j && matrix.get(new Point(i,j)) != null){
    				tmpI = Integer.parseInt(matrix.get(new Point(i,j)));
    				tmpJ = (float)tmpI/(float)highestVal;
    				normDif = Float.toString(tmpJ);
    				normMatrix.put(new Point(i,j),normDif);
    			}
    		}
    	}
    	return normMatrix;
    }
    
    public static Map<Point,String> addWeigth(Map<Point,String> normMat, int rows, float weigth){
    	Map<Point,String> normWeightedMat = new HashMap<Point,String>();
    	
    	for (int i = 0; i < rows+1; i++){
    		normWeightedMat.put(new Point(0,i),normMat.get(new Point(i,0)));
    		normWeightedMat.put(new Point(i,0),normMat.get(new Point(i,0)));
    	}
    	
    	for (int i = 1; i < rows+1; i++){
    		for (int j = 1; j < rows+1; j++){
    			if (i != j && normMat.get(new Point(i,j)) != null){
    				normWeightedMat.put(new Point(i,j),Float.toString(Float.parseFloat(normMat.get(new Point(i,j)))*weigth));
    			}
    		}
    	}
    	
    	return normWeightedMat;
    }
        
    public static String[][] orderByHighestValues(String[][] mat2Columns){
    	float[] allNeighbours = new float[mat2Columns.length];
    	String[] tmpNeighbour = new String[2];
    	
    	for (int i = 0; i < mat2Columns.length; i++){
    		allNeighbours[i] = Float.parseFloat(mat2Columns[i][1]);
    	}
    	
    	for (int i = 0; i < mat2Columns.length; i++){
    		for (int j = i; j < mat2Columns.length; j++){
    			if (allNeighbours[i] < allNeighbours[j]){
    				tmpNeighbour[0] = mat2Columns[j][0];
    				tmpNeighbour[1] = mat2Columns[j][1];
    				allNeighbours[j] = allNeighbours[i];
    				allNeighbours[i] = Float.parseFloat(mat2Columns[j][1]);
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

    public static String[][] orderByHighestValues3(String[][] mat3Columns){
    	float[] allNeighbours = new float[mat3Columns.length];
    	String[] tmpNeighbour = new String[3];
    	
    	for (int i = 0; i < mat3Columns.length; i++){
    		allNeighbours[i] = Float.parseFloat(mat3Columns[i][2]);
    	}
    	
    	for (int i = 0; i < mat3Columns.length; i++){
    		for (int j = i; j < mat3Columns.length; j++){
    			if (allNeighbours[i] < allNeighbours[j]){
    				tmpNeighbour[0] = mat3Columns[j][0];
    				tmpNeighbour[1] = mat3Columns[j][1];
    				tmpNeighbour[2] = mat3Columns[j][2];
    				allNeighbours[j] = allNeighbours[i];
    				allNeighbours[i] = Float.parseFloat(mat3Columns[j][2]);
    				mat3Columns[j][0] = mat3Columns[i][0];
    				mat3Columns[i][0] = tmpNeighbour[0];
    				mat3Columns[j][1] = mat3Columns[i][1];
    				mat3Columns[i][1] = tmpNeighbour[1];
    				mat3Columns[j][2] = mat3Columns[i][2];
    				mat3Columns[i][2] = tmpNeighbour[2];
    			}
    		}
    	}
    	return mat3Columns;
    }
    
    public static String[][] findTotal(String[][] fingerprintTmp, String[][] matrixPD){
    	String[][] countTotal = new String[fingerprintTmp.length][2];
		for (int j = 0; j < fingerprintTmp.length; j++){
			for (int k = 0; k < matrixPD.length; k ++){
				if (fingerprintTmp[j][0].equals(matrixPD[k][0])){
					int countPD = 0;
					for (int l = 1; l < matrixPD[k].length; l++){
						if (matrixPD[k][l] != null){
							countPD += 1;
						}
					}
					countTotal[j][0] = matrixPD[k][0];
					countTotal[j][1] = Integer.toString(countPD);
				}
			}
		}	
		return countTotal;
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
    
    public static String[][] reverse(String[][] normMatrix){
    	for (int i = 1; i < normMatrix.length; i++){
    		for (int j = 1; j < normMatrix.length; j++){
    			if (i != j){
    				normMatrix[i][j] = Float.toString((float)1 - Float.parseFloat(normMatrix[i][j]));
    			}
    		}
    	}
    	return normMatrix;
    }
    
    public static String[][] findKNearestNeighbours(String[][] addedMat, String searchedGene){
    	String[][] kNearestNeighboursBuild = new String[addedMat[0].length-2][2];
    	int tmp = -1;
    	int flag = 0;
    	
    	for (int i = 1; i < addedMat.length; i++){
    		if (addedMat[i][0].equals(searchedGene)){
    			tmp = i;
    		}
    	}
    	
    	if (tmp < 0){
    		throw new IllegalArgumentException("Gene not in Database!");
    	}
    	
    	for (int i = 1; i < addedMat[0].length; i++){
    		if (i == tmp){
    			flag = -1;
    		}
    		else {
	    		kNearestNeighboursBuild[i-1+flag][0] = addedMat[i][0];
	    		kNearestNeighboursBuild[i-1+flag][1] = addedMat[i][tmp];
    		}
    	}
    	
    	return kNearestNeighboursBuild;
    }
    
    public static String[][] trimMatrix(String[][] matrix, int kNearest){
    	if (kNearest > matrix.length){
    		kNearest = matrix.length;
    	}
    	
    	String[][] trimMat = new String[kNearest][matrix[0].length];
    	for (int i = 0; i < kNearest; i++){
    		for (int j = 0; j < matrix[0].length; j++){
    			trimMat[i][j] = matrix[i][j];
    		}
    	}
    	return trimMat;
    }
    
    public static Map<Point,String> addMatrizes(Map<Point,String> normMat1, Map<Point,String> normMat2, int rows1, int rows2){
    	if (normMat1.get(new Point(0,1)) == null ){
    		return normMat2;
    	}
    	else {
    		Map<Point,String> addedMat = normMat1;
    		int count = 0;
	    	for (int i = 0; i < rows2+1; i++){
	    		if (!addedMat.containsValue(normMat2.get(new Point(i,0)))){
	    			addedMat.put(new Point(0,rows1+count+1), normMat2.get(new Point(i,0)));
	    			addedMat.put(new Point(rows1+count+1,0), normMat2.get(new Point(i,0)));
	    			count += 1;
	    		}
	    	}
	    	for (int j = 0; j < rows1+1+count; j++){
	    		for (Map.Entry<Point, String> entry : normMat2.entrySet()){
	    			if (entry.getKey().y == 0 
	    					&& entry.getValue() != null
	    					&& entry.getValue().equals(addedMat.get(new Point(j,0)))){
	    				for (Map.Entry<Point, String> entry2 : normMat2.entrySet()){
	    					if (entry2.getKey().x == entry.getKey().x){
	    						for (int k = 0; k < rows1+1+count; k ++){
	    							if (entry2.getKey().y != 0 
	    									&& normMat2.get(new Point(0,entry2.getKey().y)).equals(addedMat.get(new Point(0,k)))){
	    								if (addedMat.get(new Point(j,k)) != null){
	    									addedMat.put(new Point(j,k), Float.toString(Float.parseFloat(addedMat.get(new Point(j,k)))
	    											+Float.parseFloat(entry2.getValue())));
	    								}	
	    								else {
	    									addedMat.put(new Point(j,k), entry2.getValue());
	    								}
	    							}
	    						}
	    					}
	    				}
	    			}
	    		}
	    	}
	    	return addedMat;
    	}
    }
    
    public static String[][] showMostSimilarGenes(String[][] addedMat){
    	String[][] mostSimilarGenesBuild = new String[addedMat.length*addedMat.length][3];
    	int count = 0;
    	
    	for (int i = 1; i < addedMat.length; i++){
    		for (int j = i+1; j < addedMat.length; j++){
    			if (Float.parseFloat(addedMat[i][j]) != 0.0){
    				mostSimilarGenesBuild[count][0] = addedMat[i][0];
    				mostSimilarGenesBuild[count][1] = addedMat[0][j];
    				mostSimilarGenesBuild[count][2] = addedMat[i][j];
    				count += 1;
    			}
    		}
    	}
    	
    	String[][] mostSimilarGenes = trimMatrix(mostSimilarGenesBuild,count);
    	
    	return mostSimilarGenes;
    }
   
    public static String[][] showMostSimilarSet(String[][] addedMat, String[] testSet){
        String[][] mostSimilarSetBuildAll = new String[addedMat.length*addedMat.length][3];
        int count = 0;
    	
    	for (int i = 1; i < addedMat.length; i++){
    		for (int j = 1; j < addedMat.length; j++){
    			for (int k = 0; k < testSet.length; k++)
    			if (i!=j && addedMat[i][0].equals(testSet[k])
    					&& Float.parseFloat(addedMat[i][j]) != 0.0){
    				mostSimilarSetBuildAll[count][0] = addedMat[i][0];
    				mostSimilarSetBuildAll[count][1] = addedMat[0][j];
    				mostSimilarSetBuildAll[count][2] = addedMat[i][j];
    				count += 1;
    			}
    		}
    	}
    	
        String[][] mostSimilarSetRedundant = trimMatrix(mostSimilarSetBuildAll, count);
        
        String[][] mostSimilarSetBuild = new String[mostSimilarSetRedundant.length][2];
        int countRed = 0;
        mostSimilarSetBuild[0][0] = mostSimilarSetRedundant[0][1];
        mostSimilarSetBuild[0][1] = mostSimilarSetRedundant[0][2];
        for (int i = 1; i < count; i++){
        	int saved = 0;
        	for (int j = 0; j < i; j++){
        		for (int k = 0 ; k < testSet.length; k++){
	        		if (saved == 0 && mostSimilarSetRedundant[i][1].equals(testSet[k])){
	        			saved = 1;
	        		}
	        		else if (saved == 0 && mostSimilarSetRedundant[i][1].equals(mostSimilarSetBuild[j][0])){
	        			mostSimilarSetBuild[j][1] = Float.toString(Float.parseFloat(mostSimilarSetBuild[j][1]) 
	        					+ Float.parseFloat(mostSimilarSetRedundant[i][2]));
	        			saved = 1;
	        		}
	        		else if (saved == 0 && j == i-1){
	        			countRed += 1;
	        			mostSimilarSetBuild[countRed][0] = mostSimilarSetRedundant[i][1];
	        			mostSimilarSetBuild[countRed][1] = mostSimilarSetRedundant[i][2];
	        			saved = 1;
	        		}
        		}
        	}
        }
        
        String[][] mostSimilarSet = trimMatrix(mostSimilarSetBuild, countRed+1);
        
        return mostSimilarSet;
    }
    
}
