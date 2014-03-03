import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.intermine.metadata.Model;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.OuterJoinStatus;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;

public class LikePrecalculate {

	private static final String ROOT = "http://beta.flymine.org/beta/service";

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
    	
        long t9 = 0;
    	for (int i = 0; i < views.size()/4; i++){
    		Map<Point,String> matrix = new HashMap<Point,String>();
    		Map<Point,ArrayList<String>> commonMat = new HashMap<Point,ArrayList<String>>();
            Map<Point,String> simMat = new HashMap<Point,String>();
            Map<Point,String> normMat = new HashMap<Point,String>();
    		long t3 = System.currentTimeMillis();
    		
    		// Build query i
    		Iterator<List<Object>> rows = buildQuery(views,i);
    		long t4 = System.currentTimeMillis();
        	out.print((t4-t3) + "ms to build query " + i + "\n");
    	
	        // Generate matrix i out of the query
	        matrix = runQuery(rows);
	        long t5 = System.currentTimeMillis();
	    	out.print((t5-t4) + "ms to run query " + i + "\n");
	    	
	        // Calculate common items i
	    	commonMat = findCommonItems(matrix);
	    	long t6 = System.currentTimeMillis();
	    	out.print((t6-t5) + "ms to find common items " + i + "\n");
	    	matrix = new HashMap<Point,String>();
	    	
	    	File commonItems = new File("CommonItems" + i);
		    FileOutputStream f1 = new FileOutputStream(commonItems);
		    ObjectOutputStream s1 = new ObjectOutputStream(f1);
		    s1.writeObject(commonMat);
		    s1.close();
	    	
		    long t7 = System.currentTimeMillis();
	    	out.print((t7-t6) + "ms to store common items " + i + "\n");
		    
	    	// Calculate number of common items i
	    	simMat = countCommonItems(commonMat);
	        long t8 = System.currentTimeMillis();
	    	out.print((t8-t7) + "ms to count common items " + i + "\n");
	    	commonMat = new HashMap<Point,ArrayList<String>>();
	    	
	        // Normalise similarity matrix i
	        normMat = normalize(simMat);
	      	t9 = System.currentTimeMillis();
	    	out.print((t9-t8) + "ms to normalise matrix " + i + "\n");
	    	simMat = new HashMap<Point,String>();
	    	
		    File similarityMatrix = new File("SimilarityMatrix" + i);
		    FileOutputStream f2 = new FileOutputStream(similarityMatrix);
		    ObjectOutputStream s2 = new ObjectOutputStream(f2);
		    s2.writeObject(normMat);
		    s2.close();
    	}
    	out.print("-> " + (t9-t1) + "ms to precalculate" + "\n");
    	
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

    public static Iterator<List<Object>> buildQuery(Map<Point,String> views, int i){
    	// Build the query
        ServiceFactory factory = new ServiceFactory(ROOT);
        Model model = factory.getModel();
        PathQuery query = new PathQuery(model);
        
        // add views
    	query.addViews("Gene.id",views.get(new Point(i,1)));
		
		// Add order by
        query.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);
        // Filter the results with the following constraints:
        query.addConstraint(Constraints.eq("Gene.organism.name", "Drosophila melanogaster"), "A");
        query.addConstraint(Constraints.eq("Gene.symbol", "z*"), "B");
        // Specify how these constraints should be combined.
        query.setConstraintLogic("A and B");
        
        // Outer Joins
        // Show all information about these relationships if they exist, but do not require that they exist.
        query.setOuterJoinStatus(views.get(new Point(i,0)), OuterJoinStatus.OUTER);
        
        QueryService service = factory.getQueryService();
        
    	Iterator<List<Object>> rows = service.getRowListIterator(query);
		
		return rows;
    }
    
    public static Map<Point,String> runQuery(Iterator<List<Object>> rows){
    	Map<Point,String> matrix = new HashMap<Point,String>();
    	String tmp;
        int tmpRow = 0;
        int highestIndex = -1;
        int countColumnPD = 0;
        
    	while (rows.hasNext()) {
            List<Object> row = rows.next();
        	for (int i = 0; i < 2; i++){
        		tmp = row.get(i).toString();
        		if (i==0){
        			int saved = 0;
        			for (Map.Entry<Point,String> entry : matrix.entrySet()){
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
        			matrix.put(new Point(tmpRow, 0), tmp);
        		}
        		
        		if (i==1 && tmp != "null"){
        			int saved = 0;
        			Point p = new Point(tmpRow, countColumnPD+1);
        			for (Map.Entry<Point, String> entry : matrix.entrySet()){
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
  
    public static Map<Point,ArrayList<String>> findCommonItems(final Map<Point,String> matrix) {
    	Map<Point,ArrayList<String>> commonMat = new HashMap<Point,ArrayList<String>>();
    	for (final Map.Entry<Point,String> entry : matrix.entrySet()){
    		if (entry.getKey().y == 0){
		    	Map<Integer,ArrayList<String>> count = new HashMap<Integer,ArrayList<String>>();
		    	commonMat.put(new Point(0,entry.getKey().x+1),new ArrayList<String>(){{add(matrix.get(new Point(entry.getKey().x,0)));}});
		    	commonMat.put(new Point(entry.getKey().x+1,0),new ArrayList<String>(){{add(matrix.get(new Point(entry.getKey().x,0)));}});
		    	for (Map.Entry<Point,String> entry2 : matrix.entrySet()){
	    			if (entry2.getKey().x == entry.getKey().x){
	    				for (final Map.Entry<Point,String> entry3 : matrix.entrySet()){
	    					if (entry.getKey() != entry3.getKey()
	    							&& entry2.getValue().equals(entry3.getValue())){
	    						if (count.get(entry3.getKey().x) == null){
	    							count.put(entry3.getKey().x, new ArrayList<String>(){{add(entry3.getValue());}});
	    						}
	    						else {
	    							ArrayList<String> tmp = count.get(entry3.getKey().x);
	    							tmp.add(entry3.getValue());
	    							count.put(entry3.getKey().x,tmp);
	    						}
	    					}
	    				}
	    			}
				}
	    		for (Map.Entry<Integer,ArrayList<String>> entry5 : count.entrySet()){
	    			commonMat.put(new Point(entry.getKey().x+1, entry5.getKey()+1), entry5.getValue());
	    		}
    		}
    	}
    	return commonMat;
    }
    
    public static Map<Point,String> countCommonItems(Map<Point,ArrayList<String>> commonMat){
    	Map<Point,String> simMat = new HashMap<Point,String>();
    	
    	for (Map.Entry<Point,ArrayList<String>> entry : commonMat.entrySet()){
    		if (entry.getKey().x == 0 || entry.getKey().y == 0){
    			simMat.put(entry.getKey(), entry.getValue().get(0).toString());
    		}
    		else {
    			simMat.put(entry.getKey(),Integer.toString(entry.getValue().size()));
    		}
    	}
    	
    	return simMat;
    }
    
    public static Map<Point,String> normalize(Map<Point,String> matrix){
    	Map<Point,String> normMat = new HashMap<Point,String>();
        int highestVal = 0;
    	for (Map.Entry<Point,String> entry : matrix.entrySet()){
    		if (entry.getKey().x != 0 && entry.getKey().y != 0
    				&& entry.getKey().x != entry.getKey().y
    				&& Integer.parseInt(entry.getValue()) > highestVal){
    			highestVal = Integer.parseInt(entry.getValue());
    		}
    		else if (entry.getKey().y == 0){
    			normMat.put(new Point(0,entry.getKey().x),matrix.get(new Point(entry.getKey().x,0)));
    			normMat.put(new Point(entry.getKey().x,0),matrix.get(new Point(entry.getKey().x,0)));
    		}
    	}
    	for (Map.Entry<Point,String> entry : matrix.entrySet()){
    		if (entry.getKey().x != 0 && entry.getKey().y != 0
    				&& entry.getKey().x != entry.getKey().y){
    			normMat.put(new Point(entry.getKey()), Float.toString(Float.parseFloat(entry.getValue())/(float)highestVal));
    		}
    	}
    	return normMat;
    }
    
}
