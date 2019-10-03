package com.viki3d.firebase.sender;

public class Main {

	public static void main(String[] args) {
		
		//  Test run
		args = new String[] {
				"-serverKey", "./pushcontrol-f9e17-firebase-adminsdk-bmx3w-f031f3d5d4.json"
				,"-token", "dpl2oKyV_Vs:APA91bEo-Oz2Q9hLxGv8rqojwGvgwkq2Jt1Lthaa1_LoIb4yHDlaQoS5pl3HfZ4ftrvFkpJmYQfa6mQz_QlsD5VUcBy1d_0uGIo90S26y5sLH6wszS7q-oCGH6QQI9FLTZ_5jZ8WLZpm"
				//,"-topic", "ds"
				//,"-android-priority", "normal"
				,"-logToFile", "./FcmJavaSender.log"
		};
		

		FirebaseService.run(args);
		
	}

}
