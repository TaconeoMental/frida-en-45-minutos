if(Java.available){
    Java.perform(function(){
        console.log("");

        const rootCheckerClass = Java.use("com.nivel4.RootChecker.rootChecker");

        rootCheckerClass.checkSu.implementation = function() {
            console.log("checkSu returned", this.checkSu());
            return false;
        }

        rootCheckerClass.testKeys.implementation = function() {
            console.log("testKeys returned", this.testKeys());
            return false;
        }

        rootCheckerClass.checkPackages.implementation = function() {
            console.log("checkPackages returned", this.checkPackages());
            return false;
        }
    })
}
