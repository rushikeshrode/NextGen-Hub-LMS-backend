package com.nextgenhub.lms.services;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Random;

@Component
public class SmsService {

    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.phone_number}")
    private String fromPhoneNumber;

    public void sendSms(String toPhoneNumber, String message) {
        try {
            Twilio.init(accountSid, authToken);

            Message sms = Message.creator(
                    new PhoneNumber("+91" + toPhoneNumber),  // Recipient's phone number
                    new PhoneNumber(fromPhoneNumber),  // Twilio's phone number
                    message + " --NextGen LMS." // Message content
            ).create();

            System.out.println("SMS Sent with SID: " + sms.getSid());
        }catch(Exception e){
            System.out.println("Error to send SMS: "+e.getMessage());
        }
    }

    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 6-digit OTP
    }
    public String sendOTP(String toPhoneNumber,String action){
        try {
            Twilio.init(accountSid, authToken);
            String otp = generateOtp();
            String messageBody = "Your OTP code for " +action+ " is : " + otp;
            Message sms = Message.creator(
                    new PhoneNumber("+91" + toPhoneNumber),  // Recipient's phone number
                    new PhoneNumber(fromPhoneNumber),  // Twilio's phone number
                    messageBody // Message content
            ).create();

            System.out.println("SMS Sent with SID: " + sms.getSid());
            return otp;
        }catch (ApiException e) {
            System.out.println("Twilio API Error: " + e.getMessage());
            System.out.println("More Info: " + e.getMoreInfo());
            System.out.println("Code: " + e.getCode());
            return null;
        }
        catch(Exception e){
            System.out.println("Error to send SMS: "+e.getMessage());
            return null;
        }
    }
}
