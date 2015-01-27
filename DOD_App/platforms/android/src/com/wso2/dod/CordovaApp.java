/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.wso2.dod;

import java.io.IOException;

import android.os.Bundle;
import android.os.StrictMode;

import org.apache.cordova.*;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.wso2.spreadsheet.SpreadSheetHandler;

public class CordovaApp extends CordovaActivity {
	private SpreadSheetHandler spreadSheetHandler;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.init();
		/*
		 * Thread t = new Thread(new Runnable() {
		 * 
		 * @Override public void run() { try { spreadSheetHandler = new
		 * SpreadSheetHandler("sachithu@wso2.com", "19920129Sayuriushan!");
		 * /*try { spreadSheetHandler.getdetails();
		 * //spreadSheetHandler.addRow(new
		 * String[]{"noyely@wso2.com","Sandwich","14","0981818188"}); } catch
		 * (IOException e) { e.printStackTrace(); }
		 * spreadSheetHandler.getdetails();
		 * 
		 * //spreadSheetHandler.getdetails(); } catch (AuthenticationException
		 * e) { e.printStackTrace(); }catch (ServiceException e) {
		 * e.printStackTrace(); } } }); t.start();
		 */
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		/*try {
			spreadSheetHandler =  new SpreadSheetHandler("sachithu@wso2.com","19920129Sayuriushan!");
			spreadSheetHandler.getResturantNames();
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		loadUrl(launchUrl);
	}

}
