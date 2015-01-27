package com.wso2.dod;

import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.wso2.spreadsheet.SpreadSheetHandler;

import android.util.Log;

public class Controller extends CordovaPlugin {
	final String myTag = "Dinner Demand";
	private SpreadSheetHandler spreadSheetHandler;

	public boolean execute(final String action, final JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		PluginResult.Status status = PluginResult.Status.OK;
		cordova.getActivity().runOnUiThread(new Runnable() {
			String displayText = "";

			@Override
			public void run() {
				try {
					if (action.equals("Login")) {
						displayText = "UserName " + args.getString(0)
								+ " Password" + args.getString(1);
						Log.i(myTag, displayText);
						try {
							spreadSheetHandler = new SpreadSheetHandler(args
									.getString(0), args.getString(1));
							boolean isLogged = spreadSheetHandler.login();
							Log.i(myTag, "Is Logged :" + isLogged);
							callbackContext.success(isLogged + "");
						} catch (AuthenticationException e) {
							Log.i(myTag, e.getMessage());
							callbackContext.success("Exception :"
									+ e.getMessage());
						} catch (IOException e) {
							Log.i(myTag, "Exception :" + e.getMessage());
							callbackContext.success(e.getMessage());
						}

					} else if (action.equals("LoadVendors")) {
						try {
							spreadSheetHandler = new SpreadSheetHandler(args
									.getString(0), args.getString(1));
							JSONArray vendors = spreadSheetHandler
									.getResturantNames();
							Log.i(myTag, "Vendor array is : " + vendors);
							callbackContext.success(vendors);
						} catch (AuthenticationException e) {
							callbackContext.error(e.getMessage());
							// TODO Auto-generated catch block
							// e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ServiceException e) {
							callbackContext.error(e.getMessage());
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					} else if (action.equals("LoadMenu")) {
						try {
							spreadSheetHandler = new SpreadSheetHandler(args
									.getString(0), args.getString(1));
							JSONArray jsonArray=spreadSheetHandler.getItems(args.getString(2));
							callbackContext.success(jsonArray);
						} catch (AuthenticationException e) {
							callbackContext.error(e.getMessage());
							//e.printStackTrace();
						} catch (IOException e) {
							callbackContext.error(e.getMessage());
							//e.printStackTrace();
						} catch (ServiceException e) {
							callbackContext.error(e.getMessage());
							//e.printStackTrace();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});

		return true;
	}

	private String convertStringArrayInto(String[] vendors) {
		String returnValue = "[";
		for (int i = 0; i < vendors.length; i++) {
			if ((vendors.length - 1) == i) {
				returnValue += vendors[i] + "]";
			} else {
				returnValue += vendors[i] + ",";
			}
		}
		return returnValue;
	}
}
