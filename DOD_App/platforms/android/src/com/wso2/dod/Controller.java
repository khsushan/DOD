package com.wso2.dod;

import java.io.IOException;
import java.util.Iterator;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.wso2.spreadsheet.SpreadSheetHandler;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Controller extends CordovaPlugin {
	final String myTag = "Dinner Demand";
	private SpreadSheetHandler spreadSheetHandler;
	private String orderSpreadSheetUrl = "https://spreadsheets.google.com/feeds/spreadsheets/1-nsFcc3LUdeu5sMpwEpz3FRgoLtB-kXlj3-cjqjG_dc";

	public boolean execute(final String action, final JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		PluginResult.Status status = PluginResult.Status.OK;
		TelephonyManager telephonyManager = (TelephonyManager) this.cordova
				.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		final String mobileNumber = telephonyManager.getLine1Number();
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
							boolean isLogged = spreadSheetHandler.login(args
									.getString(0), args.getString(1));
							Log.i(myTag, "Is Logged :" + isLogged);
							callbackContext.success(isLogged + "");
						} catch (AuthenticationException e) {
							Log.i(myTag, e.getMessage());
							callbackContext.error("Exception :"
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
							JSONArray jsonArray = spreadSheetHandler
									.getItems(args.getString(2));
							callbackContext.success(jsonArray);
						} catch (AuthenticationException e) {
							callbackContext.error(e.getMessage());
							// e.printStackTrace();
						} catch (IOException e) {
							callbackContext.error(e.getMessage());
							// e.printStackTrace();
						} catch (ServiceException e) {
							callbackContext.error(e.getMessage());
							// e.printStackTrace();
						}
					} else if (action.equals("PlaceOrder")) {

						JSONObject userDetails = args.getJSONObject(0);
						userDetails.put("mobilenumber", mobileNumber);
						try {
							spreadSheetHandler = new SpreadSheetHandler(
									userDetails.getString("username"),
									userDetails.getString("password"));
						} catch (AuthenticationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						JSONObject orderDetail = args.getJSONObject(1);
						Iterator<?> keys = orderDetail.keys();
						while (keys.hasNext()) {
							String key = (String) keys.next();
							System.out.println("");
							Log.i(myTag, "Resturant Name :" + key);
							if (orderDetail.get(key) instanceof JSONObject) {
								JSONObject items = (JSONObject) orderDetail
										.get(key);
								Iterator<?> orderKeys = items.keys();
								while (orderKeys.hasNext()) {
									String itemName = (String) orderKeys.next();
									Log.i(myTag,
											"Resturant Name :" + key
													+ " Item name : "
													+ itemName + " Quantity : "
													+ items.getString(itemName)
													+ "Mobile number "
													+ mobileNumber);
									String details[] = { itemName,
											items.getString(itemName), key };
									try {
										spreadSheetHandler.addRow(details,
												userDetails,
												orderSpreadSheetUrl);
										System.out.println();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (ServiceException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									System.out.println("");
								}
							}
						}
						// spreadSheetHandler.addRow(details, spreadSheetUrl)
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
