package com.viki3d.firebase.sender;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

public class FirebaseService {
	
	enum ParamsEnum {
		SERVER_KEY("-serverKey"),
		TOKEN("-token"),
		TOPIC("-topic"),
		ANDROID_PRIORITY("-android-priority"),
		APS_ALERT("-aps-alert"),
		LOG_TO_FILE("-logToFile")
		;
		
		String parameterName;
		
		ParamsEnum(String s) {
			this.parameterName = s;
		}
		
		public String getParameterName() {
			return this.parameterName;
		}
	}

	public static void printUsage() {
		System.out.println("Usage: ");
		System.out.println("java -cp .;./lib -jar PushControlServer.jar Main [options]");
		System.out.println();
		System.out.println("Options:");
		System.out.println(" -serverKey <your-firebase-adminsdk-java-server-key.json>");
		System.out.println(" [-token <your-device-token-string>]");
		System.out.println(" [-topic <your-topic>]");
		System.out.println(" [-android-priority <NORMAL/HIGH>]");
		System.out.println(" [-logToFile <filename>]");
		
	
	}
		
	public static void run(String[] args) {
		
		//  Input params
		String serverCredentialsJson = null;
		String token = null;
		String topic = null;
		Map<String, String> data = new HashMap<>();
		AndroidConfig.Priority androidPriority = null;
		LogService.logToFile = false;
		
		//  Fill input params' values
		LogService.debug(FirebaseService.class, "Reading input parameters...");//can't log this to log-file; file-logging still not set here
		if (args!=null && args.length>0) {
			
			try {
				outer:
				for (int i=0;i<args.length;i++) {
					
					for (ParamsEnum pe : ParamsEnum.values()) {
						switch (pe) {
						case SERVER_KEY :
							if (pe.getParameterName().toUpperCase().equals(args[i].toUpperCase())) {
								if (i>=args.length-1)
									throw new Exception("Error: Parameter " + ParamsEnum.SERVER_KEY.getParameterName() + " value is missing !" );
								serverCredentialsJson = args[i+1];
								if (!Files.exists(Paths.get(serverCredentialsJson))) {
									throw new Exception("Error: Server credentials json file " + serverCredentialsJson + " not found !" );
								}
								i++;
								continue outer;
							}
							break;
						case TOKEN:
							if (pe.getParameterName().toUpperCase().equals(args[i].toUpperCase())) {
								if (i>=args.length-1)
									throw new Exception("Error: Parameter " + ParamsEnum.TOKEN.getParameterName() + " value is missing !" );
								token = args[i+1];
								i++;
								continue outer;
							}
							break;
						case TOPIC:
							if (pe.getParameterName().toUpperCase().equals(args[i].toUpperCase())) {
								if (i>=args.length-1)
									throw new Exception("Error: Parameter " + ParamsEnum.TOPIC.getParameterName() + " value is missing !" );
								topic = args[i+1];
								i++;
								continue outer;
							}
							break;
						case ANDROID_PRIORITY:
							if (pe.getParameterName().toUpperCase().equals(args[i].toUpperCase())) {
								if (i>=args.length-1)
									throw new Exception("Error: Parameter " + ParamsEnum.ANDROID_PRIORITY.getParameterName() + " value is missing !" );
								String priorityStr = args[i+1];
								try {
									androidPriority = AndroidConfig.Priority.valueOf(priorityStr.toUpperCase());
								}
								catch (IllegalArgumentException illArgEx) {
									//  Build possible values for AndroidConfig.Priority
									StringBuilder possibleValues = new StringBuilder("[");
									for (AndroidConfig.Priority possibleValue : AndroidConfig.Priority.values()) {
										possibleValues.append(possibleValue.name() + ", ");
									}
									if (possibleValues.toString().endsWith(", "))
										possibleValues.delete(possibleValues.length()-2, possibleValues.length());
									possibleValues.append("]");

									
									throw new Exception("Error: Parameter " + ParamsEnum.ANDROID_PRIORITY.getParameterName() + " value is not " 
											+ "within allowed values: " + possibleValues);
								}
								//if (priority.toUpperCase().equals(AndroidConfig.Priority.NORMAL))
								i++;
								continue outer;
							}
							break;
						case APS_ALERT :
							
							break;
						case LOG_TO_FILE :
							if (pe.getParameterName().toUpperCase().equals(args[i].toUpperCase())) {
								if (i>=args.length-1)
									throw new Exception("Error: Parameter " + ParamsEnum.LOG_TO_FILE.getParameterName() + " value is missing !" );
								String logFilename = args[i+1];
								LogService.logToFile(FirebaseService.class, logFilename);
								
								i++;
								continue outer;
							}
							break;
							
						default:
							//  Skip parameter values; we check for valid parameter names:
							if (i>0 && args[i-1].startsWith("-")) continue;
							
							
							//  Build possible values for ParamsEnum
							StringBuilder possibleValues = new StringBuilder("[");
							for (ParamsEnum possibleValue : ParamsEnum.values()) {
								possibleValues.append(possibleValue.getParameterName() + ", ");
							}
							if (possibleValues.toString().endsWith(", "))
								possibleValues.delete(possibleValues.length()-2, possibleValues.length());
							possibleValues.append("]");
							
							throw new Exception("Error: Parameter " + args[i] + " value is not " 
									+ "within allowed values: " + possibleValues);
						}
						
							
						
						
					}//for (ParamsEnum 
				}//for (int i=0
				
				//  Verify all needed parameters are here:
				if (serverCredentialsJson==null) {
					throw new Exception("Error: Server credentials json file: " + ParamsEnum.SERVER_KEY + " is obligatory parameter!" );
				}
				if (token==null && topic==null) {
					throw new Exception("Error: You must provide either " + ParamsEnum.TOKEN.getParameterName() + " or " 
							+ ParamsEnum.TOPIC.getParameterName() + " parameter !");
				}
				
				
				sendFirebaseCloudMessage(serverCredentialsJson, token, topic, data, androidPriority);
			}
			catch (Exception ex) {
				LogService.error(FirebaseService.class, ex.getMessage());
				ex.printStackTrace();
			}
		}
		else {
			printUsage();
		}
		
	}
	
	public static void sendFirebaseCloudMessage(String serverCredentialsJson, 
			String token, String topic, Map<String, String> data, AndroidConfig.Priority androidPriority) {

		try {
		
			GoogleCredentials googleCredentials;
			
			//  Obtain GoogleCredentials from json file
			String jsonPath = serverCredentialsJson;
			FileInputStream credentialsStream = new FileInputStream(jsonPath);
			googleCredentials = GoogleCredentials.fromStream(credentialsStream);
		
			
			//  Build Firebase options
			FirebaseOptions options = new FirebaseOptions.Builder()
				    .setCredentials(googleCredentials)
				    .build();

			
			//  Initialize Firebase
			FirebaseApp.initializeApp(options);
			
		
			//  Generate Firebase cloud message (FCM)
			Message.Builder messageBuilder = Message.builder();
			
			//	Set token
			if (token!=null) {
				messageBuilder.setToken(token);
			}
			
			//	Set topic
			if (topic!=null) {
				messageBuilder.setTopic(topic);
			}

			//  Set data
			for (Map.Entry<String, String> entry : data.entrySet()) {
				messageBuilder.putData(entry.getKey(), entry.getValue());
			}
			
			//  Android config
			AndroidConfig.Builder androidConfigBuilder = AndroidConfig.builder();
			if (androidPriority!=null)
				androidConfigBuilder.setPriority(androidPriority).build();
			AndroidConfig androidConfig = androidConfigBuilder.build();
			messageBuilder.setAndroidConfig(androidConfig);
			
			//  Apns config
			ApnsConfig.Builder apnsConfigBuilder = ApnsConfig.builder();
			apnsConfigBuilder.setAps(Aps.builder().build());
			ApnsConfig apnsConfig = apnsConfigBuilder.build();
			messageBuilder.setApnsConfig(apnsConfig);
			
			//  Build the FCM message
			Message message = messageBuilder.build(); 
	
			
			//  Send the FCM and obtain messageId as result
			String messageId = FirebaseMessaging.getInstance().send(message);
			
			LogService.debug(FirebaseService.class, "Successfully sent message: " + messageId);

		}
		catch (Exception ex) {
			LogService.debug(FirebaseService.class, "asdasdddddddddddddd");
			LogService.error(FirebaseService.class, ex.getMessage());
			ex.printStackTrace();
		}
		
	}
	
	
}
