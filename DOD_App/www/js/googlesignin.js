var googleapi = {
        authorize: function(options) {
            var deferred = $.Deferred();

            //Build the OAuth consent page URL
            var authUrl = 'https://accounts.google.com/o/oauth2/auth?' + $.param({
                client_id: options.client_id,
                redirect_uri: options.redirect_uri,
                response_type: 'code',
                scope: options.scope

            });

            //Open the OAuth consent page in the InAppBrowser
            var authWindow = window.open(authUrl, '_blank', 'location=no,toolbar=no');

            //The recommendation is to use the redirect_uri "urn:ietf:wg:oauth:2.0:oob"
            //which sets the authorization code in the browser's title. However, we can't
            //access the title of the InAppBrowser.
            //
            //Instead, we pass a bogus redirect_uri of "http://localhost", which means the
            //authorization code will get set in the url. We can access the url in the
            //loadstart and loadstop events. So if we bind the loadstart event, we can
            //find the authorization code and close the InAppBrowser after the user
            //has granted us access to their data.
            $(authWindow).on('loadstart', function(e) {
                var url = e.originalEvent.url;
                var code = /\?code=(.+)$/.exec(url);
                var error = /\?error=(.+)$/.exec(url);

                if (code || error) {
                    //Always close the browser when match is found
                    authWindow.close();
                }

                if (code) {
                    //Exchange the authorization code for an access token
                    $.post('https://accounts.google.com/o/oauth2/token', {
                      code: code[1],
                      client_id: options.client_id,
                      client_secret: options.client_secret,
                      redirect_uri: options.redirect_uri,
                      grant_type: 'authorization_code'
                    }).done(function(data) {
                      deferred.resolve(data);
                      $("#loginStatus").html('Name: ' + data.given_name);
                    }).fail(function(response) {
                        deferred.reject(response.responseJSON);
                    });
                } else if (error) {
                    //The user denied access to the app
                    deferred.reject({
                        error: error[1]
                    });
                }
            });

            return deferred.promise();
        }
    };

    var accessToken;
    var UserData=null;

    function callGoogle()
    {
        //alert('starting');
        googleapi.authorize({
                            client_id: '626623813012-4ph22o7ao4pe53tfm9i0bnqd5c22n5tb.apps.googleusercontent.com',
                            client_secret: 'I7HVo5Jp2r_zoX9u-1etbeKb',
                            redirect_uri: 'http://localhost',
                            scope: 'https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/userinfo.email'
                            }).done(function(data) {
                                    accessToken=data.access_token;
                                    console.log(data.access_token);
                                    console.log(JSON.stringify(data));
                                    getUserInfo();
                            }).fail(function(response) {
                                //error
                            });
        //getUserInfo();
        return $.Deferred().resolve();

    }

    function getUserInfo() {
        $.ajax({
            url: 'https://www.googleapis.com/oauth2/v1/userinfo?access_token=' + accessToken,
            data: null,
            success: function(resp) {
              user    =   resp;
              console.log("User information "+JSON.stringify(user));
              console.log("Set user email "+user.email);
              var email = user.email;
              var index = -1;
              if(email){
                 var emailProvider =  email.split("@")[1];
                 index = emailProvider.localeCompare("wso2.com");
              }                           
              if(index == 0){
                localStorage.setItem("email",user.email);
                localStorage.setItem("isLogged",true);
                localStorage.setItem("userName",user.name);     
              }else if(email){
                 $("#txt-email").val("please use your WSO2 account"); 
                 if(localStorage.getItem("email")){
                     localStorage.removeItem("email");
                     localStorage.removeItem("isLogged");
                     localStorage.removeItem("userName");
                 }                 
              }else{
                $("#txt-email").val("you need to sign in first"); 
              }
              userDetailChek();
                      
            },
            dataType: "jsonp",
            error:function(error){
              
            }
      });
      
      disconnectUser();    

    }
    // This function gets data of user.
    function disconnectUser() {
      var revokeUrl = 'https://accounts.google.com/o/oauth2/revoke?token='+accessToken;

      // Perform an asynchronous GET request.
      $.ajax({
        type: 'GET',
        url: revokeUrl,
        async: false,
        contentType: "application/json",
        dataType: 'jsonp',
        success: function(nullResponse) {
          // Do something now that user is disconnected
          // The response is always undefined.
          accessToken=null;
          console.log(JSON.stringify(nullResponse));
          console.log("-----signed out..!!----"+accessToken);
        },
        error: function(e) {
          // Handle the error
          // console.log(e);
          // You could point users to manually disconnect if unsuccessful
          // https://plus.google.com/apps
        }
      });
    }
    var email;
    var isLogged;
    var userName;
    function userDetailChek(){
        email  = localStorage.getItem("email");
        isLogged =  localStorage.getItem("isLogged");
        userName =  localStorage.getItem("userName");
        //alert(email+"@"+isLogged+"@"+userName);
        if(email || isLogged || userName){
          //$('#btn-submit').attr('disabled','disabled');
          $('#btn-submit').show();
          $('input[type="submit"]').removeAttr('disabled');
          $("#txt-email").val(email);                   
        }else{
            $('#btn-submit').hide();
            alert("you need to sign in first");
        }
  }