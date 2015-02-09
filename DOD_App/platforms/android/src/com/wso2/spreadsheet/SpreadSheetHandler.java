package com.wso2.spreadsheet;

import android.app.ActionBar.Tab;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.gdata.client.ClientLoginAccountType;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by ushan on 1/14/15.
 */
public class SpreadSheetHandler {

	private String username = "";
	private String password = "";
	private SpreadsheetService spreadsheetService;
	final String myTag = "Dinner Demand";

	public SpreadSheetHandler() throws AuthenticationException {
		this.username = "sachithu@wso2.com";
		this.password = "19920129Sa!";
		spreadsheetService = new SpreadsheetService("Spread Sheet version");
		spreadsheetService.setProtocolVersion(SpreadsheetService.Versions.V3);
		Log.i(myTag, "Username : " + username + "Password :" + password);
		spreadsheetService.setUserCredentials(this.username, this.password);

	}
	
	public JSONObject getdetails(String email) throws JSONException {
		String SPREADSHEET_URL = "https://spreadsheets.google.com/feeds/spreadsheets/1-nsFcc3LUdeu5sMpwEpz3FRgoLtB-kXlj3-cjqjG_dc";
		JSONObject vendors = new JSONObject();
		try {
			URL metafeedUrl = new URL(SPREADSHEET_URL);
			com.google.gdata.data.spreadsheet.SpreadsheetEntry spreadsheet = spreadsheetService
					.getEntry(
							metafeedUrl,
							com.google.gdata.data.spreadsheet.SpreadsheetEntry.class);
			URL listFeedUrl = ((WorksheetEntry) spreadsheet.getWorksheets()
					.get(0)).getListFeedUrl();
			ListFeed listFeed = (ListFeed) spreadsheetService.getFeed(
					listFeedUrl, ListFeed.class);
			for (ListEntry entry : listFeed.getEntries()) {
				String entryEmail = entry.getCustomElements().getValue(
						"emailaddress");
				if (email.equals(entryEmail)) {
					String id = entry.getCustomElements().getValue("id");
					String vendor = entry.getCustomElements().getValue(
							"restaurant");
					String time = entry.getCustomElements().getValue(
							"timestamp");
					Log.i(myTag, "timestamp " + time);
					String itemName = entry.getCustomElements().getValue(
							"itemname");
					Log.i(myTag, "Item name " + itemName);
					String qty = entry.getCustomElements().getValue("quantity");
					Log.i(myTag, "Quantity " + qty);
					String array[] = time.split(" ");
					String withoutSeconds = array[1];
					String status = entry.getCustomElements().getValue("status");
					
					JSONArray jsonArray = new JSONArray();
					jsonArray.put(itemName);
					jsonArray.put(qty);
					jsonArray.put(withoutSeconds);
					jsonArray.put(status);
					
					if (vendors.has(id)) {
						JSONObject vendor_details = vendors.getJSONObject(id);
						if (vendor_details.has(vendor)) {
							JSONObject items = vendor_details
									.getJSONObject(vendor);							
							items.put(time,jsonArray);
						}else{
							JSONObject items = new JSONObject();
							items.put(time,jsonArray);
							vendor_details.put(vendor,items);
						}
					} else {
						JSONObject items = new JSONObject();
						items.put(time,jsonArray);
						JSONObject vendor_details = new JSONObject();
						vendor_details.put(vendor, items);
						vendors.put(id, vendor_details);
					}
				}
				
				

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return vendors;
	}

	// to get list for feed
	private ListFeed getListFeed(String spreadSheetURL) throws IOException,
			ServiceException {
		URL metafeedUrl = new URL(spreadSheetURL);
		com.google.gdata.data.spreadsheet.SpreadsheetEntry spreadsheet = spreadsheetService
				.getEntry(
						metafeedUrl,
						com.google.gdata.data.spreadsheet.SpreadsheetEntry.class);
		Log.i(myTag,
				"Title : "
						+ ((WorksheetEntry) spreadsheet.getWorksheets().get(0))
								.getTitle());
		URL listFeedUrl = ((WorksheetEntry) spreadsheet.getWorksheets().get(0))
				.getListFeedUrl();
		ListFeed listFeed = (ListFeed) spreadsheetService.getFeed(listFeedUrl,
				ListFeed.class);

		return listFeed;
	}

	// feed url for insert data
	private URL getListFeedUrl(String spreadSheetUrl) throws IOException,
			ServiceException {
		URL metafeedUrl = new URL(spreadSheetUrl);
		com.google.gdata.data.spreadsheet.SpreadsheetEntry spreadsheet = spreadsheetService
				.getEntry(
						metafeedUrl,
						com.google.gdata.data.spreadsheet.SpreadsheetEntry.class);
		URL listFeedUrl = ((WorksheetEntry) spreadsheet.getWorksheets().get(0))
				.getListFeedUrl();
		return listFeedUrl;
	}

	// add new data row to spreasheet
	public void addRow(String[] details, JSONObject jsonObject,
			String spreadSheetUrl) throws IOException, ServiceException,
			JSONException {
		Log.i(myTag, "Adding new data");

		URL listFeedUrl = getListFeedUrl(spreadSheetUrl);
		ListEntry row = new ListEntry();
		String date = (DateFormat.format("dd/MM/yyyy hh:mm:ss",
				new java.util.Date()).toString());
		row.getCustomElements().setValueLocal("id", jsonObject.getString("ID"));
		row.getCustomElements().setValueLocal("timestamp", date);
		row.getCustomElements().setValueLocal("emailaddress",
				jsonObject.getString("username"));
		row.getCustomElements().setValueLocal("itemname", details[0]);
		row.getCustomElements().setValueLocal("quantity", details[1]);
		row.getCustomElements().setValueLocal("mobilenumber",
				jsonObject.getString("mobilenumber"));
		row.getCustomElements().setValueLocal("office",
				jsonObject.getString("office"));
		row.getCustomElements().setValueLocal("team",
				jsonObject.getString("team"));
		row.getCustomElements().setValueLocal("restaurant", details[2]);
		row.getCustomElements().setValueLocal("status","Pending");
		row = spreadsheetService.insert(listFeedUrl, row);
	}

	// update order quantity
	public boolean updateQuantity(String mobileNumber, String newQuantity,
			String itemName, String spreadSheetUrl) throws IOException,
			ServiceException {
		ListFeed listFeed = getListFeed(spreadSheetUrl);
		for (ListEntry entry : listFeed.getEntries()) {
			if (entry.getCustomElements().getValue("mobilenumber")
					.equals(mobileNumber)
					&& entry.getCustomElements().getValue("itemname")
							.equals(itemName)) {
				Log.i(myTag, "Updating quantity");
				entry.getCustomElements()
						.setValueLocal("quantity", newQuantity);
				entry.update();
				return true;
			}
		}
		return false;
	}

	public JSONArray getResturantNames() throws IOException, ServiceException {
		String spreadSheetUrl = "https://spreadsheets.google.com/feeds/spreadsheets/1-q7SRlSBVlwhYndw6mD78DrH9lm8gsUkxcgAzyGHWng";
		ListFeed listFeed = getListFeed(spreadSheetUrl);
		ListEntry entry = listFeed.getEntries().get(0);
		Set<String> columnHeadings = entry.getCustomElements().getTags();
		Log.i(myTag, "Size :" + columnHeadings.size());
		JSONArray jsonArray = new JSONArray();
		for (String string : columnHeadings) {
			jsonArray.put(string);
		}
		return jsonArray;
		// return columnHeadings.toArray(new String[columnHeadings.size()]);
		// return null;
	}

	public JSONArray getItems(String resturant) throws IOException,
			ServiceException {
		String spreadSheetUrl = "https://spreadsheets.google.com/feeds/spreadsheets/1-q7SRlSBVlwhYndw6mD78DrH9lm8gsUkxcgAzyGHWng";
		ListFeed listFeed = getListFeed(spreadSheetUrl);
		Set<String> menuItem = new HashSet<String>();
		for (ListEntry entry : listFeed.getEntries()) {
			Set<String> columnHeadings = entry.getCustomElements().getTags();
			for (String tag : columnHeadings) {
				if (tag.equals(resturant)) {
					menuItem.add(entry.getCustomElements().getValue(tag));
				}
			}
		}
		JSONArray jsonArray = new JSONArray();
		for (String string : menuItem) {
			if (string != null) {
				jsonArray.put(string);
			}

		}
		return jsonArray;
	}
	
	public JSONArray getTeams() throws IOException, ServiceException{
		String spreadSheetUrl = "https://spreadsheets.google.com/feeds/spreadsheets/1ILbynmHNCdVu3gCVMS08woRhqsbI8jFuqEOnmcID83M";
		ListFeed listFeed = getListFeed(spreadSheetUrl);
		Set<String> menuItem = new HashSet<String>();
		for (ListEntry entry : listFeed.getEntries()) {
			menuItem.add(entry.getCustomElements().getValue("team"));
		}
		JSONArray jsonArray = new JSONArray();
		for (String string : menuItem) {
			if (string != null) {
				jsonArray.put(string);
			}

		}
		return jsonArray;
	}
}
