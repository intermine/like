import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.vecmath.Point3d;

import org.intermine.metadata.Model;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;

public class LikeRefactoring {

	private static final String ROOT = "http://beta.flymine.org/beta/service";

    /**
     * Perform the query and print the rows of results.
     * @param args command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        PrintStream out = System.out;
    	long t1 = System.currentTimeMillis();
        
        // read properties
    	Map<Point,String> views = getProperties();
    	long t2 = System.currentTimeMillis();
    	out.print((t2-t1) + "ms to read the property file" + "\n");
    	
    	Map<Point3d,String> matrix = new HashMap<Point3d,String>();
        Map<Point3d,String> simMat = new HashMap<Point3d,String>();
        Map<Point3d,String> normMat = new HashMap<Point3d,String>();
//        Map<Point3d,String> weightedMat = new HashMap<Point3d,String>();
        long t8 = 0;
    	for (int i = 0; i < views.size()/3; i++){
    		long t3 = System.currentTimeMillis();
    		
    		// Build query i
    		Iterator<List<Object>> rows = buildQuery(views,i);
    		long t4 = System.currentTimeMillis();
        	out.print((t4-t3) + "ms to build query " + i + "\n");
    	
	        // Generate matrix i out of the query
	        matrix = runQuery(matrix,rows,i);
	        long t5 = System.currentTimeMillis();
	    	out.print((t5-t4) + "ms to run query " + i + "\n");
	        
	        // Calculate matrix i
	        simMat = calculateMatrix(simMat,matrix,i);
	        long t6 = System.currentTimeMillis();
	    	out.print((t6-t5) + "ms to calculate the matrix " + i + "\n");
	        
	        // Normalise similarity matrix i
	        normMat = normalize(normMat,simMat,i);
	      	long t7 = System.currentTimeMillis();
	    	out.print((t7-t6) + "ms to normalise matrix " + i + "\n");
	       
	        // Add weight i to similarity matrix i
//	      	weightedMat = addWeigth(normMat,Float.parseFloat(views.get(new Point(i,2))),i);
		    t8 = System.currentTimeMillis();
//	    	out.print((t8-t7) + "ms to add weight to matrix " + i + "\n");
    	}
    	out.print("-> " + (t8-t1) + "ms to precalculate" + "\n");
    	PrintStream out2 = new PrintStream(new FileOutputStream("output.txt"));
    	System.setOut(out2);
    	
    	// Add similarity matrices
        Map<Point,String> addedMat = addMatrices(normMat);
		long t9 = System.currentTimeMillis();
    	out.print((t9-t8) + "ms to add the matrices" + "\n");

    	// Search for similar genes to the testSet
        String[] testSet = {"FBgn0022720","FBgn0004606","FBgn0033096","1427416","1039707"};
        Map<String,Float> mostSimilarSet = findMostSimilarSet(addedMat,testSet);
        long t10 = System.currentTimeMillis();
        out.print((t10-t9) + "ms to find the most similar genes to the testSet:\n");
        
        long t11 = System.currentTimeMillis();
        out.print("-> " + (t11-t8) + "ms for the run time calculations" + "\n");
        out.print((t11-t1) + "ms all together" + "\n");
        
        
    	// Output
//        for (int i = 0; i < views.size()/3; i++){
//        	out.print("\nData of matrix " + i + ": \n");
//	        for ( int k = 0; k < 33; k++ ) {
//	            for ( int j = 0; j < 33; j++ ) { 
//	               String val = weightedMat.get(new Point3d(k, j, i));
//	               out.print(val + " ");
//	            }
//	            out.print("\n");
//	        }
//        }
//        
       	out.print("\nAdded matrices: \n");
        for ( int k = 0; k < 33; k++ ) {
            for ( int j = 0; j < 33; j++ ) { 
               String val = addedMat.get(new Point(k,j));
               out.print(val + " ");
            }
            out.print("\n");
        }
//        
        
//        File file = new File("oldOutput");
//	    FileOutputStream f = new FileOutputStream(file);
//	    ObjectOutputStream s = new ObjectOutputStream(f);
//	    s.writeObject(mostSimilarSet);
//	    s.close();
//	    
//	    PrintStream out3 = new PrintStream(new FileOutputStream("oldOutput.txt"));
//	    System.setOut(out3);
//	    out3.println(mostSimilarSet);
	    
        out.print("\nMost similar set of genes: \n");
        for (Map.Entry<String,Float> entry : mostSimilarSet.entrySet()){
        	out.print(entry.getKey() + " " + entry.getValue() + "\n");
        }
    }
    
    public static Map<Point,String> getProperties() throws IOException{
    	Map<Point,String> views = new HashMap<Point,String>();
    	
    	Properties prop = new Properties();
        String fileName = "/home/selma/workspace/LIKE/annotationConfig2.txt";
        InputStream is = new FileInputStream(fileName);
        prop.load(is);
        
        int countViews = 0;
        for (int i = 0; i < prop.size(); i++){
        	if (prop.getProperty("query." + (i+1) + ".required") != null
        			&& prop.getProperty("query." + (i+1) + ".required").equals("yes")){
	        	views.put(new Point(countViews,0),prop.getProperty("query." + (i+1) + ".identifier"));
	        	views.put(new Point(countViews,1),prop.getProperty("query." + (i+1) + ".name"));
	        	views.put(new Point(countViews,2),prop.getProperty("query." + (i+1) + ".weight"));
	        	countViews += 1;
        	}
        }
    	return views;
    }

    public static Iterator<List<Object>> buildQuery(Map<Point,String> views, int i){
    	// Build the query
        ServiceFactory factory = new ServiceFactory(ROOT);
        Model model = factory.getModel();
        PathQuery query = new PathQuery(model);
        
        // add views
    	query.addViews("Gene.primaryIdentifier",views.get(new Point(i,0)));
		
		// Add order by
        query.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);
        // Filter the results with the following constraints:
        query.addConstraint(Constraints.eq("Gene.organism.name", "Drosophila melanogaster"), "A");
        query.addConstraint(Constraints.eq("Gene.symbol", "z*"), "B");
        // Specify how these constraints should be combined.
        query.setConstraintLogic("A and B");
        
        QueryService service = factory.getQueryService();
        
    	Iterator<List<Object>> rows = service.getRowListIterator(query);
		
		return rows;
    }
    
    public static Map<Point3d,String> runQuery(Map<Point3d,String> matrix, Iterator<List<Object>> rows, int pos){
        String tmp;
        double tmpRow = 0;
        double highestIndex = -1;
        int countColumnPD = 0;
//        out.print(service.getCount(query) + "\n");
        
    	while (rows.hasNext()) {
            List<Object> row = rows.next();
        	for (int i = 0; i < 2; i++){
        		tmp = row.get(i).toString();
        		if (i==0){
        			int saved = 0;
        			for (Map.Entry<Point3d,String> entry : matrix.entrySet()){
        				if (entry.getKey().y == 0){
        					if (entry.getValue().equals(tmp)){
        						tmpRow = entry.getKey().x;
        						saved = 1;
        						break;
        					}
        				}
        				if (entry.getKey().x > highestIndex){
        					highestIndex = entry.getKey().x;
        				}
        			}
        			if (saved == 0) {
        					tmpRow = highestIndex+1;
        			}
        			matrix.put(new Point3d(tmpRow, 0, pos), tmp);
        		}
        		
        		if (i==1 && tmp != "null"){
        			int saved = 0;
        			Point3d p = new Point3d(tmpRow, countColumnPD+1,pos);
        			for (Map.Entry<Point3d, String> entry : matrix.entrySet()){
        				if (saved == 0 && tmp.equals(entry.getValue())){
        					p.y = entry.getKey().y;
        					saved += 1;
        				}
        			}
        			matrix.put(p, tmp);
        			if (p.y == countColumnPD+1){
        				countColumnPD += 1;
        			}
        		}
        	}
        }
    	return matrix;
    }
    
    public static Map<Point3d,String> calculateMatrix(Map<Point3d,String> simMat, Map<Point3d,String> matrix, int pos) {
		for (Map.Entry<Point3d,String> entry : matrix.entrySet()){
			if (entry.getKey().z == pos && entry.getKey().y == 0){
		    	Map<Double,Integer> count = new HashMap<Double,Integer>();
				simMat.put(new Point3d(0,entry.getKey().x+1,pos),matrix.get(new Point3d(entry.getKey().x,0,pos)));
	    		simMat.put(new Point3d(entry.getKey().x+1,0,pos),matrix.get(new Point3d(entry.getKey().x,0,pos)));
	    		for (Map.Entry<Point3d,String> entry2 : matrix.entrySet()){
	    			if (entry2.getKey().z == pos && entry2.getKey().x == entry.getKey().x){
	    				for (Map.Entry<Point3d,String> entry3 : matrix.entrySet()){
	    					if (entry3.getKey().z == pos && entry.getKey() != entry3.getKey()
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
	    		for (Map.Entry<Double,Integer> entry5 : count.entrySet()){
	    			simMat.put(new Point3d(entry.getKey().x+1, entry5.getKey()+1,pos), Integer.toString(entry5.getValue()));
	    		}
			}
		}
    	return simMat;
    }	
    
    public static Map<Point3d,String> normalize(Map<Point3d,String> normMat, Map<Point3d,String> matrix, int pos){
        int highestVal = 0;
    	for (Map.Entry<Point3d,String> entry : matrix.entrySet()){
    		if (entry.getKey().z == pos
    				&& entry.getKey().x != 0 && entry.getKey().y != 0
    				&& entry.getKey().x != entry.getKey().y
    				&& Integer.parseInt(entry.getValue()) > highestVal){
    			highestVal = Integer.parseInt(entry.getValue());
    		}
    		else if (entry.getKey().z == pos && entry.getKey().y == 0){
    			normMat.put(new Point3d(0,entry.getKey().x,pos),matrix.get(new Point3d(entry.getKey().x,0,pos)));
    			normMat.put(new Point3d(entry.getKey().x,0,pos),matrix.get(new Point3d(entry.getKey().x,0,pos)));
    		}
    	}
    	for (Map.Entry<Point3d,String> entry : matrix.entrySet()){
    		if (entry.getKey().z == pos
    				&& entry.getKey().x != 0 && entry.getKey().y != 0
    				&& entry.getKey().x != entry.getKey().y){
    			normMat.put(new Point3d(entry.getKey()), Float.toString(Float.parseFloat(entry.getValue())/(float)highestVal));
    		}
    	}
    	return normMat;
    }
    
    public static Map<Point3d,String> addWeigth(Map<Point3d,String> matrix, float weigth, int pos){
    	for (Map.Entry<Point3d,String> entry : matrix.entrySet()){
    		if (entry.getKey().z == pos && entry.getKey().x != 0 && entry.getKey().y != 0){
    			matrix.put(entry.getKey(), Float.toString(Float.parseFloat(entry.getValue())*weigth));
    		}
    	}
    	return matrix;
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
   
    public static Map<Point,String> addMatrices(Map<Point3d,String> matrix){
    	Map<Point,String> addedMat = new HashMap<Point,String>();
    	for (Map.Entry<Point3d,String> entry : matrix.entrySet()){
    		if (entry.getKey().x == 0 || entry.getKey().y == 0){
    			addedMat.put(new Point((int)entry.getKey().x, (int)entry.getKey().y), entry.getValue());
    		}
    		else if (addedMat.get(new Point((int)entry.getKey().x, (int)entry.getKey().y)) != null){
    			addedMat.put(new Point((int)entry.getKey().x, (int)entry.getKey().y), 
    					Float.toString(Float.parseFloat(addedMat.get(new Point((int)entry.getKey().x, (int)entry.getKey().y))) 
    							+ Float.parseFloat(entry.getValue())));
    		}
    		else {
    			addedMat.put(new Point((int)entry.getKey().x, (int)entry.getKey().y), entry.getValue());
    		}
    	}
	    return addedMat;
    }
    
    public static Map<String,Float> findMostSimilarSet(Map<Point,String> addedMat,String[] testSet){
    	Map<String,Float> mostSimilarSet = new HashMap<String,Float>();
    	
        for (Map.Entry<Point,String> entry : addedMat.entrySet()){
        	if (entry.getKey().y == 0){
	        	for (int i = 0; i < testSet.length; i++){
	        		if (entry.getValue().equals(testSet[i])){
	        			for (Map.Entry<Point,String> entry2 : addedMat.entrySet()){
	        				if (entry2.getKey().y != 0 
	        						&& entry2.getKey().x == entry.getKey().x){
	        					int ignore = 0;
	        					for (int j = 0; j < testSet.length; j++){
	        						if (addedMat.get(new Point(0,entry2.getKey().y)).equals(testSet[j])){
	        							ignore = 1;
	        							break;
	        						}
	        					}
	        					if (ignore == 0){
	        						if (mostSimilarSet.get(addedMat.get(new Point(0,entry2.getKey().y))) != null){
	        							mostSimilarSet.put(addedMat.get(new Point(0,entry2.getKey().y)), 
	        								mostSimilarSet.get(addedMat.get(new Point(0,entry2.getKey().y)))
	        										+ Float.parseFloat(entry2.getValue()));
	        						}
	        						else {
	        							mostSimilarSet.put(addedMat.get(new Point(0,entry2.getKey().y)), Float.parseFloat(entry2.getValue()));
	        						}
	        					}
	        				}
	        			}
	        		}
	        	}
        	}
        }
        return mostSimilarSet;
    }
    
}
