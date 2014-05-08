window.initChat = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Disco", "initChat", []);
};

window.advertizeChat = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Disco", "advertizeChat", []);
};

window.discoverChat = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Disco", "discoverChat", []);
};

window.connectChat = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Disco", "connectChat", []);
};

window.sendChatMessage = function(messageString, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Disco", "sendChatMessage", [messageString]);
};
