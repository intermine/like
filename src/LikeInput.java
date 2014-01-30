
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.intermine.metadata.Model;
//import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.PathQuery;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;



public final class LikeInput {
	
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
	        query.addViews("Gene.primaryIdentifier",
	                "Gene.symbol",
	                "Gene.proteins.proteinDomains.name",
	                "Gene.proteins.proteinDomains.primaryIdentifier");

	        /*
	         * Gene.primaryIdentifier: unique so use for calculations
	         * Gene.symbol: human readable
	         * 
	         *  ProteinDomain.name: human readable
	         *  ProteinDomain.primaryIdentifier: unique
	         */
	        
	        // Add orderby
	        query.addOrderBy("Gene.secondaryIdentifier", OrderDirection.ASC);

	        // Filter the results with the following constraints:
//	        query.addConstraint(Constraints.lookup("Gene", "zen", ""), "A");
	        
	        // Specify how these constraints should be combined.
//	        query.setConstraintLogic("A and B");

	        QueryService service = factory.getQueryService();
	        PrintStream out = System.out;
	        String format = "%-12.12s | %-12.12s | %-12.12s | %-12.12s | %-12.12s | %-12.12s | %-12.12s | %-12.12s\n";
	        out.printf(format, query.getView().toArray());
	        Iterator<List<Object>> rows = service.getRowListIterator(query);
	        while (rows.hasNext()) {
//	        	List<Object> row = rows.next();
	        	// the gene and the domain ==> matrix
	        }
	        out.printf("%d rows\n", service.getCount(query));
	    }
	    
	    // prompt for gene symbol (dummy for the UI - normally user would be on a report page for zen)
	    
	    // return similar genes using algorithm
}
