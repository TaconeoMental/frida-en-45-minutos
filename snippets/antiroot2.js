if(Java.available){
    Java.perform(function(){
        console.log("");

        const rootCheckerClass = Java.use("com.nivel4.RootChecker.rootChecker");
        rootCheckerClass.packages.value = Java.array('java.lang.String', Array(9).fill("/fake/fake"));

        rootCheckerClass.checkSu.implementation = function() {
            console.log("checkSu returned", this.checkSu());
            return false;
        }

        rootCheckerClass.testKeys.implementation = function() {
            console.log("testKeys returned", this.testKeys());
            return false;
        }
    })
}
