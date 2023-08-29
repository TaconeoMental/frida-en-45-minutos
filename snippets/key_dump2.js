// https://stackoverflow.com/questions/40031688/javascript-arraybuffer-to-hex
function buf2hex(buffer) {
      return [...new Uint8Array(buffer)]
          .map(x => x.toString(16).padStart(2, '0'))
          .join('');
}

if (Java.available) {
    Java.perform(function() {
        console.log("");

        const main_activity = Java.use("com.nivel4.fridaen45minutos.MainActivity")
        main_activity.isRootedDevice.implementation = function() {
            console.log("isRootedDevice returned", this.isRootedDevice());
            return false;
        }

        var secret_key_spec = Java.use("javax.crypto.spec.SecretKeySpec");
        secret_key_spec.$init.overload("[B", "java.lang.String").implementation = function (key, algo) {
            console.log("new KeySpec:", buf2hex(new Uint8Array(key)));
            return this.$init(key, algo);
        }
    })
}
