import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.lookback.LookbackQuery;
import com.rallydev.lookback.LookbackResult;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;


public class Feature extends connect {
	
	public  List<BigInteger> all_iter = new ArrayList<BigInteger>();
	public List<BigInteger> iteration_array = new ArrayList<BigInteger>();
	public Iteration iteration;
	Feature() throws Exception{
		super();
		
		this.all_iter = this.iteration.get_all_iterations();
	}
	public JsonObject get_feature(String ObjectID) throws IOException {
		QueryRequest request = new QueryRequest("PortfolioItem/Feature");
		request.setFetch(new Fetch("Name","FormattedID","ObjectID"));
		request.setQueryFilter(new QueryFilter("ObjectID","=",ObjectID));
		
		QueryResponse response = this.rest.query(request);
		
		if(response.wasSuccessful() && response.getTotalResultCount()==1){
			
			for(JsonElement result: response.getResults()){
				JsonObject features = result.getAsJsonObject();
				return features;
			}
		}
		return null;
	}
	
	public void process_all_features() throws Exception{
		QueryRequest request = new QueryRequest("PortfolioItem/Feature");
		request.setFetch(new Fetch("Name","FormattedID","ObjectID"));
		
		QueryResponse response = this.rest.query(request);
		if(response.wasSuccessful()){
			
			
		}
		
		
	}
	
	public void process_feature_leaves(JsonObject feat){
		try{
			boolean unscheduled = false;
			BigInteger fOID = feat.get("ObjectID").getAsBigInteger();
			LookbackQuery query = this.lookback.newSnapshotQuery();
			query.addFindClause("_TypeHierarchy", "HierarchicalRequirement");
			query.addFindClause("_ItemHierarchy", fOID);
			query.addFindClause("Children", null);
			query.addFindClause("__At", "current");
			
			query.requireFields("FormattedID","Iteration","ObjectID","Name");
			query.sortBy("Iteration",-1);
			
			LookbackResult resultSet = query.execute();
			int resultCount = resultSet.Results.size();
			Iterator i = resultSet.Results.listIterator();
			
			Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.SEVERE);
			
			while(i.hasNext()){
				Map m = (Map) i.next();
				if(m.containsKey("Iteration")){
					Double iOID = Double.parseDouble(m.get("Iteration").toString());
					BigInteger iterationOID = new BigDecimal(iOID).toBigInteger();
					this.iteration_array.add(iterationOID);
					
				}
			}
			if(this.iteration_array.size()!=resultCount)
				unscheduled = true;
			
			this.iteration_array.retainAll(this.all_iter);
			if(this.iteration_array.size()>0) System.out.println();
				//String latest_iteration = this.iteration.get_name_of_iteration(this.get_latest_iteration(this.all_iter,this.iteration_array));
			String latest_iteration = this.iteration.get_name_of_iteration(this.iteration.get_latest_iteration(this.all_iter, this.iteration_array));
		}
		catch(Exception e){
			System.out.println("Exception in process_feature_leaves");
			System.out.println("Details "+e);
		}
	}
}
