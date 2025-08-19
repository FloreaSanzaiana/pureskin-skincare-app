from mysql.connector.cursor import MySQLCursorDict
from db_connect import *
from api_requests import *



def load_endings():
    try:
        with open('endings.json', 'r', encoding='utf-8') as file:
            data = json.load(file)
            return data['endings']
    except:
        return [
            "Tap the links below for more details.",
            "Check out more resources in the app.",
            "Explore additional tips in our guides section.",
            "Find more helpful content below."
        ]


def getBotResponse(message):
    endings=load_endings()
    random_ending = choice(endings)

    chat_response=apiResponse(message)
    links=get_google_links(message)
    r=apiVerification(message).strip().lower()

    if("yes" in r):
      final_response = chat_response + "\n" + random_ending + "\n\n"+links
    else:
        final_response = chat_response

    return final_response





def getResponse(message, sender, user_id):
    insertMessage(message, sender, user_id)

    bot_message = getBotResponse(message)
    insertMessage(bot_message, "bot", user_id)

    con = get_new_connection()
    cursor = con.cursor(dictionary=True)
    query = """
        SELECT message_id, user_id, sender, text, timestamp
        FROM messages
        WHERE user_id = %s AND sender = 'bot'
        ORDER BY timestamp DESC
        LIMIT 1
    """
    cursor.execute(query, (user_id,))
    last_bot_message = cursor.fetchone()

    query = """
           SELECT message_id, user_id, sender, text, timestamp
           FROM messages
           WHERE user_id = %s AND sender = 'user'
           ORDER BY timestamp DESC
           LIMIT 1
       """
    cursor.execute(query, (user_id,))
    last_user_message = cursor.fetchone()
    cursor.close()
    con.close()

    return [last_bot_message,last_user_message]


def insertMessage(message, sender, user_id):
    con = get_new_connection()
    cursor = con.cursor()
    query = "INSERT INTO messages(user_id, sender, text) VALUES (%s, %s, %s)"
    cursor.execute(query, (user_id, sender, message))
    con.commit()
    cursor.close()
    con.close()
    return True

def insertMessageOnly(message, sender, user_id):
    con = get_new_connection()
    cursor = con.cursor(dictionary=True)
    query = "INSERT INTO messages(user_id, sender, text) VALUES (%s, %s, %s)"
    cursor.execute(query, (user_id, sender, message))
    con.commit()
    query = """
              SELECT message_id, user_id, sender, text, timestamp
              FROM messages
              WHERE user_id = %s AND sender = %s
              ORDER BY timestamp DESC
              LIMIT 1
          """
    cursor.execute(query, (user_id,sender,))
    message = cursor.fetchone()
    cursor.close()
    con.close()
    return message


def getAllMessages(user_id):
    con = get_new_connection()
    cursor = con.cursor(dictionary=True)
    query = "SELECT * FROM messages WHERE user_id = %s ORDER BY timestamp"
    cursor.execute(query, (user_id,))
    mess = cursor.fetchall()
    cursor.close()
    con.close()
    return mess


def deleteAllMessages(user_id):
    con = get_new_connection()
    cursor = con.cursor()
    query = "DELETE FROM messages WHERE user_id = %s"
    cursor.execute(query, (user_id,))
    con.commit()
    cursor.close()
    con.close()
    return True

if __name__=="__main__":
    #insertMessage("thx","bot",14)
    print(getAllMessages(14))
    #deleteAllMessages(14)
    #pass
