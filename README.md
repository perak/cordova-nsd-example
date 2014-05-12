Android NSD example
-------------------

Just trying to make [NSD](http://developer.android.com/training/connect-devices-wirelessly/nsd.html) example work under cordova box...

	cordova plugin add https://github.com/perak/cordova-nsd-example.git

Cordova **www/index.html**

	<!DOCTYPE html>
	<html>
	    <head>
	        <meta charset="utf-8" />
	        <title>Hello World</title>
			<script type="text/javascript">
				window.onerror=function(msg, url, linenumber) {
					alert('Error message: ' + msg + '\nURL: ' + url + '\nLine Number: ' + linenumber);
					return true;
				}

			</script>
	        <script type="text/javascript" src="cordova.js"></script>
	        <script type="text/javascript" src="plugins/DiscoPlugin.js"></script>
	        <script type="text/javascript">
				function onLoad() {
					document.addEventListener('deviceready', onDeviceReady, false);
				}

				function onDeviceReady() {
				}

				function showError(s) {
					var logDiv = document.getElementById("logDiv");
					logDiv.innerHTML = "<span style=\"color: red;\">" + s + "</span><br />" + logDiv.innerHTML;
				}

				function showMessage(o) {
					if(o.type == "message") {
						var msgDiv = document.getElementById("msgDiv");
						msgDiv.innerHTML = "<strong>" + o.data + "</strong><br />" + msgDiv.innerHTML;
					}

					if(o.type == "log") {
						var logDiv = document.getElementById("logDiv");
						logDiv.innerHTML = o.data + "<br />" + logDiv.innerHTML;
					}
				}

				function sendMessage() {
					var inputBox = document.getElementById("inputBox");
					window.sendChatMessage(inputBox.value, function(o) { showMessage(o); }, function(e) { showError(e); });
				}
	          </script>

	    </head>
	    <body onload="onLoad()">
	        <div class="app">
	            <h1>Apache Cordova</h1>
	            <div>
	            	<button type="button" onclick="window.initChat(function(o) { showMessage(o); }, function(e) { showError(e); }); return false;">Initialize</button>
	            	<button type="button" onclick="window.advertizeChat(function(o) { showMessage(o); }, function(e) { showError(e); }); return false;">Advertize</button>
	            	<button type="button" onclick="window.discoverChat(function(o) { showMessage(o); }, function(e) { showError(e); }); return false;">Discover</button>
	            	<button type="button" onclick="window.connectChat(function(o) { showMessage(o); }, function(e) { showError(e); }); return false;">Connect</button>
	            </div>
	            <div>
	            	<input type="text" id="inputBox"></input>
	            	<button type="button" onclick="sendMessage(); return false;">Send</button>
	            </div>
	            <div id="msgDiv">
	            </div>
	            <div id="logDiv">
	            </div>
	        </div>
	    </body>
	</html>

Add internet permission to your `platforms/android/AndroidManifest.xml`:

	<uses-permission android:name="android.permission.INTERNET" />

That's it. :)