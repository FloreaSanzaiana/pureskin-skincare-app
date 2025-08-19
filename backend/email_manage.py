import smtplib
from email.message import EmailMessage
import os
from dotenv import load_dotenv
def sendEmail(email,token):
    EMAIL_SENDER = os.getenv("EMAIL_SENDER")
    EMAIL_PASSWORD = os.getenv("EMAIL_PASSWORD")
    EMAIL_RECEIVER = email

    msg = EmailMessage()
    msg['Subject'] = "Resetare parola PureSkin"
    msg['From'] = EMAIL_SENDER
    msg['To'] = EMAIL_RECEIVER
    msg.set_content("Your code is: "+token+".")

    try:
        with smtplib.SMTP_SSL("smtp.gmail.com", 465) as server:
            server.login(EMAIL_SENDER, EMAIL_PASSWORD)
            server.send_message(msg)
        return 1
    except Exception as e:
        return 0


