import json
from flask import jsonify
from utils import *


def load_json_data(file_path):
    try:
        with open(file_path, 'r') as file:
            data = json.load(file)
        return data
    except FileNotFoundError:
        print(f"Error: File {file_path} not found.")
        return None
    except json.JSONDecodeError:
        print(f"Error: File {file_path} invalid JSON.")
        return None


def get_skincare_steps(routine_json_path, routine_type):
    routine_data = load_json_data(routine_json_path)
    if routine_data:
        steps = routine_data.get(routine_type.lower().strip(), [])
        return steps

    return []



def get_restrictions(restrictions_json_path):
    return load_json_data(restrictions_json_path)


def apply_restrictions(user_data,routine_type):

    steps = get_skincare_steps("knowledge/steps.json", routine_type)
    restrictions = get_restrictions("knowledge/restrictions.json")

    if not steps or not restrictions:
        return steps

    conflicts = restrictions
    restricted_steps = set()

    if "skin_type" in conflicts and "skin_type" in user_data:
        user_skin_types = user_data["skin_type"].split(',') if isinstance(user_data["skin_type"], str) else user_data[
            "skin_type"]
        for skin_type in user_skin_types:
            skin_type = skin_type.strip() if isinstance(skin_type, str) else skin_type
            if skin_type in conflicts["skin_type"]:
                restricted_steps.update([step.lower() for step in conflicts["skin_type"][skin_type]])

    if "age" in conflicts and "varsta" in user_data:
        age = int(user_data["varsta"])
        for age_range in conflicts["age"]:
            if "-" in age_range:
                min_age, max_age = map(int, age_range.split("-"))
                if min_age <= age <= max_age:
                    restricted_steps.update([step.lower() for step in conflicts["age"][age_range]])
            elif age_range.endswith("+"):
                min_age = int(age_range[:-1])
                if age >= min_age:
                    restricted_steps.update([step.lower() for step in conflicts["age"][age_range]])

    if "sex" in conflicts and "sex" in user_data:
        sex = user_data["sex"]
        if sex in conflicts["sex"]:
            restricted_steps.update([step.lower() for step in conflicts["sex"][sex]])

    if "skin_sensitivity" in conflicts and "skin_sensitivity" in user_data:
        sensitivity = user_data["skin_sensitivity"]
        if sensitivity in conflicts["skin_sensitivity"]:
            restricted_steps.update([step.lower() for step in conflicts["skin_sensitivity"][sensitivity]])

    if "skin_phototype" in conflicts and "skin_phototype" in user_data:
        phototype = user_data["skin_phototype"]
        if phototype in conflicts["skin_phototype"]:
            restricted_steps.update([step.lower() for step in conflicts["skin_phototype"][phototype]])

    if "concerns" in conflicts and "concerns" in user_data:
        user_concerns = user_data["concerns"].split(',') if isinstance(user_data["concerns"], str) else user_data[
            "concerns"]
        for concern in user_concerns:
            concern = concern.strip() if isinstance(concern, str) else concern
            if concern in conflicts["concerns"]:
                restricted_steps.update([step.lower() for step in conflicts["concerns"][concern]])


    filtered_steps = [step for step in steps if step.lower() not in restricted_steps]

    return filtered_steps




def recommend_routine(user_id,routine_type):
    user_data=get_user_details_for_recommendations(user_id)
    print(user_data)
    routine=apply_restrictions(user_data,routine_type)
    if routine:
       return routine
    return  None

if __name__=="__main__":
    print(recommend_routine(55,"evening"))