from datetime import datetime
from db_connect import *
import jwt
from dateutil.relativedelta import relativedelta

SECRET_KEY = os.getenv("SECRET_KEY")

def generate_session_token(user_id):
    user_id = int(user_id)
    expiration_time = datetime.utcnow() + relativedelta(months=1)
    expiration_timestamp = int(expiration_time.timestamp())

    payload = {
        'user_id': user_id,
        'exp': expiration_timestamp
    }
    token = jwt.encode(payload, SECRET_KEY, algorithm='HS256')
    return token,float(expiration_timestamp)


def save_session_token(user_id, session_token):
    user_id = int(user_id)
    session_token=str(session_token)
    con=get_new_connection()
    if con:
        cursor = con.cursor()
        query = "INSERT INTO sessions (user_id, session_token) VALUES (%s, %s)"
        cursor.execute(query, (user_id, session_token))
        con.commit()
        cursor.close()
        con.close()


def validate_session_token(token):
        decoded_token = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
        if decoded_token['exp'] < datetime.utcnow().timestamp():
            return None
        user_id = decoded_token['user_id']
        connection = get_new_connection()
        cursor = connection.cursor(dictionary=True)
        cursor.execute("SELECT * FROM sessions WHERE user_id = %s AND session_token = %s", (user_id, token))
        session = cursor.fetchone()
        cursor.close()
        connection.close()
        if session is None:
            return False
        return True


""""
def validate_session_token(token):
        decoded_token = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
        if decoded_token['exp'] < datetime.utcnow().timestamp():
            return None
        user_id = decoded_token['user_id']
        connection = conn
        cursor = connection.cursor(dictionary=True)
        cursor.execute("SELECT * FROM sessions WHERE user_id = %s AND session_token = %s", (user_id, token))
        session = cursor.fetchone()
        cursor.close()
        if session is None:
            return False
        return True
"""