from flask import Flask, request, jsonify
from flask_cors import CORS
from procedures import *
from session_tokens import *
from daily_quote import *
app = Flask(__name__,static_url_path='/static')
CORS(app)




@app.route('/user/delete', methods=['POST'])
def delete_user():
    data=str(request.json["user_id"])
    deleteUser(request,data)
    return jsonify({"message": "User deleted"}), 200

@app.route('/users', methods=['GET'])
def get_users():
    return getAllUsers()



@app.route('/users', methods=['POST'])
def add_user():
    data = request.json
    return addUser(data)

@app.route('/users/login', methods=['POST'])
def login_user():
    data = request.json
    return findUser(data)

@app.route('/users/register', methods=['POST'])
def register_user():
    data = request.json
    return addUser(data)



@app.route('/users/<int:user_id>', methods=['PUT'])
def update_user(user_id):
    data = request.json
    new_user=editUser(data[0],int(user_id))
    if new_user ==-1:
        return jsonify({"error": "User not found"}), 404
    else:
        return new_user, 200



@app.route('/users/forgotpassword', methods=['POST'])
def forgot_password():
    data = request.json
    return forgotPassword(data)

@app.route('/users/forgotpassword/verify_code', methods=['POST'])
def verify_code():
    data = request.json
    return verifyCode(data)

@app.route('/users/resetpassword', methods=['POST'])
def reset_password():
    data = request.json
    return resetpassword(data)


@app.route('/quote', methods=['GET'])
def get_quote():
    return get_quote_validated(request)


@app.route('/users/register_details', methods=['POST'])
def register_with_details():
    data = request.json
    return register(data)

@app.route('/user/update_age', methods=['POST'])
def update_age():
    data = request.json
    return updateAge(request,data)

@app.route('/user/update_sex', methods=['POST'])
def update_sex():
    data = request.json
    return updateSex(request,data)

@app.route('/user/update_concerns', methods=['POST'])
def update_concerns():
    data = request.json
    return updateConcerns(request,data)

@app.route('/user/update_type', methods=['POST'])
def update_type():
    data = request.json
    return updateSkinType(request,data)

@app.route('/user/update_phototype', methods=['POST'])
def update_phototype():
    data = request.json
    return updateSkinPhotoType(request,data)

@app.route('/user/update_sensitivity', methods=['POST'])
def update_sensitivity():
    data = request.json
    return updateSkinSensitivity(request,data)

@app.route('/users/<int:user_id>', methods=['POST'])
def get_user_by_id(user_id):
    token = request.headers.get('Authorization')
    if token is None:
        return jsonify({"error": "Token not provided"}), 400
    if not token.startswith("Bearer "):
        return jsonify({"error": "Invalid token format"}), 400
    token = token[7:]
    if not validate_session_token(token):
        return jsonify({"error": "Invalid or expired session token"}), 401
    return getUserById(user_id)


@app.route('/products', methods=['GET'])
def get_all_products():
    return jsonify(getAllProducts())

@app.route('/products/filtered', methods=['GET'])
def get_filtered_products():
    products_to_filter = getProductsWithAllAtributes()

    has_spf = request.args.get('spf')
    product_type = request.args.get('type')
    ingredients = request.args.getlist('ingredients')
    area = request.args.get('area')
    time = request.args.get('time')
    filtered = filter_products_list(request,products_to_filter, has_spf, product_type, ingredients, area, time)
    return jsonify(filtered)


@app.route('/recommend/product', methods=['GET'])
def recommend_product():
  return recommendProduct(request)

@app.route('/recommend/routine', methods=['GET'])
def recommend_routine__():
  return recommendRoutine(request)

@app.route('/routine/add', methods=['POST'])
def add_routine():
  return addroutine(request)

@app.route('/routine/addspf', methods=['POST'])
def add_routine_spf():
  return addroutinespf(request)


@app.route('/routine/delete', methods=['POST'])
def delete_routine():
    return deleteroutine(request)


@app.route('/routine/deletespf', methods=['POST'])
def delete_routine_spf():
    return deleteroutinespf(request)

@app.route('/routines', methods=['GET'])
def getAllRoutines():
    return get_all_routines(request)

@app.route('/routines/spf', methods=['GET'])
def get_spf():
    return getSpf(request)

@app.route('/routines/step/delete', methods=['POST'])
def deletestep():
    return deleteStep(request)
@app.route('/routines/step/add', methods=['POST'])
def add_step():
    return add_Step(request)

@app.route('/routines/step/modify', methods=['POST'])
def modify_step():
    return modify_step_order(request)

@app.route('/routines/modifytime', methods=['POST'])
def modify_routine_time():
    return modifyTimeRoutine(request)

@app.route('/routines/spf/modifytime', methods=['POST'])
def modify_spf_routine_time():
    return modifySpfTime(request)

@app.route('/routines/add_product', methods=['POST'])
def add_product_to_step():
    return addProduct(request)

@app.route('/routines/spf/add_product', methods=['POST'])
def add_product_to_spf():
    return addSpfProduct(request)

@app.route('/recommend/routine/delete', methods=['POST'])
def remove_recommended_routines():
    return removeRecommendedRoutines(request)

@app.route('/recommend/routine/add_new', methods=['POST'])
def add_recommended_routines():
    return addRecommendeRoutineToBd(request)


@app.route('/recommend/routine/get', methods=['POST'])
def get_recommended_routines():
  return getRecommendedroutine(request)

@app.route('/messages', methods=['GET'])
def get_messages():
  return get_all_messages(request)

@app.route('/messages/remove', methods=['POST'])
def delete_messages():
  return delete_all_messages(request)

@app.route('/messages/response', methods=['POST'])
def insert_message():
  return get_response(request)

@app.route('/messages/add_message', methods=['POST'])
def insert_message_without_response():
  return insertMessageWithoutResponse(request)

@app.route('/routines/complete', methods=['POST'])
def save_routine_complete():
  return insertRoutineCompletion(request)

@app.route('/routines/complete', methods=['GET'])
def get_completed_routine():
  return get_all_routine_completions(request)

@app.route('/daily_log/disable', methods=['POST'])
def disable_daily_log():
  return disableDailyLog(request)

@app.route('/daily_log/enable', methods=['POST'])
def enable_daily_log():
  return enableDailyLog(request)

@app.route('/daily_log/enable', methods=['GET'])
def get_daily_log():
  return getDailyLog(request)

@app.route('/daily_log/add_content', methods=['POST'])
def insert_daily_log_content():
  return insertDailyLogContent(request)

@app.route('/routines_complete/details', methods=['POST'])
def insert_routine_completions_details():
  return insertRoutineCompletionDetails(request)

@app.route('/routines_complete/details', methods=['GET'])
def get_routine_completions_details():
  return getRoutineCompletionDetails(request)




if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

