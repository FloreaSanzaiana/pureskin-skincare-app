
import json
import random
from email_manage import *
from datetime import timedelta
import bcrypt
from session_tokens import *
from daily_quote import *
from utils import  *
from products_recommendation import recommend_products
from full_routine_recommendation import  recommend
from manage_routines import *
from menage_messages import *
from manage_diary import *
from manage_daily_logs import *

def cryptPassword(password):
    return bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')


def getAllUsers():
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM users")
    rows = cursor.fetchall()
    users_list = json.dumps(rows, indent=4)
    cursor.close()
    return users_list

def getUserById(user_id):
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM users where id = %s", (user_id,))
    rows = cursor.fetchall()
    users_list = json.dumps(rows, indent=4)
    cursor.close()
    return users_list

def addUser(data):
    deleteUserWithoutDetails(str(data["email"]))
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM users WHERE email = %s", (data["email"],))
    new_user = cursor.fetchone()
    if new_user is not None:
        return jsonify({"message":"email already used"}), 401
    cursor.execute(
        "INSERT INTO users (username, email, password_hash) VALUES (%s, %s, %s)",
        (data["username"], data["email"], cryptPassword(data["password_hash"]))
    )
    conn.commit()
    user_id = cursor.lastrowid
    cursor.close()

    return jsonify({"id":str(user_id)}),200


def deleteUser(request,user_id):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    print(user_id,type(user_id))
    cursor = conn.cursor(dictionary=True)
    cursor.execute("DELETE FROM sessions WHERE user_id = %s", (user_id,))
    cursor.execute("DELETE FROM user_details WHERE id_user = %s", (user_id,))
    cursor.execute("DELETE FROM users WHERE id = %s", (user_id,))
    conn.commit()
    cursor.close()

def editUser(data,user_id):
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT * FROM users WHERE id = %s", (user_id,))
    user = cursor.fetchone()

    if user is not None:
        cursor.execute("""
               UPDATE users 
               SET username = %s, email = %s, password_hash = %s 
               WHERE id = %s
           """, (data["username"], data["email"], data["password_hash"], user_id))

        conn.commit()

        cursor.execute("SELECT * FROM users WHERE id = %s", (user_id,))
        new_user = cursor.fetchone()

        cursor.close()
        return json.dumps(new_user, indent=4)
    else:
        cursor.close()
        return -1



def findUser2(data):
    cursor= conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM users where email=%s ",(data["email"],))
    user=cursor.fetchall()
    cursor.close()
    print(user)
    if len(user) == 0:
        return jsonify({"error": "User not found"}), 401
    user=user[0]
    print(str(user["id"]))
    token=generate_session_token(user["id"])[0]
    experation=generate_session_token(user["id"])[1]
    save_session_token(user["id"],token)
    if True: #bcrypt.checkpw(data["password_hash"].encode('utf-8'), user["password_hash"].encode('utf-8'))
        user_details=get_user_details(user["id"])
        return jsonify({"id":str(user["id"]),"session_token":str(token),"expires_at":experation,"email":user_details["email"],"username":user_details["username"],"varsta":user_details["varsta"],"sex":user_details["sex"],"skin_type":user_details["skin_type"],"skin_sensitivity":user_details["skin_sensitivity"],"skin_phototype":user_details["skin_phototype"],"concerns":user_details["concerns"],"poza_profil":user_details["poza_profil"]}),200
    return jsonify({"error": "User not found"}), 401

def findUser(data):
    deleteUserWithoutDetails(str(data["email"]))
    con=get_new_connection()
    cursor = con.cursor(dictionary=True)
    cursor.execute("SELECT * FROM users WHERE email = %s", (data["email"],))
    user = cursor.fetchall()
    cursor.close()
    con.close()

    if len(user) == 0:
        return jsonify({"error": "User not found"}), 401

    user = user[0]
    print("email primit:_" + data["email"] + "_")
    print("id user:_" + str(user["id"]) + "_")
    token, experation = generate_session_token(user["id"])
    save_session_token(user["id"], token)

    if bcrypt.checkpw(data["password_hash"].encode('utf-8'), user["password_hash"].encode('utf-8')):
        user_details = get_user_details(user["id"])

        for key in user_details:
            if isinstance(user_details[key], set):
                user_details[key] = list(user_details[key])

        if isinstance(user_details["poza_profil"], (bytes, bytearray)):
            import base64
            user_details["poza_profil"] = base64.b64encode(user_details["poza_profil"]).decode('utf-8')
        return jsonify({
            "id": str(user["id"]),
            "session_token": str(token),
            "expires_at": experation,
            "email": user_details["email"],
            "username": user_details["username"],
            "varsta": user_details["varsta"],
            "sex": user_details["sex"],
            "skin_type": user_details["skin_type"],
            "skin_sensitivity": user_details["skin_sensitivity"],
            "skin_phototype": user_details["skin_phototype"],
            "concerns": user_details["concerns"],
            "poza_profil": user_details["poza_profil"]
        }), 200

    return jsonify({"error": "User not found"}), 401


def generate_token():
    return str(randint(10000000, 99999999))
def generate_token_expiry():
    current_time = datetime.now()
    expiry_time = current_time + timedelta(minutes=10)
    return expiry_time
def forgotPassword(data):
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM users where email=%s ", (data["email"],))
    user = cursor.fetchall()
    cursor.close()
    if len(user) == 0:
        return jsonify({"error": "User not found"}), 401
    code=generate_token()
    timee=generate_token_expiry()
    ok=sendEmail(data["email"],code)
    if ok==1:
     cursor = conn.cursor(dictionary=True)
     cursor.execute("""UPDATE users SET reset_token = %s, token_expires = %s WHERE email = %s """, (code, timee, data["email"]))
     conn.commit()
     cursor.close()
     return jsonify({"message":"Token sent","time_expires":str(timee)}),200
    else:
        return jsonify({"error": "Token can not be sent. The email address does not exist."}), 400

def verifyCode(data):
    cursor= conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM users WHERE email = %s", (data["email"],))
    user = cursor.fetchone()
    cursor.close()
    if user is None:
        return jsonify({"message": "User not found"}), 402
    cod_bd=user["reset_token"]
    time_bd=user["token_expires"]
    if(data["reset_token"]==cod_bd and time_bd>=datetime.now()):
     return jsonify({"message":"ok","id":str(user["id"])}),200
    elif(data["reset_token"]!=cod_bd):
        return jsonify({"message":"wrong code"}),400
    elif(time_bd<datetime.now()):
        return jsonify({"message": "time expired"}), 401

def resetpassword(data):
    con=get_new_connection()
    cursor=con.cursor(dictionary=True)
    cursor.execute("""
                       UPDATE users 
                       SET password_hash = %s
                       WHERE email = %s
                   """, ( cryptPassword(data["password_hash"]),  data["email"]))
    print("email:_"+data["email"]+"_")
    print("parola:_" + data["password_hash"] + "_")
    con.commit()
    cursor.close()
    con.close()
    return jsonify({"message": "Password reset"}), 200


def get_quote_validated(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 400

    return getQuote()




def register(data):
    import base64
    poza_bytes = base64.b64decode(data["poza_profil"])
    con=get_new_connection()
    cursor = con.cursor(dictionary=True)
    cursor.execute("SELECT id FROM users WHERE email = %s", (data["email"],))

    user = int(cursor.fetchone()["id"])
    print("user:")
    print(','.join(data["concerns"]))
    print( poza_bytes)
    query = """
               INSERT INTO user_details (
                   id_user, varsta, sex, skin_type, skin_sensitivity,
                   skin_phototype, concerns, poza_profil
               )
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
           """
    cursor.execute(query,
        (user,data["varsta"],data["sex"],','.join(data["skin_type"]),','.join(data["skin_sensitivity"]),','.join(data["skin_phototype"]),','.join(data["concerns"]), poza_bytes)
    )
    con.commit()
    cursor.close()
    con.close()
    token = generate_session_token(user)[0]
    experation = generate_session_token(user)[1]
    save_session_token(user, token)

    return jsonify({
    "id": str(user),
    "session_token": str(token),
    "expires_at": experation,
    "email": data["email"],
    "username": data["username"],
    "varsta": data["varsta"],
    "sex": data["sex"],
    "skin_type": (data["skin_type"]),
    "skin_sensitivity": data["skin_sensitivity"],
    "skin_phototype": data["skin_phototype"],
    "concerns": data["concerns"],
    "poza_profil": data["poza_profil"]
     }), 200

def updateAge(request,data):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401

    cursor = conn.cursor(dictionary=True)
    query = """ UPDATE user_details SET varsta = %s  WHERE id_user = %s; """
    cursor.execute(query,(data["varsta"],data["user_id"]) )
    conn.commit()
    cursor.close()

    return jsonify({"message": "succes"}), 200

def updateSex(request,data):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    cursor = conn.cursor(dictionary=True)
    import base64
    poza_bytes = base64.b64decode(data["poza_profil"])
    query = """
                      UPDATE user_details
                      SET sex = %s, poza_profil=%s
                      WHERE id_user = %s;
                  """
    cursor.execute(query,
                   (data["sex"], poza_bytes,data["user_id"])
                   )
    conn.commit()
    cursor.close()
    return jsonify({"message": "succes"}), 200

def updateSkinType(request,data):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 402
    cursor = conn.cursor(dictionary=True)
    query = """
                      UPDATE user_details
                      SET skin_type = %s
                      WHERE id_user = %s;
                  """
    cursor.execute(query,
                   (','.join(data["skin_type"]), data["user_id"])
                   )
    conn.commit()
    cursor.close()
    return jsonify({"message": "succes"}), 200

def updateSkinSensitivity(request,data):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    cursor = conn.cursor(dictionary=True)
    query = """
                         UPDATE user_details
                         SET skin_sensitivity = %s
                         WHERE id_user = %s;
                     """
    cursor.execute(query,
                   (','.join(data["skin_sensitivity"]), data["user_id"])
                   )
    conn.commit()
    cursor.close()
    return jsonify({"message": "succes"}), 200

def updateSkinPhotoType(request,data):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    cursor = conn.cursor(dictionary=True)
    query = """
                         UPDATE user_details
                         SET skin_phototype = %s
                         WHERE id_user = %s;
                     """
    cursor.execute(query,
                   (','.join(data["skin_phototype"]), data["user_id"])
                   )
    conn.commit()
    cursor.close()
    return jsonify({"message": "succes"}), 200

def updateConcerns(request,data):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    cursor = conn.cursor(dictionary=True)
    query = """
                         UPDATE user_details
                         SET concerns = %s
                         WHERE id_user = %s;
                     """
    cursor.execute(query,
                   (','.join(data["concerns"]), data["user_id"])
                   )
    conn.commit()
    cursor.close()
    return jsonify({"message": "succes"}), 200

def getAllProducts():

    with open('old_datasets/final_products_without_new_products.csv', newline='', encoding='utf-8') as csvfile:
        products=[]
        reader = csv.DictReader(csvfile)
        for idx, row in enumerate(reader):
            products.append({
                "id": idx + 1,
                "name": row["product_name"],
                "type": row["product_type"],
                "ingredients": row["clean_ingreds"]
            })
        return products

def getProductsWithAllAtributes():
    with open('./products_with_id.csv', newline='', encoding='utf-8') as csvfile:
        products = []
        reader = csv.DictReader(csvfile)
        for idx, row in enumerate(reader):
            products.append({
                "id": row["id"],
                "name": row["product_name"],
                "type": row["product_type"],
                "ingredients": row["clean_ingreds"],
                "area": row["area"],
                "time": row["time"],
                "spf": row["spf"],
                "url": row["product_url"],
                "price": row["price"],
                "irritating_ingredients":row["irritating_ingredients"]
            })
        return products



def filter_products_list(request,products, has_spf=None, product_type=None, ingredients=None, area=None, time=None):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401

    def matches(product):
        # === Filtru SPF ===
        try:
            spf_value = int(product.get('spf', -1))
        except ValueError:
            spf_value = -1

        if has_spf is not None:
            if has_spf.lower() == 'true' and spf_value == -1:
                return False
            if has_spf.lower() == 'false' and spf_value != -1:
                return False

        if product_type and product.get('type', '').lower() != product_type.lower():
            return False

        if ingredients:
            ingreds = product.get('ingredients', '').lower()
            if not all(ing.lower() in ingreds for ing in ingredients):
                return False

        if area and product.get('area', '').lower() != area.lower():
            return False

        if time and product.get('time', '').lower() != time.lower():
            return False

        return True
    print(type([p for p in products if matches(p)]))
    return [p for p in products if matches(p)]

def recommendProduct(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    try:
        user_id = request.args.get('user_id')
        product_type = request.args.get('product_type')
        nr_products = int(request.args.get('nr_products', 1))
        time = request.args.get('time', 'day')
        is_time = request.args.get('is_time', 'false').lower() == 'true'
        area = request.args.get('area', 'face').lower()
        if not user_id or not product_type:
            return jsonify({"message": "invalid data"}), 400
        from product_recommendation2 import recommend_products as recommend_product_with_ai
        result = recommend_product_with_ai(user_id, product_type, nr_products, is_time, time,area)

        if result:
            return jsonify(result), 200
        else:
            return jsonify({"message": "recommendation failed"}), 401

    except Exception as e:
        return jsonify({"message": f"An error occurred: {str(e)}"}), 500


def recommendRoutine(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    try:

        user_id = request.args.get('user_id')
        routine_type = request.args.get('routine_type')
        nr_products = int(request.args.get('nr_products', 1))

        print(user_id,routine_type,nr_products)
        if not user_id or not routine_type:
            return jsonify({"message": "invalid data"}), 400

        result = recommend(user_id, routine_type, nr_products)

        if result:
            return jsonify(result), 200
        else:
            return jsonify({"message": "recommendation failed"}), 401

    except Exception as e:
        print("error")
        return jsonify({"message": f"An error occurred: {str(e)}"}), 500


def addroutine(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data=request.json
    response=addRoutine(data)
    if response:
        return get_routine_by_routine_id(int(response)), 200
    else:
        return jsonify({"message": "error adding routine"}), 401


def deleteroutine(request):
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"error": "Authorization token is missing or invalid"}), 401

        token = auth_header.split(" ")[1]
        if not validate_session_token(token):
            return jsonify({"error": "Invalid or expired session token"}), 401

        data = request.json
        deleteRoutine(data)
        return jsonify({"message": "Success"}), 200

    except Exception as e:
        return jsonify({"error": f"Error deleting routine: {str(e)}"}), 500



def addroutinespf(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data = request.json
    response = addSpfRoutine(data)
    if response:
        return jsonify(response), 200
    else:
        return jsonify({"message": "error adding routine"}), 401




def deleteroutinespf(request):
    try:
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"error": "Authorization token is missing or invalid"}), 401

        token = auth_header.split(" ")[1]
        if not validate_session_token(token):
            return jsonify({"error": "Invalid or expired session token"}), 401

        data = request.json
        deleteSpfRoutine(data)
        return jsonify({"message": "Success"}), 200

    except Exception as e:
        return jsonify({"error": f"Error deleting routine: {str(e)}"}), 500

def get_all_routines(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    user_id = request.args.get('user_id')
    aux=get_routines_by_user_id(int(user_id))
    if aux:

        return aux,200
    else:
        return jsonify({"error": "No routine."}), 404

def getSpf(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    user_id = request.args.get('user_id')
    aux=getSpfByUserId(int(user_id))
    if aux:

        return jsonify(aux),200
    else:
        return jsonify({"error": "No routine."}), 404

def deleteStep(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    step_id = request.json["id"]
    response=deleteStepById(step_id)
    if(response):
        return jsonify({"message": "Succes"}), 200
    else:
        return jsonify({"message": "No step deleted."}), 404

def add_Step(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    routine_id=request.json["routine_id"]
    step_name=request.json["step_name"]
    response=addStep(routine_id,step_name)
    if(response):
        return  response, 200
    else:
        return jsonify({"message": "No step added."}), 404

def modify_step_order(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data=request.json
    response=modifyStepPosition(data["id"],data["step_order"],data["routine_id"])
    if(response):
        return jsonify({"message": "succes"}),200
    else:
        return jsonify({"message": "No step added."}), 404


def modifyTimeRoutine(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data = request.json
    response=modifyNotifyTime(data["routine_id"],data["notify_time"],data["notify_days"])
    print("routine id:_"+str(data["routine_id"])+"notify_time:_"+str(data["notify_time"])+"notify_days:_"+str(data["notify_days"])+"_")
    if (response):
        return jsonify({"message": "succes"}), 200
    else:
        return jsonify({"message": "No step added."}), 404

def modifySpfTime(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data = request.json
    response = modifySpfNotifyTime(data["id"],data["start_time"],data["end_time"],data["interval_minutes"],data["active_days"])
    if (response):
        return jsonify({"message": "succes"}), 200
    else:
        return jsonify({"message": "No step added."}), 404

def addProduct(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data = request.json

    response = addProductToStep(data["id"], data["product_id"])
    if (response):
        return jsonify({"message": "succes"}), 200
    else:
        return jsonify({"message": "No product added."}), 404

def addSpfProduct(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data = request.json

    response =addProductToSpf(data["id"], data["product_id"])
    if (response):
        return jsonify({"message": "succes"}), 200
    else:
        return jsonify({"message": "No product added."}), 404


def removeRecommendedRoutines(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401

    try:
        user_id = request.args.get('user_id')
        routine_type = request.args.get('routine_type')

        if not user_id or not routine_type:
            return jsonify({"error": "Missing user_id or routine_type"}), 400

        user_id = int(user_id)

        response = deleteRecommendedRoutine(user_id, routine_type)
        if response:
            return jsonify({"message": "success"}), 200
        else:
            return jsonify({"message": "Failed to delete routine"}), 500

    except ValueError:
        return jsonify({"error": "Invalid user_id format"}), 400
    except Exception as e:
        print("error")
        return jsonify({"error": "Internal server error"}), 500


def addRecommendeRoutineToBd(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    user_id = request.args.get('user_id')
    routine_type = request.args.get('routine_type')
    response =addRecommendedRoutine(user_id,routine_type)
    if (response):
        return jsonify({"message": "succes"}), 200
    else:
        return jsonify({"message": "No product added."}), 404


def getRecommendedroutine(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    user_id = request.args.get('user_id')
    routine_type = request.args.get('routine_type')
    response=get_routineid_by_userid_and_type(user_id,routine_type)
    if response:
        return response, 200
    else:
        return jsonify({"message": "error getting routine"}), 401


def get_all_messages(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    user_id = request.args.get('user_id')
    response = getAllMessages(user_id)
    if response:
        return response, 200
    else:
        return jsonify({"message": "error getting messages"}), 401


def delete_all_messages(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    user_id = request.args.get('user_id')
    response = deleteAllMessages(user_id)
    if response:
        return jsonify({"message": "succes"}), 200
    else:
        return jsonify({"message": "error getting messages"}), 401




def get_response(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data = request.json
    user_id = data["user_id"]
    message = data["text"]
    sender = data["sender"]
    response = getResponse(message, sender, user_id)
    if response:
        return response, 200
    else:
        return jsonify({"message": "error getting messages"}), 401

def insertMessageWithoutResponse(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data = request.json
    user_id = data["user_id"]
    message = data["text"]
    sender = data["sender"]
    response = insertMessageOnly(message, sender, user_id)
    if response:
        return response, 200
    else:
        return jsonify({"message": "error getting messages"}), 401


def insertRoutineCompletion(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401

    try:
        data = request.json
        user_id = data.get("user_id")
        routine_id = data.get("routine_id", -1)
        steps = data.get("steps", [])
        spf_id = data.get("spf_id", -1)

        print(user_id,routine_id,steps,spf_id)
        if not user_id:
            return jsonify({"error": "user_id is required"}), 400

        if routine_id == -1 and spf_id == -1:
            return jsonify({"error": "Must provide either routine_id or spf_id"}), 400

        if routine_id != -1 and not steps:
            return jsonify({"error": "Steps are required for routine completion"}), 400

        response = saveRoutineCompletion(routine_id, steps, spf_id, user_id)
        if response:
            print(response)
            return jsonify(response), 200
        else:
            return jsonify({"message": "Error saving routine completion"}), 400

    except Exception as e:
        return jsonify({"error": str(e)}), 500


def get_all_routine_completions(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    user_id = request.args.get('user_id')
    response = getUserRoutineCompletions(user_id)
    if response:
        return response, 200
    else:
        return jsonify({"message": "error getting messages"}), 401

def enableDailyLog(request):
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"error": "Authorization token is missing or invalid"}), 401

        token = auth_header.split(" ")[1]
        if not validate_session_token(token):
            return jsonify({"error": "Invalid or expired session token"}), 401
        user_id = request.args.get('user_id')
        response = insertDailyLog(user_id)
        if response:
            return response, 200
        else:
            return jsonify({"message": "error getting messages"}), 401

def disableDailyLog(request):
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith("Bearer "):
            return jsonify({"error": "Authorization token is missing or invalid"}), 401

        token = auth_header.split(" ")[1]
        if not validate_session_token(token):
            return jsonify({"error": "Invalid or expired session token"}), 401
        user_id = request.args.get('user_id')
        response = deleteDailyLogByUserId(user_id)
        if response:
            return jsonify({"message": "success"}), 200
        else:
            return jsonify({"message": "error getting messages"}), 401


def getDailyLog(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    user_id = request.args.get('user_id')
    response = getDailyLogWithContentsByUserId(user_id)
    if response:
        return response, 200
    else:
        return jsonify({"message": "error getting messages"}), 401

def insertDailyLogContent(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data=request.json
    daily_log_id = data.get('daily_log_id')
    print(daily_log_id)
    skin_feeling_score = data.get('skin_feeling_score')
    print(skin_feeling_score)
    skin_condition = data.get('skin_condition')
    print(skin_condition)
    notes = data.get('notes')
    print(notes)
    weather = data.get('weather')
    print(weather)
    stress_level = data.get('stress_level')
    print(stress_level)
    response = insertDailyLogWithContent(daily_log_id,skin_feeling_score,skin_condition,notes,weather,stress_level)
    if response:
        return response, 200
    else:
        return jsonify({"message": "error getting messages"}), 400


def insertRoutineCompletionDetails(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    data=request.json
    user_id = data.get('user_id')
    routine_type = data.get('routine_type')
    steps = data.get('steps')
    max_steps= data.get('max_steps')

    response = saveRoutineCompletionDetails(user_id,routine_type,steps,max_steps)
    if response:
        return response, 200
    else:
        return jsonify({"message": "error getting routine details"}), 400


def getRoutineCompletionDetails(request):
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith("Bearer "):
        return jsonify({"error": "Authorization token is missing or invalid"}), 401

    token = auth_header.split(" ")[1]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    user_id = request.args.get('user_id')
    print(user_id)
    response = getRoutineCompletionsByUser(user_id)
    if response:
        return response, 200
    else:
        return jsonify({"message": "error getting routine details"}), 400


if __name__=="__main__":
 print("hei")
 #print(getAllProducts())