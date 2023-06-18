if (Java.available){
    Java.perform(function(){

        console.log();

        var rootCheckerClass = Java.use("com.nivel4.RootChecker.rootChecker");
        rootCheckerClass.checkSu.implementation = function(){
            console.log("com.nivel4.RootChecker.rootChecker.checkSu() returned:", this.checkSu());
            return false;
        }

        rootCheckerClass.checkPackages.implementation = function(){
            console.log("com.nivel4.RootChecker.rootChecker.checkPackages() returned:", this.checkPackages());
            return false;
        }

        rootCheckerClass.testKeys.implementation = function(){
            console.log("com.nivel4.RootChecker.rootChecker.testKeys() returned:", this.testKeys());
            return false;
        }

        var EncryptDecryptClass = Java.use("com.nivel4.Cipher.EncryptDecrypt");
        EncryptDecryptClass.encrypt.implementation = function(String, Secretkey){
            console.log("[+] Plaintext:", String);
            console.log("[+] Secretkey:", Secretkey);
            if(String.includes("user")){
                String = "admin";
            }
            return this.encrypt(String, Secretkey);
        }

    })
}