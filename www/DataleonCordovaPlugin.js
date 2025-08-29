var exec = require("cordova/exec");

export class DataleonCordovaPlugin {
  constructor(sessionUrl) {
    if (!sessionUrl) throw new Error("sessionUrl is required");
    this.sessionUrl = sessionUrl;
  }

  /**
   * Open the native session
   * @param {Function} callback - receives "FINISHED", "CANCELED" or custom payload
   */
  openSession(callback) {
    exec(
      callback,                   // success callback
      (err) => console.error(err), // error callback
      "DataleonCordovaPlugin",     // plugin name
      "openSession",               // native action
      [this.sessionUrl]            // parameters
    );
  }
}
