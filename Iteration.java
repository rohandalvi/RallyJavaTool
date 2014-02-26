import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;


public class Iteration extends connect {
	private ArrayList<BigInteger> all_iterations = new ArrayList<BigInteger>();
	private String latest_iteration;
	
	public void set_latest_iteration(String iteration){
		this.latest_iteration = iteration;
	}
	
	public String get_last_iteration(){
		return this.latest_iteration;
	}
	public ArrayList get_all_iterations() throws Exception {
		
		QueryRequest iterations = new QueryRequest("Iteration");
		iterations.setFetch(new Fetch("Name","StartDate","EndDate","ObjectID"));
		iterations.setOrder("EndDate DESC");
		
		QueryResponse response = this.rest.query(iterations);
		
		if(response.wasSuccessful()){
			
			System.out.println(String.format("Total  count %d", response.getTotalResultCount()));
			
			for(JsonElement result: response.getResults()){
				
				JsonObject iter = result.getAsJsonObject();
				this.all_iterations.add(iter.get("ObjectID").getAsBigInteger());
			}
		}
		return this.all_iterations;
	}
	
	public String get_name_of_iteration(String iOID) throws Exception {
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
	public String get_latest_iteration(List<BigInteger> superSet,List<BigInteger> subSet){
		for(int i=0;i<superSet.size();i++){
			if(subSet.contains(superSet.get(i))){
				return superSet.get(i).toString();
				
			}
		}
		return null;
	}
}
