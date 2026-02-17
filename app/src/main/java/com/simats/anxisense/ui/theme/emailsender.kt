package com.simats.anxisense

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    fun sendOtpEmail(
        senderEmail: String,
        senderPassword: String,
        receiverEmail: String,
        otp: String
    ) {

        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, senderPassword)
                }
            })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(senderEmail))
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(receiverEmail)
            )
            message.subject = "AnxiSense OTP Verification"
            message.setText("Your OTP is: $otp")

            Transport.send(message)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
