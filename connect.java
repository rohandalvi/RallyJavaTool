import java.net.URI;

import com.rallydev.lookback.LookbackApi;
import com.rallydev.rest.RallyRestApi;


public class connect {
	public String url,user,pass,app;
	RallyRestApi rest;
	LookbackApi lookback;
	connect(){
		
	}
	connect(String rallyURL,String username, String password, String applicationName)throws Exception {
		System.out.println("Super called");
		this.url = rallyURL;
		this.user = username;
		this.pass = password;
		this.app = applicationName;
		
		this.rest = new RallyRestApi(new URI(this.url),this.user,this.pass);
		this.lookback = new LookbackApi();
		this.lookback.setCredentials(username, password);
		this.lookback.setWorkspace("2154600806");
	}
	
}
