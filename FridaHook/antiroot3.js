if(Java.available){
    Java.perform(function(){
        console.log("");

        const main_activity = Java.use("com.nivel4.fridaen45minutos.MainActivity");
        main_activity.isRootedDevice.implementation = function() {
            console.log("isRootedDevice returned", this.isRootedDevice());
            return false;
        }
    })
}
