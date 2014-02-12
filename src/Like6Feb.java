import java.awt.Point;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.intermine.metadata.Model;
import org.intermine.pathquery.Constraints;
import org.intermine.pathquery.OrderDirection;
import org.intermine.pathquery.OuterJoinStatus;
import org.intermine.pathquery.PathQuery;
import org.intermine.util.PropertiesUtil;
import org.intermine.webservice.client.core.ServiceFactory;
import org.intermine.webservice.client.services.QueryService;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

//////////////////////////////////////////////////////////////////////////////////

// Protein domains; 
// Pathways;
// Transcripts;
// Length;
// Aspects user-defined combined;
// Configuration file;
// Compare genes;
// Compare set of genes;
// Finger print

//////////////////////////////////////////////////////////////////////////////////
public class Like6Feb {
	
	private static final String ROOT = "http://beta.flymine.org/beta/service";

    /**
     * Perform the query and print the rows of results.
     * @param args command line arguments
     * @throws IOException
     */
	
    public static void main(String[] args) throws IOException {
    	
    	// where the config file is?
    	// user typed in
    	// extra constraints supplied by user
    	// [future] - show me genes like eve that are in the organism: mouse
    	
    	long t1 = System.currentTimeMillis();
    	// Read in the configuration file    	
    	Properties prop = new Properties();
    	// put in config file (later)
        String fileName = "/home/selma/workspace/LIKE/annotationConfig.txt";
        InputStream is = new FileInputStream(fileName);
        prop.load(is);
        
        String[] views = new String[10];
        int[] saveViews = new int[10];
        int countViews = 2;
        
// configs = list of the different things you are going to compare
        // annotation, descriptors
        // loop through the properties and buiild up a configuration 
//        Enumeration<?> propNames = props.propertyNames();
//
//        while (propNames.hasMoreElements()) {
//            String mineId =  (String) propNames.nextElement();
//            mineId = mineId.substring(0, mineId.indexOf("."));
//            Properties mineProps = PropertiesUtil.stripStart(mineId,
            		
        views[0] = prop.getProperty("recommendation.engine.query.1.identifier");
        views[1] = prop.getProperty("recommendation.engine.query.1.name");
        saveViews[0] = 0;
        saveViews[1] = 1;
        if (prop.getProperty("recommendation.engine.query.2.required").equals("yes")){
        	views[2] = prop.getProperty("recommendation.engine.query.2.identifier");
        	views[3] = prop.getProperty("recommendation.engine.query.2.name");
            saveViews[countViews] = 2;
            countViews += 1;
            saveViews[countViews] = 3;
            countViews += 1;
        }
        if (prop.getProperty("recommendation.engine.query.3.required").equals("yes")){
        	views[4] = prop.getProperty("recommendation.engine.query.3.identifier");
        	views[5] = prop.getProperty("recommendation.engine.query.3.name");
            saveViews[countViews] = 4;
            countViews += 1;
            saveViews[countViews] = 5;
            countViews += 1;
        }
        if (prop.getProperty("recommendation.engine.query.4.required").equals("yes")){
        	views[6] = prop.getProperty("recommendation.engine.query.4.identifier");
//        	views[7] = prop.getProperty("recommendation.engine.query.4.name");
            saveViews[countViews] = 6;
            countViews += 1;
//            saveViews[countViews] = 7;
//            countViews += 1;
        }
        if (prop.getProperty("recommendation.engine.query.5.required").equals("yes")){
        	views[8] = prop.getProperty("recommendation.engine.query.5.identifier");
//        	views[9] = prop.getProperty("recommendation.engine.query.5.name");
            saveViews[countViews] = 8;
            countViews += 1;
//            saveViews[countViews] = 9;
//            countViews += 1;
        }
    	
        // now have list of configs
        // save the config but keep a list of the views
        
        // Build the query
        ServiceFactory factory = new ServiceFactory(ROOT);
        Model model = factory.getModel();
        PathQuery query = new PathQuery(model);
        
        // Select the output columns:
        query.addViews(views[0]);
        for (int i = 0; i < views.length; i ++){
        	if(saveViews[i] != 0){
        		query.addViews(views[saveViews[i]]);
        	}
        }

        // Add orderby
        query.addOrderBy(views[0], OrderDirection.ASC);

        // "filters" are going to come from the form itself, not the config file
        
        // Filter the results with the following constraints:
        int constraintA = 0;
        if (prop.getProperty("recommendation.engine.constraint.1.required").equals("yes")){
        	query.addConstraint(Constraints.eq(
        			prop.getProperty("recommendation.engine.constraint.1.what"), 
        			prop.getProperty("recommendation.engine.constraint.1.how")), "A");
        	constraintA = 1;
        }
        if (prop.getProperty("recommendation.engine.constraint.2.required").equals("yes")){
        	if (constraintA == 0){
	        	query.addConstraint(Constraints.eq(
	        			prop.getProperty("recommendation.engine.constraint.2.what"), 
	        			prop.getProperty("recommendation.engine.constraint.2.how")), "A");
        	}
        	else
        		query.addConstraint(Constraints.eq(
            			prop.getProperty("recommendation.engine.constraint.2.what"), 
            			prop.getProperty("recommendation.engine.constraint.2.how")), "B");
        }
        // Specify how these constraints should be combined.
        query.setConstraintLogic("A and B");

        // remove this section
        
        // Outer Joins
        // Show all information about these relationships if they exist, but do not require that they exist.
        if (prop.getProperty("recommendation.engine.query.2.required").equals("yes") 
        		&& prop.getProperty("recommendation.engine.outerJoins.1.required").equals("yes")){
        	query.setOuterJoinStatus(
        			prop.getProperty("recommendation.engine.outerJoins.1.what"),
        			OuterJoinStatus.OUTER);
        }
        if (prop.getProperty("recommendation.engine.query.2.required").equals("yes") 
        		&& prop.getProperty("recommendation.engine.outerJoins.2.required").equals("yes")){
        	query.setOuterJoinStatus(
        			prop.getProperty("recommendation.engine.outerJoins.2.what"),
        			OuterJoinStatus.OUTER);
        }
        if (prop.getProperty("recommendation.engine.query.3.required").equals("yes") 
        		&& prop.getProperty("recommendation.engine.outerJoins.3.required").equals("yes")){
        	query.setOuterJoinStatus(
        			prop.getProperty("recommendation.engine.outerJoins.3.what"),
        			OuterJoinStatus.OUTER);
        }
        if (prop.getProperty("recommendation.engine.query.4.required").equals("yes") 
        		&& prop.getProperty("recommendation.engine.outerJoins.4.required").equals("yes")){
        	query.setOuterJoinStatus(
        			prop.getProperty("recommendation.engine.outerJoins.4.what"),
        			OuterJoinStatus.OUTER);
        }
        
        QueryService service = factory.getQueryService();
        PrintStream out = System.out;
        long t2 = System.currentTimeMillis();
        // Generate the matrices out of the query
        Iterator<List<Object>> rows = service.getRowListIterator(query);
        String tmp;
        int countRow = 0;
        int countColumnPD = 0;
        int countColumnP = 0;
        int countColumnT = 0;
        out.print(service.getCount(query) + "\n");
//        String format = "%-13.13s | %-13.13s | %-13.13s | %-13.13s | %-13.13s | %-13.13s\n";
        	
        Map<Point, String> matrixPD = new HashMap<Point, String>();
        Map<Point, String> matrixP = new HashMap<Point, String>();
        Map<Point, String> matrixT = new HashMap<Point, String>();
        Map<Point, String> matrixL = new HashMap<Point, String>();

        while (rows.hasNext()) {
            List<Object> row = rows.next();
//        	out.printf(format, row.toArray());
        	for (int i = 0; i < countViews; i++){
        		tmp = row.get(i).toString();
        		if (i==0){
	   				if (!matrixPD.containsValue(tmp)){
	       				matrixPD.put(new Point(countRow, 0), tmp);
	       				matrixP.put(new Point(countRow, 0), tmp);
        				matrixT.put(new Point(countRow, 0), tmp);
    					matrixL.put(new Point(countRow, 0), tmp);
	       				countRow += 1;
	   				}
        		}
        		
        		// Protein domains
        		if (saveViews[i]==2 && tmp!="null"){
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
        		if (saveViews[i]==4 && tmp!="null"){
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
        		
        		// Transcripts
        		if (saveViews[i]==6 && tmp!="null"){
//        			if (!matrixT.containsValue(tmp)){
        				countColumnT += 1;
    					matrixT.put(new Point(countRow-1, countColumnT), tmp);
//        			}
//        			else {
//        				int saved = 0;
//        				for (int j = 0; j <= countRow; j++){
//        					for (int k = 0; k <= countColumnT+1; k++){
//        						if (saved == 0 && tmp.equals(matrixT.get(new Point(j, k)))){
//        							matrixT.put(new Point(countRow-1, k), tmp);
//        							saved += 1;
//        						}
//        					}
//        				}
//        			}
        		}
        		
        		// Length
        		if (saveViews[i]==8 && tmp!="null"){
        			matrixL.put(new Point(countRow-1, 1), tmp);
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
//        out.print("\nData about Transcripts: \n");
//        for ( int i = 0; i < countRow; i++ ) {
//        	for ( int j = 0; j < countColumnT+1; j++ ) { 
//        		String val = matrixT.get(new Point(i, j));
//        		out.print(val + " ");
//        	}
//        	out.print("\n");
//        } 
//        out.print("\nData about Length: \n");
//        for ( int i = 0; i < countRow; i++ ) {
//        	for ( int j = 0; j < 2; j++ ) { 
//        		String val = matrixL.get(new Point(i, j));
//        		out.print(val + " ");
//        	}
//        	out.print("\n");
//        } 
        
        out.print("\n" + service.getCount(query) + " genes including: \n"
        		+ views[0] + ", \n" + views[2] + ", \n" + views[4] + ", \n" + views[6] + ", \n" + views[8] + ":\n"
        		+ (t2 - t1) + "ms to read in the config file & query settings\n"
        		+ (t3 - t2) + "ms to generate matrices out of the query\n"
        		+ (t3 - t1) + "ms all together");
        out.print("\nData about Protein Domains: \n");
        
        /////*** Generate the similarity matrices ***/////
        Map<Point, String> simMatPD = new HashMap<Point, String>();
        Map<Point, String> normWeightedMatPD = new HashMap<Point, String>();
        Map<Point, String> simMatP = new HashMap<Point, String>();
        Map<Point, String> normWeightedMatP = new HashMap<Point, String>();
        Map<Point, String> simMatT = new HashMap<Point, String>();
        Map<Point, String> normWeightedMatT = new HashMap<Point, String>();
        Map<Point, String> simMatL = new HashMap<Point, String>();
        Map<Point, String> normWeightedMatL = new HashMap<Point, String>();
        
//        for (int i = 2; i < countViews; i++){
//	        if (saveViews[i] == 2){ // PD
//	        	simMatPD = calculateMatrix(matrixPD);
//	          	out.print("\nNumber of Protein Domains in common: \n");
//	          	for (int j = 0; j < countRow; j++) {
//	          		for (int k = 0; k < countColumnPD+1; k++) {
//	          			String val = matrixPD.get(new Point(i, j));
//		                   out.print(val + " ");
//	          		}
//	          		out.print("\n");
//	          	}	          	
//	          	String[][] normMatPD = normalize(simMatPD);
//	            normWeightedMatPD = addWeigth(normMatPD, Float.parseFloat(prop.getProperty("recommendation.engine.query.2.weight")));
//	            out.print("\nNormalised Number of Protein Domains in common: \n");
//	            for (int j = 0; j < normWeightedMatPD.length; j++) {
//	                for (int k = 0; k < normWeightedMatPD[0].length; k++) {
//	                    out.print(normWeightedMatPD[j][k] + " ");
//	                }
//	                out.print("\n");
//	            }
//	          	
//	        }
//	        if (saveViews[i] == 4){ // P
//	        	simMatP = calculateMatrix(matrixP);
////	            out.print("\nNumber of Pathways in common: \n");
////	            for (int j = 0; j < simMatP.length; j++) {
////	                for (int k = 0; k < simMatP[0].length; k++) {
////	                    out.print(simMatP[j][k] + " ");
////	                }
////	                out.print("\n");
////	            }
//	        	String[][] normMatP = normalize(simMatP);
//	            normWeightedMatP = addWeigth(normMatP, Float.parseFloat(prop.getProperty("recommendation.engine.query.3.weight")));
////	            out.print("\nNormalised Number of Pathways in common: \n");
////	            for (int j = 0; j < normWeightedMatP.length; j++) {
////	                for (int k = 0; k < normWeightedMatP[0].length; k++) {
////	                    out.print(normWeightedMatP[j][k] + " ");
////	                }
////	                out.print("\n");
////	            }
//	        }
//	        if (saveViews[i] == 6){ // T
//	        	simMatT = calculateMatrixDif(calculateMatrixT(matrixT));
////	            out.print("\nNumber of Transcripts differences: \n");
////	            for (int j = 0; j < simMatT.length; j++) {
////	                for (int k = 0; k < simMatT[0].length; k++) {
////	                    out.print(simMatT[j][k] + " ");
////	                }
////	                out.print("\n");
////	            }
//	            String[][] normMatT = normalize(simMatT);
//	            String[][] revNormMatT = reverse(normMatT);
//	            normWeightedMatT = addWeigth(revNormMatT, Float.parseFloat(prop.getProperty("recommendation.engine.query.4.weight")));
////	            out.print("\nNormalised Number of Transcripts differences: \n");
////	            for (int j = 0; j < normWeightedMatT.length; j++) {
////	                for (int k = 0; k < normWeightedMatT[0].length; k++) {
////	                    out.print(normWeightedMatT[j][k] + " ");
////	                }
////	                out.print("\n");
////	            }
//	        }
//	        if (saveViews[i] == 8){ // L
//	        	simMatL = calculateMatrixDif(matrixL);
////	            out.print("\nNumber of Length differences: \n"); 
////	            for (int j = 0; j < simMatL.length; j++) {
////	                for (int k = 0; k < simMatL[0].length; k++) {
////	                    out.print(simMatL[j][k] + " ");
////	                }
////	                out.print("\n");
////	            }
//	            String[][] normMatL = normalize(simMatL);
//	            String[][] revNormMatL = reverse(normMatL);
//	            normWeightedMatL = addWeigth(revNormMatL, Float.parseFloat(prop.getProperty("recommendation.engine.query.5.weight")));
////	            out.print("\nNormalised Number of Length differences: \n");
////	            for (int j = 0; j < normWeightedMatL.length; j++) {
////	                for (int k = 0; k < normWeightedMatL[0].length; k++) {
////	                    out.print(normWeightedMatL[j][k] + " ");
////	                }
////	                out.print("\n");
////	            }
//	        }
//        }
        
        // Merge the matrices
//        String[][] addedMat = new String[service.getCount(query)+1][service.getCount(query)+1];
//        for (int i = 2; i < countViews; i++){
//	        if (saveViews[i] == 2){
//	        	addedMat = addMatrizes(addedMat,normWeightedMatPD);
//	        }
//	        if (saveViews[i] == 4){
//	        	addedMat = addMatrizes(addedMat,normWeightedMatP);
//	        }
//	        if (saveViews[i] == 6){
//	        	addedMat = addMatrizes(addedMat,normWeightedMatT);
//	        }
//	        if (saveViews[i] == 8){
//	        	addedMat = addMatrizes(addedMat,normWeightedMatL);
//	        }
//        }
        
        long t4 = System.currentTimeMillis();
        
//        out.print("\nAll Aspects combined: \n");
//        for (int i = 0; i < addedMat.length; i++) {
//            for (int j = 0; j < addedMat[0].length; j++) {
//                out.print(addedMat[i][j] + " ");
//            }
//            out.print("\n");
//        }
        
        // Return most similar genes
//        String[][] mostSimilarGenes = showMostSimilarGenes(addedMat);
//        mostSimilarGenes = orderByHighestValues3(mostSimilarGenes);
//        out.print("\nMost Similar Genes: \n");
//        for (int i = 0; i < mostSimilarGenes.length; i++) {
//            for (int j = 0; j < mostSimilarGenes[0].length; j++) {
//                out.print(mostSimilarGenes[i][j] + " ");
//            }
//            out.print("\n");
//        }
        
        // Return the most similar set of genes
//        String testSetAll = prop.getProperty("recommendation.engine.testSet");
//        String[] testSet = testSetAll.split(",");
//        String[][] mostSimilarSet = showMostSimilarSet(addedMat, testSet);
//        mostSimilarSet = orderByHighestValues(mostSimilarSet);
//        int kNearestSet = Integer.parseInt(prop.getProperty("recommendation.engine.kNearestSet"));
//        mostSimilarSet = trimMatrix(mostSimilarSet,kNearestSet);
//        out.print("\nMost Similar Set of Genes to the testSet: \n");
//        for (int i = 0; i < mostSimilarSet.length; i++) {
//            for (int j = 0; j < mostSimilarSet[0].length; j++) {
//                out.print(mostSimilarSet[i][j] + " ");
//            }
//            out.print("\n");
//        }
        // get objects
//        Iterator<List<Object>> objects = service.getRowListIterator(query);
//        List<Object> mostSimilarSetObj = new ArrayList<Object>();
//        while (objects.hasNext()) {
//            List<Object> row = objects.next();
//        	tmp = row.get(0).toString();
//        	for (int i = 0; i < mostSimilarSet.length; i++){
//        		if(tmp.equals(mostSimilarSet[i][0])
//        				&& !mostSimilarSetObj.contains(row)){
//        			mostSimilarSetObj.add(row);
////        			out.print(tmp + "\n");
//        		}
//        	}
//        }
        long t5 = System.currentTimeMillis();
        // Fingerprint
        String searchedGene = prop.getProperty("recommendation.engine.searchedGene");
        int kNearestGene = Integer.parseInt(prop.getProperty("recommendation.engine.kNearestGene"));
        if (kNearestGene >= service.getCount(query)){
        	kNearestGene = service.getCount(query)-1;
    	}
        String[][] fingerprintTmp = new String[service.getCount(query)][2];
        String[][] fingerprint = new String[kNearestGene+1][5];
        String[][] countTotal = new String[fingerprintTmp.length][2];
        int countAspects = 0;
        for (int i = 2; i < countViews; i++){
//	        if (saveViews[i] == 2){
//	        	fingerprintTmp = orderByHighestValues(findKNearestNeighbours(simMatPD, searchedGene));
//	        	countTotal = orderByTotalLowestDifference(fingerprintTmp, findTotal(fingerprintTmp, matrixPD));
//	        	fingerprintTmp = trimMatrix(fingerprintTmp, kNearestGene);
//	        	
//	        	fingerprint[0][countAspects] = "|# Common Protein Domains";
//	        	for (int j = 0; j < fingerprintTmp.length; j ++){
//	        		fingerprint[j+1][countAspects] = "|" + countTotal[j][0] + " with " + fingerprintTmp[j][1]
//	        				+ " (total " + countTotal[j][1] + ")";
//	        	}
//	        	countAspects += 1;
//	        }
//	        if (saveViews[i] == 4){
//	        	fingerprintTmp = orderByHighestValues(findKNearestNeighbours(simMatP, searchedGene));
//	        	countTotal = orderByTotalLowestDifference(fingerprintTmp, findTotal(fingerprintTmp, matrixP));
//	        	fingerprintTmp = trimMatrix(fingerprintTmp, kNearestGene);
//	        	
//	        	fingerprint[0][countAspects] = "|# Common Pathways       ";
//	        	for (int j = 0; j < fingerprintTmp.length; j ++){
//	        		fingerprint[j+1][countAspects] = "|" + countTotal[j][0] + " with " + fingerprintTmp[j][1]
//	        				+ " (total " + countTotal[j][1] + ")";
//	        	}
//	        	countAspects += 1;
//	        }
//	        if (saveViews[i] == 6){
//	        	fingerprintTmp = orderByLowestValues(findKNearestNeighbours(simMatT, searchedGene));
//	        	countTotal = orderByTotalHighestDifference(fingerprintTmp, findTotal(fingerprintTmp, matrixT));
//	        	fingerprintTmp = trimMatrix(fingerprintTmp, kNearestGene);
//	        	
//	        	fingerprint[0][countAspects] = "|#Transcripts differences";
//	        	for (int j = 0; j < fingerprintTmp.length; j ++){
//	        		fingerprint[j+1][countAspects] = "|" + countTotal[j][0] + " with " + fingerprintTmp[j][1]
//	        				+ " (total " + countTotal[j][1] + ")";
//	        	}
//	        	countAspects += 1;
//	        }
//	        if (saveViews[i] == 8){
//	        	fingerprintTmp = orderByLowestValues(findKNearestNeighbours(simMatL, searchedGene));
//	        	for (int j = 0; j < fingerprintTmp.length; j ++){
//	        		for (int k = 0; k < matrixL.length; k ++){
//	        			if (fingerprintTmp[j][0].equals(matrixL[k][0])){
//	        				countTotal[j][0] = matrixL[k][0];
//	        				countTotal[j][1] = matrixL[k][1];
//	        			}
//	        		}
//	        	}
//	        	fingerprintTmp = trimMatrix(fingerprintTmp, kNearestGene);
//	        	
//	        	fingerprint[0][countAspects] = "| Length differences            ";
//	        	for (int j = 0; j < fingerprintTmp.length; j ++){
//	        		fingerprint[j+1][countAspects] = "|" + countTotal[j][0] + " with " + fingerprintTmp[j][1]
//	        				+ " (total " + countTotal[j][1] + ")";
//	        	}
//	        	countAspects += 1;
//	        }
        }
//        fingerprintTmp = findKNearestNeighbours(addedMat, searchedGene);
    	fingerprintTmp = orderByHighestValues(fingerprintTmp);
    	fingerprintTmp = trimMatrix(fingerprintTmp, kNearestGene);
    	
        fingerprint[0][countAspects] = "| All Aspects Combined";
        for (int j = 0; j < fingerprintTmp.length; j ++){
    		fingerprint[j+1][countAspects] = "|" + fingerprintTmp[j][0] + " with " + fingerprintTmp[j][1];
    	}
        out.print("\nFingerprint of " + searchedGene + ":\n");
        for (int i = 0; i < fingerprint.length; i++) {
            for (int j = 0; j < fingerprint[0].length; j++) {
                out.print(fingerprint[i][j] + " ");
            }
            out.print("\n");
        }
        
        long t6 = System.currentTimeMillis();
    
        out.print("\n" + service.getCount(query) + " genes including: \n"
        		+ views[0] + ", \n" + views[2] + ", \n" + views[4] + ", \n" + views[6] + ", \n" + views[8] + ":\n"
        		+ (t2 - t1) + "ms to read in the config file & query settings\n"
        		+ (t3 - t2) + "ms to generate matrices out of the query\n"
        		+ (t4 - t3) + "ms to generate the similarity matrix addedMat\n"
//        		+ (t5 - t4) + "ms to find most similar set of genes (" + testSet.length + " genes in the testSet, " + kNearestSet + " nearest neighbours)\n"
        		+ (t6 - t5) + "ms to generate the fingerprint (" + kNearestGene + " nearest neighbours)\n" 
        		+ (t6 - t1) + "ms all together"); 

    }

    
    public static Map<Point, String> calculateMatrix(Map<Point, String> matrix) {
    	// Compare matrix matrix to all other Genes
    	
    	Map<Point, String> simMat = new HashMap<Point, String>();
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

    public static String[][] calculateMatrixT(String[][] matrixT){
    	String[][] simMatT = new String[matrixT.length][2];
    	
    	for (int i = 0; i < matrixT.length; i++){
    		simMatT[i][0] = matrixT[i][0];
    		int count = 0;
    		for (int j = 1; j < matrixT[i].length; j++){
    			if (matrixT[i][j] != null){
    				count += 1;
    			}
    		}
    		simMatT[i][1] = Integer.toString(count);
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
    
    public static String[][] addWeigth(String[][] normMat, float weigth){
    	String[][] normWeightedMat = new String[normMat.length][normMat.length];
    	
    	for (int i = 0; i < normMat.length; i++){
    		normWeightedMat[i][0] = normMat[i][0];
    		normWeightedMat[0][i] = normMat[i][0];
    	}
    	
    	for (int i = 1; i < normMat.length; i++){
    		for (int j = 1; j < normMat.length; j++){
    			if (i != j){
    				normWeightedMat[i][j] = Float.toString(Float.parseFloat(normMat[i][j])*weigth);
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
    
    public static String[][] addMatrizes(String[][] normMat, String[][] normMat2){
    	if (normMat[0][1] == null){
    		return normMat2;
    	}
    	else {
	    	String[][] addedMat = new String[normMat.length][normMat.length];
	    	
	    	for (int i = 0; i < normMat.length; i++){
	    		addedMat[0][i] = normMat[i][0];
	    		addedMat[i][0] = normMat[i][0];
	    	}
	    	
	    	for (int i = 1; i < normMat.length; i ++){
	    		for(int j = 1; j < normMat.length; j++){
	    			if (i != j){
	    				addedMat[i][j] = Float.toString(Float.parseFloat(normMat[i][j])+Float.parseFloat(normMat2[i][j]));
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