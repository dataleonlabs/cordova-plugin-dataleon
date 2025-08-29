var exec = require("cordova/exec");

function Dataleon(sessionUrl) {
  if (!sessionUrl) throw new Error("sessionUrl is required");
  this.sessionUrl = sessionUrl;
}

Dataleon.prototype.openSession = function (callback) {
  exec(
    callback,                              // success callback
    function (err) { console.error(err);}, // error callback
    "DataleonCordovaPlugin",               // plugin name
    "openSession",                         // native action
    [this.sessionUrl]                      // parameters
  );
};

Dataleon.prototype.closeSession = function () {
  exec(
    function () { console.log("Session closed successfully"); }, // success callback
    function (err) { console.error("Error closing session:", err); }, // error callback
    "DataleonCordovaPlugin", // plugin name
    "closeSession",           // native action
    []                        // no parameters
  );
};

// C'est ce qui sera exposé comme "Dataleon"
module.exports = Dataleon;
