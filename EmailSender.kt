package com.example.localtourismguide

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


object EmailSender {
    fun sendEmail(toEmail: String, subject: String, body: String) {
        val properties = Properties()
        properties["mail.smtp.host"] = "smtp.gmail.com"
        properties["mail.smtp.port"] = "587"
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true"

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("tourismguide419@gmail.com"
                    , "mrpl gcuv dgjp tpbm")
            }
        })

        val message = MimeMessage(session)
        message.setFrom(InternetAddress("tourismguide419@gmail.com"))
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
        message.subject = subject
        message.setText(body)

        Transport.send(message)
    }
}
