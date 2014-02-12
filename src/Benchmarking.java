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
import org.intermine.pathquery.OuterJoinStatus;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;

public class Benchmarking {

	private static final String ROOT = "http://beta.flymine.org/beta/service";

    /**
     * Perform the query and print the rows of results.
     * @param args command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	
    	long t1 = System.currentTimeMillis();
    	
        // now have list of configs
        // save the config but keep a list of the views
        
        // Build the query
        ServiceFactory factory = new ServiceFactory(ROOT);
        Model model = factory.getModel();
        PathQuery query = new PathQuery(model);
        
     // Select the output columns:
        query.addViews("Gene.primaryIdentifier",
                "Gene.proteins.proteinDomains.shortName",
                "Gene.proteins.proteinDomains.primaryIdentifier",
                "Gene.pathways.name",
                "Gene.pathways.identifier");

        // Add orderby
        query.addOrderBy("Gene.primaryIdentifier", OrderDirection.ASC);

        // Filter the results with the following constraints:
        query.addConstraint(Constraints.eq("Gene.organism.name", "Drosophila melanogaster"), "A");
        query.addConstraint(Constraints.eq("Gene.symbol", "z*"), "B");
        // Specify how these constraints should be combined.
        query.setConstraintLogic("A and B");

        // Outer Joins
        // Show all information about these relationships if they exist, but do not require that they exist.
        query.setOuterJoinStatus("Gene.pathways", OuterJoinStatus.OUTER);
        query.setOuterJoinStatus("Gene.proteins", OuterJoinStatus.OUTER);
        
        QueryService service = factory.getQueryService();
        PrintStream out = System.out;
        
        long t2 = System.currentTimeMillis();
        
        // Generate the matrices out of the query
        Iterator<List<Object>> rows = service.getRowListIterator(query);
        String tmp;
        int countRow = 0;
        int countColumnPD = 0;
        int countColumnP = 0;
//        out.print(service.getCount(query) + "\n");
        	
        Map<Point, String> matrixPD = new HashMap<Point, String>();
        Map<Point, String> matrixP = new HashMap<Point, String>();

        while (rows.hasNext()) {
            List<Object> row = rows.next();
        	for (int i = 0; i < 5; i++){
        		tmp = row.get(i).toString();
        		if (i==0){
	   				if (!matrixPD.containsValue(tmp)){
	       				matrixPD.put(new Point(countRow, 0), tmp);
	       				matrixP.put(new Point(countRow, 0), tmp);
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
        		
        		// Pathways
        		if (i==4 && tmp != "null"){
        			int saved = 0;
        			Point p = new Point(countRow-1, countColumnP+1);
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
    long t3 = System.currentTimeMillis();
        
        // Output of the data storehouse results
//        out.print("\nData about Protein Domains: \n");
//        for ( int i = 0; i < countRow; i++ ) {
//            for ( int j = 0; j < countColumnPD+1; j++ ) { 
//               String val = matrixPD.get(new Point(i, j));
//               out.print(val + " ");
//            }
//            out.print("\n");
//        } 
//        out.print("\nData about Pathways: \n");
//        for ( int i = 0; i < countRow; i++ ) {
//        	for ( int j = 0; j < countColumnP+1; j++ ) { 
//        		String val = matrixP.get(new Point(i, j));
//        		out.print(val + " ");
//        	}
//        	out.print("\n");
//        }
  	out.print("\n" + service.getCount(query) + " genes, protein domains and pathways, 1 query, 2 outer joins: \n"
//  		+ views[0] + ", \n" + views[2] + ", \n" + views[4] + ", \n" + views[6] + ", \n" + views[8] + ":\n"
  		+ (t2 - t1) + "ms for the query settings\n"
  		+ (t3 - t2) + "ms to generate matrices out of the query\n"
  		+ (t3 - t1) + "ms all together");
  	out.print("\n");
    }
}
