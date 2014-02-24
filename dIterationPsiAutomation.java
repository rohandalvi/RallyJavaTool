import com.google.gson.JsonElement;
import com.rallydev.lookback.LookbackApi;
import com.rallydev.lookback.LookbackQuery;
import com.rallydev.lookback.LookbackResult;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.io.*;
import java.util.HashSet;
import java.util.ArrayList;
import java.net.URI;

import javax.management.Query;
import com.google.gson.JsonObject;
public class dIterationPsiAutomation {
	public String rallyURI;
	public String appName;
	public String username;
	public String password;
	public RallyRestApi restApi;
	public LookbackApi lookback;
	public static HashSet<BigInteger> all_iter = new HashSet<BigInteger>();
	public HashSet<BigInteger> iteration_array = new HashSet<BigInteger>();
	public dIterationPsiAutomation(String rallyURL,String username,String password, String applicationName) throws IOException {
		// TODO Auto-generated constructor stub
		this.rallyURI = rallyURL;
		this.appName = applicationName;
		this.username = username;
		this.password = password;
		
		
		this.connect(this.rallyURI, this.appName, this.username, this.password);
		
		get_all_iterations();
	}
	public void connect(String uri,String app,String username,String password){
		try //Unhandled URI Exception occurs here
		{
			
			this.restApi = new RallyRestApi(new URI(uri),username,password);
			this.restApi.setApplicationName("v2.0");
			this.restApi.setApplicationName(app);
			this.lookback = new LookbackApi();
			this.lookback.setCredentials(username, password);
			this.lookback.setWorkspace("2154600806");
			
		}
		catch(Exception e){
			System.out.println("Exception occured "+e);
		}
	}
	public void getFeatureInfo(String featureName) throws IOException {
		QueryRequest feature = new QueryRequest("portfolioitem/feature");
		feature.setFetch(new Fetch("FormattedID","ObjectID","Name"));
		feature.setQueryFilter(new QueryFilter("Name","=",featureName));
		
		QueryResponse queryResponse = this.restApi.query(feature);
		
		
	}
	public  void get_all_iterations() throws IOException {
		
		QueryRequest iterations = new QueryRequest("Iteration");
		iterations.setFetch(new Fetch("Name","StartDate","EndDate","ObjectID"));
		iterations.setOrder("EndDate DESC");
		
		QueryResponse response = this.restApi.query(iterations);
		if(response.wasSuccessful()){
			System.out.println(String.format("Total  count %d", response.getTotalResultCount()));
			for(JsonElement result: response.getResults()){
				
				JsonObject iter = result.getAsJsonObject();
				all_iter.add(iter.get("ObjectID").getAsBigInteger());
			}
			System.out.println("All iter length "+all_iter);
		}
		
	}
	
	public void get_formattedID(JsonElement ObjectID){
		
	}
	public void get_prefixed_stories(String prefix){
		try{
			QueryRequest stories = new QueryRequest("hierarchicalrequirement");
			stories.setFetch(new Fetch("Children","Name","Iteration","FormattedID","ObjectID"));
			stories.setQueryFilter(new QueryFilter("Name","contains",prefix));
			
			
			QueryResponse response = this.restApi.query(stories);
			if(response.wasSuccessful()){
				System.out.println(String.format("Total result count %d", response.getTotalResultCount()));
				for(JsonElement result: response.getResults()){
					JsonObject story = result.getAsJsonObject();	
					System.out.println(String.format("%s - %s: ObjectID: %s", story.get("FormattedID").getAsString(), story.get("Name").getAsString(), story.get("ObjectID").getAsBigInteger()));
					get_all_leaf_stories(story.get("ObjectID").getAsBigInteger());
				}
			}
		}
		catch(Exception e){
			System.out.println("Caught an exception in get_prefixed_stories method");
			System.out.println("More details "+e);
		}
	}
	public void get_all_leaf_stories(BigInteger oID){
		try{
			LookbackQuery query = this.lookback.newSnapshotQuery();
			query.addFindClause("_TypeHierarchy", "HierarchicalRequirement");
			query.addFindClause("_ItemHierarchy", oID);
			query.addFindClause("Children", null);
			query.addFindClause("__At", "current");
			
			query.requireFields("Iteration","StartDate","EndDate","ObjectID","Name");
			query.sortBy("Iteration", -1);
			
			LookbackResult resultSet = query.execute();
			
			int resultCount = resultSet.Results.size();
			Iterator i= resultSet.Results.listIterator();
			 
			 //Iterator iterator = resultSet.getResultsIterator();
			System.out.println("Children count "+resultCount);
			 while(i.hasNext()){
				 System.out.println("Next "+i.next());
				 Map m = (Map) i.next();
				 Double d = Double.parseDouble(m.get("ObjectID").toString());
				 System.out.println("Double "+d);
				 BigInteger big = new BigDecimal(d).toBigInteger();
				 System.out.println("BigInteger "+big);
				 this.iteration_array.add(big);
				 
			 }
//			 if(all_iter.size()!=0 && this.iteration_array.size()!=0){
//				 iteration_array.retainAll(all_iter);
//			 }
			 System.out.println("Size "+iteration_array.size());

			
		}
		catch(Exception e){
			System.out.println("Lookback Exception "+e);
		}
	}
}
