import com.google.gson.JsonElement;
import java.util.logging.*;
import java.util.regex.*;
import com.rallydev.lookback.LookbackApi;
import com.rallydev.lookback.LookbackQuery;
import com.rallydev.lookback.LookbackResult;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.io.*;
import java.util.HashSet;
import java.util.ArrayList;
import java.net.URI;

import javax.management.Query;
import com.google.gson.JsonObject;
public class dIterationPsiAutomation extends connect {
	public String rallyURI;
	public String appName;
	public String username;
	public String password;
	//public RallyRestApi restApi;
	//public LookbackApi lookback;
	public static List<BigInteger> all_iter = new ArrayList<BigInteger>();
	public List<BigInteger> iteration_array = new ArrayList<BigInteger>();
	public List<BigInteger> temporary_array = new ArrayList<BigInteger>();
	public dIterationPsiAutomation(String rallyURL,String username,String password, String applicationName) throws Exception {
		// TODO Auto-generated constructor stub
		super(rallyURL,username,password,applicationName);
		
		
		get_all_iterations();
	}
	public void getFeatureInfo(String featureName) throws IOException {
		QueryRequest feature = new QueryRequest("portfolioitem/feature");
		feature.setFetch(new Fetch("FormattedID","ObjectID","Name"));
		feature.setQueryFilter(new QueryFilter("Name","=",featureName));
		
		QueryResponse queryResponse = this.rest.query(feature);
		if(queryResponse.wasSuccessful()){
			for(JsonElement result: queryResponse.getResults()){
				JsonObject f = result.getAsJsonObject();
				
			}
		}
		
	}
	public  void get_all_iterations() throws IOException {
		
		QueryRequest iterations = new QueryRequest("Iteration");
		iterations.setFetch(new Fetch("Name","StartDate","EndDate","ObjectID"));
		iterations.setOrder("EndDate DESC");
		
		QueryResponse response = this.rest.query(iterations);
		if(response.wasSuccessful()){
			System.out.println(String.format("Total  count %d", response.getTotalResultCount()));
			for(JsonElement result: response.getResults()){
				
				JsonObject iter = result.getAsJsonObject();
				all_iter.add(iter.get("ObjectID").getAsBigInteger());
			}
		}
		
	}
	
	public void get_formattedID(JsonElement ObjectID){
		
	}
	public void get_prefixed_stories(String prefix){
		try{
			QueryRequest stories = new QueryRequest("hierarchicalrequirement");
			stories.setFetch(new Fetch("Children","Name","Iteration","FormattedID","ObjectID"));
			stories.setQueryFilter(new QueryFilter("Name","contains",prefix));
			
			
			QueryResponse response = this.rest.query(stories);
			if(response.wasSuccessful()){
				for(JsonElement result: response.getResults()){
					JsonObject story = result.getAsJsonObject();
					System.out.println(String.format("%s - %s: ObjectID: %s", story.get("FormattedID").getAsString(), story.get("Name").getAsString(), story.get("ObjectID").getAsBigInteger()));
					get_all_leaf_stories(story);
				}
			}
		}
		catch(Exception e){
			System.out.println("Caught an exception in get_prefixed_stories method");
			System.out.println("More details "+e);
		}
	}
	public void get_all_leaf_stories(JsonObject story){
		try{
			
			//pre-processing
			BigInteger pOID = story.get("ObjectID").getAsBigInteger();
			boolean unscheduled = false;
			//lookback call
			LookbackQuery query = this.lookback.newSnapshotQuery();
			query.addFindClause("_TypeHierarchy", "HierarchicalRequirement");
			query.addFindClause("_ItemHierarchy", pOID);
			query.addFindClause("Children", null);
			query.addFindClause("__At", "current");
			
			query.requireFields("FormattedID","Iteration","ObjectID","Name");
			query.sortBy("Iteration", -1);
			
			LookbackResult resultSet = query.execute();
			
			int resultCount = resultSet.Results.size();
			Iterator i= resultSet.Results.listIterator();
			 
			 //Iterator iterator = resultSet.getResultsIterator();
			Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.SEVERE);
			System.out.println("Children count "+resultCount);
			 while(i.hasNext()){
				// System.out.println("Next "+i.next());
				 Map m = (Map) i.next();
				 if(m.containsKey("Iteration")){
					 
					 Double iOID = Double.parseDouble(m.get("Iteration").toString());
					 BigInteger iterationOID = new BigDecimal(iOID).toBigInteger();
					 this.iteration_array.add(iterationOID);
					 
				 } 
			 }
			 if(this.iteration_array.size()!=resultCount)
				 unscheduled=true;
			 
			 this.iteration_array.retainAll(all_iter);
			 //this.temporary_array.retainAll(this.iteration_array);
			 if(this.iteration_array.size()>0){
				 System.out.println("Before retainAll, subset is "+this.iteration_array);

				 String latest_iteration = this.get_name_of_iteration(this.get_latest_iteration());
				 if(unscheduled){ latest_iteration+="*";}
				 System.out.println("Latest iteration is "+latest_iteration);
				 System.out.println("Size "+this.iteration_array.size());
				 this.iteration_array.clear();
				// this.update_top_level_story(pOID,story.get("_ref").getAsString(),latest_iteration);
			 }
			 
			
		}
		catch(Exception e){
			System.out.println("Lookback Exception "+e);
		}
	}
	public String get_latest_iteration(){
		for(int i=0;i<all_iter.size();i++){
			if(iteration_array.contains(all_iter.get(i))){
				return all_iter.get(i).toString();
			}
			
	}
		return null;
}
	public BigInteger get_first_iteration(){
		for(int i=all_iter.size()-1;i>0;i--){
			if(iteration_array.contains(all_iter.get(i))){
				return all_iter.get(i);
			}
		}
		return new BigInteger("0");
	}
	public void update_top_level_story(BigInteger parentOID,String ref,String dIteration ) throws IOException{
		//get query ready
		JsonObject update_story = new JsonObject();
		update_story.addProperty("dIteration", dIteration);
		//update_story.addProperty("dPSI", "PSI "+);
		
		//update part of the code (updating dIteration for top-level-story)
		UpdateRequest updateRequest = new UpdateRequest(ref,update_story);
		UpdateResponse updateResponse = this.rest.update(updateRequest);
		if(updateResponse.wasSuccessful()){
			JsonObject obj = updateResponse.getObject();
			System.out.println("Updated dIteration for "+parentOID+" and the updated value is "+obj.get("dIteration"));
		}	
	}
	
	/*
	 * Method Name: get_name_of_iteration(String)
	 * Parameters: iOID - Object ID of the iteration passed
	 * 
	 * Description: Return the name of the iteration with ObjectID iOID.
	 */
	public String get_name_of_iteration(String iOID) throws IOException{
		QueryRequest iteration = new QueryRequest("Iteration");
		iteration.setFetch(new Fetch("Name","ObjectID","StartDate","EndDate"));
		iteration.setQueryFilter(new QueryFilter("ObjectID","=",iOID));
		
		QueryResponse response = this.rest.query(iteration);
		for(JsonElement result: response.getResults()){
			JsonObject iter = result.getAsJsonObject();
			if(response.getTotalResultCount()==1){
				return iter.get("Name").getAsString();
			}
			else{
				return null;
			}
		}
		return null;
	}

	
}
