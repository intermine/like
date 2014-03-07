import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LikeRunTime {
    /**
     * Perform the query and print the rows of results.
     * @param args command line arguments
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        PrintStream out = System.out;
    	long t1 = System.currentTimeMillis();
    	
    	// read properties
    	Map<Point,String> views = getProperties();
    	long t2 = System.currentTimeMillis();
    	out.print((t2-t1) + "ms to read the property file" + "\n");
    	 
    	Map<Point,String> addedMat = new HashMap<Point,String>();
    	long t9 = 0;
    	
    	// Add similarity matrices
     	for (int i = 0; i < views.size()/4; i++){
	    	// Read in file i
	    	// takes approx 30sec
     		File file2 = new File("SimilarityMatrix" + i);
		    FileInputStream f2 = new FileInputStream(file2);
		    ObjectInputStream s2 = new ObjectInputStream(f2);
			HashMap<Point,String> normMat = (HashMap<Point,String>)s2.readObject();
		    s2.close();
		    
	    	// Add similarity matrices
	        addedMat = addMatrices(addedMat,normMat);
			t9 = System.currentTimeMillis();
	    	out.print((t9-t1) + "ms to add the matrices" + "\n");
     	}
     	
    	// Search for similar genes to the testSet
        String[] testSet = {"1039707","FBgn0022720","FBgn0004606","FBgn0033096","1427416"};
        String[][] mostSimilarSet = findMostSimilarSet(addedMat,testSet);
        long t10 = System.currentTimeMillis();
        out.print((t10-t9) + "ms to find the most similar genes to the testSet\n");
        
        // takes approx 300sec
        for (int i = 0; i < views.size()/4; i++){
        	long t11 = System.currentTimeMillis();
	        File file1 = new File("CommonItems" + i);
	            // file input is long
		    FileInputStream f1 = new FileInputStream(file1);
		    ObjectInputStream s1 = new ObjectInputStream(f1);
		    Map<Point,ArrayList<String>> commonMat = (Map<Point,ArrayList<String>>)s1.readObject();
		    s1.close();
		    long t12 = System.currentTimeMillis();
		    out.print((t12-t11) + "ms to read in the commonItems file\n");
		    
		    // expensive
		    Map<Integer,Map<String,ArrayList<String>>> commonItems = findCommonItems(commonMat,testSet,mostSimilarSet);
		    long t13 = System.currentTimeMillis();
		    out.print((t13-t12) + "ms to find the common items\n");
		    
		    out.print("\ncommonItems " + i + "\n");
	        for ( int k = 0; k < mostSimilarSet.length; k++ ) { 
	        	Map<String,ArrayList<String>> val = commonItems.get(k);
	        	for (Map.Entry<String,ArrayList<String>> entry : val.entrySet()){
	        		ArrayList<String> val2 = entry.getValue();
	        			out.print(val2 + " ");	
	        	}
	            
	            out.print("\n");
	        }
        }
        
        long t14 = System.currentTimeMillis();
        out.print("-> " + (t14-t1) + "ms for the run time calculations" + "\n");
        
        
    	// Output
//       	out.print("\nAdded matrices: \n");
//        for ( int k = 0; k < 35; k++ ) {
//            for ( int j = 0; j < 35; j++ ) { 
//               String val = addedMat.get(new Point(k,j));
//               out.print(val + " ");
//            }
//            out.print("\n");
//        }
    	out.print("\nMost similar set of genes: \n");
        for ( int k = 0; k < mostSimilarSet.length; k++ ) {
            for ( int j = 0; j < mostSimilarSet[0].length; j++ ) { 
               out.print(mostSimilarSet[k][j] + " ");
            }
            out.print("\n");
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
        		views.put(new Point(countViews,0),prop.getProperty("query." + (i+1) + ".outerjoin"));
	        	views.put(new Point(countViews,1),prop.getProperty("query." + (i+1) + ".identifier"));
	        	views.put(new Point(countViews,2),prop.getProperty("query." + (i+1) + ".name"));
	        	views.put(new Point(countViews,3),prop.getProperty("query." + (i+1) + ".weight"));
	        	countViews += 1;
        	}
        }
    	return views;
    }
    
    public static Map<Point,String> addWeigth(Map<Point,String> matrix, float weigth){
    	for (Map.Entry<Point,String> entry : matrix.entrySet()){
    		if (entry.getKey().x != 0 && entry.getKey().y != 0){
    			matrix.put(entry.getKey(), Float.toString(Float.parseFloat(entry.getValue())*weigth));
    		}
    	}
    	return matrix;
    }
    
    public static Map<Point,String> addMatrices(Map<Point,String> addedMat,Map<Point,String> matrix){
    	if (addedMat.isEmpty()){
    		for (Map.Entry<Point,String> entry : matrix.entrySet()){
    			addedMat.put(new Point((int)entry.getKey().x, (int)entry.getKey().y), entry.getValue());
    		}
    	}
    	else {
	    	for (Map.Entry<Point,String> entry : matrix.entrySet()){
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
    	}
	    return addedMat;
    }
    
    public static String[][] findMostSimilarSet(Map<Point,String> addedMat,String[] testSet){
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
        String[][] mostSimilarSet2 = new String[mostSimilarSet.size()][2];
        int count = 0;
        for (Map.Entry<String,Float> entry : mostSimilarSet.entrySet()){
        	mostSimilarSet2[count][0] = entry.getKey();
        	mostSimilarSet2[count][1] = Float.toString(entry.getValue());
        	count += 1;
        }
        return mostSimilarSet2;
    }
    
    // commonMat 100k * ~10
    // mostSimilarSet 500 ^2 
    public static Map<Integer,Map<String,ArrayList<String>>> findCommonItems(Map<Point,ArrayList<String>> commonMat, String[] testSet, String[][] mostSimilarSet){
    	Map<Integer,Map<String,ArrayList<String>>> commonItems = new HashMap<Integer,Map<String,ArrayList<String>>>();
    	
    	for (int i = 0; i < mostSimilarSet.length; i++){
			Map<String,ArrayList<String>> commonItemsSingle = new HashMap<String,ArrayList<String>>();
    		for (Map.Entry<Point,ArrayList<String>> entry : commonMat.entrySet()){
    			if (entry.getKey().y == 0 
    					&& entry.getValue().get(0).toString().equals(mostSimilarSet[i][0])){
    				for (Map.Entry<Point,ArrayList<String>> entry2 : commonMat.entrySet()){
    					if (entry2.getKey().x == entry.getKey().x){
    						if (entry2.getKey().y != 0){
	    						for (int j = 0; j < testSet.length; j++){
	    							if (testSet[j].equals(commonMat.get(new Point(0,entry2.getKey().y)).get(0).toString())){
	    								commonItemsSingle.put(testSet[j], entry2.getValue());
	    							}
	    						}
    						}
    					}
    				}
    			}
    		}
			commonItems.put(i,commonItemsSingle);
    	}
    	return commonItems;
    }
    
}
