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
					alert(s);
				}

				function showMessage(s) {
					var msgDiv = document.getElementById("msgDiv");
					msgDiv.innerHTML = msgDiv.innerHTML + s + "<br />";
				}

				function sendMessage() {
					var inputBox = document.getElementById("inputBox");
					window.sendChatMessage(inputBox.value, function(s) { showMessage(s); }, function(e) { showError(e); });
				}
	          </script>

	    </head>
	    <body onload="onLoad()">
	        <div class="app">
	            <h1>Apache Cordova</h1>
	            <div>
	            	<button type="button" onclick="window.initChat(function(s) { showMessage(s); }, function(e) { showError(e); }); return false;">Initialize</button>
	            	<button type="button" onclick="window.advertizeChat(function(s) { showMessage(s); }, function(e) { showError(e); }); return false;">Advertize</button>
	            	<button type="button" onclick="window.discoverChat(function(s) { showMessage(s); }, function(e) { showError(e); }); return false;">Discover</button>
	            </div>
	            <div>
	            	<input type="text" id="inputBox"></input>
	            	<button type="button" onclick="sendMessage(); return false;">Send</button>
	            </div>
	            <div id="msgDiv">
	            </div>
	        </div>
	    </body>
	</html>

