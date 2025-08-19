import json
import pandas as pd
import numpy as np
from sklearn.preprocessing import MinMaxScaler
from scipy.spatial.distance import cosine
from flask import jsonify
from utils import *


def read_csv():

    file_path = "./knowledge/products.csv"
    try:
        df = pd.read_csv(file_path)
        return df
    except Exception as e:
        print(f"Error reading csv: {e}")
        return None


def load_json_data():
    file_path = "./knowledge/ingredients_for_concerns.json"
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





def apply_user_restrictions(user_data, products, is_time, time, restrictions,nr_products):

    if products.empty:
        return products

    if not restrictions:
        return products.head(5)

    if is_time:
        time = time.lower().strip()
        day_equivalents = ["day", "morning"]
        night_equivalents = ["night", "evening"]

        if time in day_equivalents:
            products = products[
                products['time'].str.lower().isin(['day', 'morning', 'both', '']) | products['time'].isna()]
        elif time in night_equivalents:
            pass

    filtered_products = products.copy()

    recommended_ingredients = set()
    avoided_ingredients = set()

    def find_age_range(age, age_ranges):
        for age_range in age_ranges:
            if "-" in age_range:
                min_age, max_age = map(int, age_range.split("-"))
                if min_age <= age <= max_age:
                    return age_range
            elif age_range.endswith("+") or age_range.endswith("-100"):
                min_age = int(age_range.split("-")[0]) if "-" in age_range else int(age_range.rstrip("+"))
                if age >= min_age:
                    return age_range
        return None

    if "age" in restrictions and "varsta" in user_data:
        age = int(user_data["varsta"])
        age_ranges = list(restrictions["age"].keys())
        matched_range = find_age_range(age, age_ranges)

        if matched_range and matched_range in restrictions["age"]:
            recommended_ingredients.update(
                [ing.lower() for ing in restrictions["age"][matched_range].get("recommended", [])])
            avoided_ingredients.update([ing.lower() for ing in restrictions["age"][matched_range].get("avoid", [])])

    if "sex" in restrictions and "sex" in user_data:
        sex = user_data["sex"]
        if sex in restrictions["sex"]:
            recommended_ingredients.update([ing.lower() for ing in restrictions["sex"][sex].get("recommended", [])])
            avoided_ingredients.update([ing.lower() for ing in restrictions["sex"][sex].get("avoid", [])])

    if "skin_type" in restrictions and "skin_type" in user_data:
        skin_types = user_data["skin_type"].split(',') if isinstance(user_data["skin_type"], str) else [
            user_data["skin_type"]]
        for skin_type in skin_types:
            skin_type = str(skin_type).strip()
            if skin_type in restrictions["skin_type"]:
                recommended_ingredients.update(
                    [ing.lower() for ing in restrictions["skin_type"][skin_type].get("recommended", [])])
                avoided_ingredients.update(
                    [ing.lower() for ing in restrictions["skin_type"][skin_type].get("avoid", [])])

    if "skin_sensitivity" in restrictions and "skin_sensitivity" in user_data:
        sensitivity = user_data["skin_sensitivity"]

        if isinstance(sensitivity, set):
            if sensitivity:
                sensitivity = next(iter(sensitivity))
            else:
                sensitivity = ""

        sensitivity_str = str(sensitivity).strip()

        if sensitivity_str in restrictions["skin_sensitivity"]:
            recommended_ingredients.update(
                [ing.lower() for ing in restrictions["skin_sensitivity"][sensitivity_str].get("recommended", [])])
            avoided_ingredients.update(
                [ing.lower() for ing in restrictions["skin_sensitivity"][sensitivity_str].get("avoid", [])])

    if "skin_phototype" in restrictions and "skin_phototype" in user_data:
        phototype = user_data["skin_phototype"]

        if isinstance(phototype, set):
            if phototype:
                phototype = next(iter(phototype))
            else:
                phototype = ""

        phototype_str = str(phototype).strip()

        if phototype_str in restrictions["skin_phototype"]:
            recommended_ingredients.update(
                [ing.lower() for ing in restrictions["skin_phototype"][phototype_str].get("recommended", [])])
            avoided_ingredients.update(
                [ing.lower() for ing in restrictions["skin_phototype"][phototype_str].get("avoid", [])])

    concerns_recommended = set()
    if "concerns" in restrictions and "concerns" in user_data:
        user_concerns = user_data["concerns"]
        if isinstance(user_concerns, str):
            user_concerns = [concern.strip() for concern in user_concerns.split(',')]
        elif isinstance(user_concerns, list):
            user_concerns = [concern.strip() for concern in user_concerns]

        for concern in user_concerns:
            if concern in restrictions["concerns"]:
                concerns_recommended.update(
                    [ing.lower() for ing in restrictions["concerns"][concern].get("recommended", [])])
                avoided_ingredients.update([ing.lower() for ing in restrictions["concerns"][concern].get("avoid", [])])

    def calculate_product_score(ingredients_str, price, spf, irritating_ingredients_str):
        if pd.isna(ingredients_str) or not ingredients_str:
            return 0

        try:
            ingredients_list = ast.literal_eval(ingredients_str)
        except (ValueError, SyntaxError):
            ingredients_list = []  # Dacă stringul nu poate fi evaluat ca listă

        ingredients_list = [ing.strip().lower() for ing in ingredients_list]
        score = 0

        recommended_matches = sum(1 for rec in recommended_ingredients if any(rec in ing for ing in ingredients_list))
        score += recommended_matches * 10

        concerns_matches = sum(1 for rec in concerns_recommended if any(rec in ing for ing in ingredients_list))
        score += concerns_matches * 15

        avoided_matches = sum(1 for avoid in avoided_ingredients if any(avoid in ing for ing in ingredients_list))
        score -= avoided_matches * 20

        if irritating_ingredients_str:
            try:
                irritating_list = ast.literal_eval(irritating_ingredients_str)
            except (ValueError, SyntaxError):
                irritating_list = []

            irritating_list = [ing.strip().lower() for ing in irritating_list]
            irritating_count = len(irritating_list)
            score -= irritating_count * 5

        if is_time and str(time).lower().strip()=="day":
          if not pd.isna(spf) and spf > 0 and "skin_phototype" in user_data:
            phototype = user_data["skin_phototype"]
            if "Pale white skin" in phototype or "White skin" in phototype:
                score += min(spf, 50) * 0.5
            else:
                score += min(spf, 30) * 0.3
        else:
            score -= min(spf, 0) * 0.9

        if not pd.isna(price) and price:
            try:
                price_clean = ''.join(c for c in str(price) if c.isdigit() or c == '.')
                price_value = float(price_clean) if price_clean else 0

                if price_value > 0:
                    price_factor = max(0, 1 - (price_value / 200))
                    score += price_factor * 10
            except (ValueError, TypeError):
                pass

        return score

    filtered_products['score'] = filtered_products.apply(
        lambda row: calculate_product_score(
            row['clean_ingreds'],
            row['price'],
            row['spf'],
            row['irritating_ingredients']
        ),
        axis=1
    )

    filtered_products = filtered_products[filtered_products['score'] > 0]

    filtered_products = filtered_products.sort_values(by='score', ascending=False)
    filtered_products = filtered_products.drop(columns=['score'])

    return filtered_products.head(nr_products)





def normalize_string(text):
    if isinstance(text, str):
        return text.lower().strip().replace(" ", "")
    return str(text).lower().strip().replace(" ", "")



def getProducts(user_data, product_type, nr_products,is_time, time):
    products = read_csv()
    if products is None:
        return None

    restrictions = load_json_data()
    if restrictions is None:
        return None


    products['product_type_normalized'] = products['product_type'].apply(normalize_string)
    normalized_product_type = normalize_string(product_type)

    filtered_products = products[products['product_type_normalized'] == normalized_product_type].copy()

    filtered_products.drop(columns=['product_type_normalized'], inplace=True)

    if filtered_products.empty:
        return None

    products_recommended =apply_user_restrictions(user_data, filtered_products, is_time, time, restrictions,nr_products)

    if not products_recommended.empty:
        return products_recommended
    else:
        return None



def get_product_by_id(id_product):
    # Citește fișierul CSV
    file_path = "./knowledge/products.csv"
    try:
        df = pd.read_csv(file_path)
        df = df.fillna('')
        # Căutăm rândul unde 'id' este egal cu id_product
        product_row = df[df['id'] == id_product]

        if product_row.empty:
            print(f"Produsul cu id {id_product} nu a fost găsit.")
            return None
        else:
            return product_row
    except Exception as e:
        print(f"Error reading CSV: {e}")
        return None


def recommend_products(user_id,product_type,nr_products=1,is_time=False,time="day"):  #time-morning/evening
    user_data=get_user_details_for_recommendations(int(user_id))
    products=getProducts(user_data,str(product_type).lower().strip(),nr_products,is_time,str(time).lower().strip())
    if products is not None and not products.empty:
        product_id=products.to_dict(orient='records')[0]["id"]
        return get_product_by_id(product_id).to_dict(orient='records')
    else:
        return None


if __name__=="__main__":

    print(recommend_products(55,"serum",6,True,"night"))