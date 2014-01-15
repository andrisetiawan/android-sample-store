package id.co.veritrans.androidsamplestore;


import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

	public final static String EXTRA_MESSAGE = "Ini extra message.";
	public final static String CLIENT_KEY = "94b45959-5466-490c-90f3-503042162f80";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void sendMessage(View view) {
		//TODO: asynchronous http request
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
	    Intent intent = new Intent(this, DisplayMessageActivity.class);
	    
	    EditText ccNumber = (EditText) findViewById(R.id.editTextCCNumber);
	    EditText expiredMonth = (EditText) findViewById(R.id.editTextCCMonth);
	    EditText expiredYear = (EditText) findViewById(R.id.editTextCCYear);
	    EditText cvv = (EditText) findViewById(R.id.editTextCVV);
	    
	    Card card = new Card();
	    card.number = ccNumber.getText().toString();
	    card.expiredMonth = expiredMonth.getText().toString();
	    card.expiredYear = expiredYear.getText().toString();
	    card.cvv = cvv.getText().toString();
	    
	    // Get Token
	    String tokenId;
	    JSONObject json = getToken(card);
	    String response;
	    
		try {
			String code = json.getString("code");
			String status = json.getString("status");
			String m = json.getString("message");
			  
			if (code.equals("VD00")) {
				JSONObject x = json.getJSONObject("data");
				tokenId = x.getString("token_id");
				
				// Send Data to Merchant
			    response = sendDataToMerchantServer(getSampleOrderDetailData(tokenId));
			} else {
				// TODO: error handling here
				response = m;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			response = "Error while parsing JSON response.";
		} catch (Exception e) {
			e.printStackTrace();
			response = e.toString();
		}
	    
	    intent.putExtra(EXTRA_MESSAGE, response);
	    startActivity(intent);
	}
	
	private String sendDataToMerchantServer(String jsondata) {
		String responseMessage = new String();
		
		HttpClient httpClient = new DefaultHttpClient();
		try {
	    	
	    	HttpPost request = new HttpPost("http://10.0.2.2/sampleserver/charge_test.php");
	        
	        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
	        pairs.add(new BasicNameValuePair("json_body", jsondata));
	        request.setEntity(new UrlEncodedFormEntity(pairs));
	        
	        HttpResponse response2 = httpClient.execute(request);
	        
	        //Convert Inputstream to String
			StringWriter writer = new StringWriter();
			IOUtils.copy(response2.getEntity().getContent(), writer, "UTF-8");
			String bodyContent = writer.toString();
	        
			responseMessage = bodyContent;
	        // handle response here...
	        
	    }catch (Exception ex) {
	        // handle exception here
	    	responseMessage = ex.toString();
	    } finally {
	        httpClient.getConnectionManager().shutdown();
	    }
		
		return responseMessage;
	}

	private JSONObject getToken(Card card) {
		StringBuilder builder = new StringBuilder(); 
		builder.append("https://payments.veritrans.co.id/vtdirect/v1/tokens?"); 
		builder.append("card_number=").append(card.number).append("&"); 
		builder.append("card_exp_month=").append(card.expiredMonth).append("&"); 
		builder.append("card_exp_year=").append(card.expiredYear).append("&"); 
		builder.append("card_cvv=").append(card.cvv).append("&"); 
		builder.append("client_key=").append(CLIENT_KEY);
		
		HttpGet get = new HttpGet(builder.toString()); 
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response;
		
		//String tokenId = null;

		try {
			response = httpClient.execute(get);
			
			//Convert Inputstream to String
			StringWriter writer = new StringWriter();
			IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
			String bodyContent = writer.toString();
			 
			JSONObject json = new JSONObject(bodyContent);
			return json;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	private String getSampleOrderDetailData(String tokenId) {
		JSONObject obj = new JSONObject();
		String jsonData = null;
	
		try {
			obj.put("token_id", tokenId);
	
			// Sample order details data
			Random rand = new Random();
		    int randomNum = rand.nextInt(10000) + 1;
		    String orderId = "order".concat(Integer.toString(randomNum));
			
			obj.put("order_id", orderId);
			JSONArray order_items = new JSONArray();
			JSONObject item1 = new JSONObject();
			item1.put("id", 123);
			item1.put("price", 1500000);
			item1.put("qty", 1);
			item1.put("name", "Sepatu Adidas F50");
	
			/*
			JSONObject item2 = new JSONObject();
			item2.put("id", 124);
			item2.put("price", 20000);
			item2.put("qty", 1);
			item2.put("name", "Mie Sapi");
			*/
	
			order_items.put(item1);
			//order_items.put(item2);
			obj.put("order_items", order_items);
	
			obj.put("gross_amount", 1500000);
			obj.put("email", "vt-testing@veritrans.co.id");
	
			JSONObject shipping_address = new JSONObject();
			shipping_address.put("first_name", "Sam");
			shipping_address.put("last_name", "Anthony");
			shipping_address.put("address1", "Buaran I");
			shipping_address.put("address2", "Pulogadung");
			shipping_address.put("city", "Jakarta");
			shipping_address.put("postal_code", "16954");
			shipping_address.put("phone", "0123456789123");
			
			obj.put("shipping_address", shipping_address);
			
			JSONObject billing_address = new JSONObject();
			billing_address.put("first_name", "Sam");
			billing_address.put("last_name", "Anthony");
			billing_address.put("address1", "Buaran I");
			billing_address.put("address2", "Pulogadung");
			billing_address.put("city", "Jakarta");
			billing_address.put("postal_code", "16954");
			billing_address.put("phone", "0123456789123");
			
			obj.put("billing_address", billing_address);
			jsonData = obj.toString();
	
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		return jsonData;
	}
	
}
