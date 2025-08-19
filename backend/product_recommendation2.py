import ast
import json
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
import joblib
from flask import jsonify
from utils import get_user_details_for_recommendations

rf_model = None
important_ingredients = None
_system_initialized = False


def load_rf_model(model_path='./skincare_rf_model2.pkl'):
    global rf_model
    try:
        rf_model = joblib.load(model_path)
        return True
    except Exception as e:
        return False


def extract_important_ingredients(json_path='./knowledge/ingredients_for_concerns.json'):
    global important_ingredients
    try:
        with open(json_path, 'r') as f:
            restrictions = json.load(f)

        all_ingredients = set()
        for category in restrictions.values():
            for subcategory in category.values():
                if isinstance(subcategory, dict):
                    all_ingredients.update(subcategory.get('recommended', []))
                    all_ingredients.update(subcategory.get('avoid', []))

        important_ingredients = sorted(list(all_ingredients))
        return True
    except Exception as e:
        return False


def ensure_system_initialized():
    global _system_initialized, rf_model, important_ingredients

    if not _system_initialized:
        success = initialize_rf_system()
        _system_initialized = True
        return success

    if rf_model is None or important_ingredients is None:
        success = initialize_rf_system()
        return success

    return True


def prepare_user_features(user_data):
    features = []

    features.append(int(user_data.get('varsta', 25)))

    sex = user_data.get('sex', '')
    features.extend([
        1 if sex == 'Male' else 0,
        1 if sex == 'Female' else 0
    ])

    skin_type = user_data.get('skin_type', '')
    features.extend([
        1 if 'Normal Skin' in skin_type else 0,
        1 if 'Dry Skin' in skin_type else 0,
        1 if 'Oily Skin' in skin_type else 0,
        1 if 'Combination Skin' in skin_type or 'Karma Skin' in skin_type else 0
    ])

    sensitivity = str(user_data.get('skin_sensitivity', '')).strip()
    features.extend([
        1 if sensitivity == 'Not sensitive at all' else 0,
        1 if sensitivity == 'Somewhat sensitive' else 0,
        1 if sensitivity == 'Very sensitive' else 0
    ])

    phototype = str(user_data.get('skin_phototype', '')).strip()
    features.extend([
        1 if phototype == 'Pale white skin' else 0,
        1 if phototype == 'White skin' else 0,
        1 if phototype == 'Light brown skin' else 0,
        1 if phototype == 'Moderate brown skin' else 0,
        1 if phototype == 'Dark brown skin' else 0,
        1 if phototype == 'Deep brown skin' else 0
    ])

    all_concerns = [
        'Acne & Blemishes', 'Anti-aging', 'Black Heads', 'Dark Circles',
        'Dark Spots', 'Dryness', 'Dullness', 'Fine Lines & Wrinkles',
        'Loss of Firmness', 'Oiliness', 'Puffiness', 'Redness',
        'Uneven Texture', 'Visible Pores'
    ]

    user_concerns = user_data.get('concerns', '')
    if isinstance(user_concerns, str):
        user_concerns = [c.strip() for c in user_concerns.split(',')]
    elif not isinstance(user_concerns, list):
        user_concerns = []

    for concern in all_concerns:
        features.append(1 if concern in user_concerns else 0)

    return features


def prepare_product_features(product):
    global important_ingredients
    features = []

    try:
        product_ingredients = ast.literal_eval(product['clean_ingreds']) if pd.notna(product['clean_ingreds']) else []
        product_ingredients_lower = [ing.lower().strip() for ing in product_ingredients]
    except:
        product_ingredients_lower = []

    for ingredient in important_ingredients:
        has_ingredient = 0
        for prod_ing in product_ingredients_lower:
            if ingredient.lower() in prod_ing:
                has_ingredient = 1
                break
        features.append(has_ingredient)

    try:
        price_clean = ''.join(c for c in str(product['price']) if c.isdigit() or c == '.')
        price_value = float(price_clean) if price_clean else 0
    except:
        price_value = 0
    features.append(price_value)

    spf_raw = product['spf'] if pd.notna(product['spf']) else -1
    spf_value = max(0, float(spf_raw)) if spf_raw != -1 else 0
    features.append(spf_value)

    has_spf_mentioned = 1 if (pd.notna(product['spf']) and float(product['spf']) >= 0) else 0
    features.append(has_spf_mentioned)

    try:
        irritating_raw = product['irritating_ingredients']
        if pd.notna(irritating_raw) and str(irritating_raw).strip():
            irritating_str = str(irritating_raw).strip()
            if irritating_str.startswith('[') and irritating_str.endswith(']'):
                irritating = ast.literal_eval(irritating_str)
            else:
                irritating = [ing.strip() for ing in irritating_str.split(',') if ing.strip()]
        else:
            irritating = []
        nr_irritating = len(irritating)
    except:
        nr_irritating = 0
    features.append(nr_irritating)

    return features


def apply_user_restrictions_rf(user_data, products, is_time, time, restrictions, nr_products):
    global rf_model, important_ingredients

    if products.empty:
        return products

    if not ensure_system_initialized():
        return apply_user_restrictions_classic(user_data, products, is_time, time, restrictions, nr_products)

    if rf_model is None or important_ingredients is None:
        return apply_user_restrictions_classic(user_data, products, is_time, time, restrictions, nr_products)

    if is_time and time:
        time = str(time).lower().strip()

        if 'time' in products.columns:
            unique_times = products['time'].unique()

            day_equivalents = ["day", "morning", "zi", "dimineata"]
            night_equivalents = ["night", "evening", "noapte", "seara"]

            if time in day_equivalents:
                mask = (
                        products['time'].str.lower().isin(['day', 'morning', 'both', 'zi', 'dimineata', '']) |
                        products['time'].isna()
                )
                products = products[mask]

            elif time in night_equivalents:
                aux=products.copy()
                mask = (
                        products['time'].str.lower().isin(['night', 'evening', 'both', 'noapte', 'seara', '']) |
                        products['time'].isna()
                )
                products = products[mask]
                if(len(products)==0):
                    products=aux.copy()
                    mask = (
                            products['time'].str.lower().isin(['day', 'morning', 'both', 'zi', 'dimineata', '']) |
                            products['time'].isna()
                    )
                    products = products[mask]

        else:
            print("Coloana 'time' nu există în dataset!")

    filtered_products = products.copy()
#######################################################################################################################################################
    user_features = prepare_user_features(user_data)

    scores = []
    for idx, product in filtered_products.iterrows():
        try:
            product_features = prepare_product_features(product)

            combined_features = user_features + product_features

            score = rf_model.predict([combined_features])[0]
            scores.append(score)
        except Exception as e:
            print(f"Eroare la predicție pentru produsul {idx}: {e}")
            scores.append(1)

    filtered_products['score'] = scores

    filtered_products = filtered_products.sort_values('score', ascending=False)

    if not filtered_products.empty:
        print(f"Top {min(nr_products, len(filtered_products))} produse:")
        for i, (idx, row) in enumerate(filtered_products.head(nr_products).iterrows()):
            time_info = row.get('time', 'N/A')
            print(f"   {i + 1}. {row.get('product_name', 'N/A')} - Scor: {row['score']:.2f} - Timp: {time_info}")

        return filtered_products.head(nr_products)
    else:
        print("Nu s-au găsit produse potrivite.")
        return pd.DataFrame()


def apply_user_restrictions_classic(user_data, products, is_time, time, restrictions, nr_products):
    print(" Folosesc algoritm clasic...")

    filtered_products = products.copy()

    if is_time and time:
        time = str(time).lower().strip()
        day_equivalents = ["day", "morning", "zi", "dimineata"]
        night_equivalents = ["night", "evening", "noapte", "seara"]

        if 'time' in products.columns:
            if time in day_equivalents:
                mask = (
                        products['time'].str.lower().isin(['day', 'morning', 'both', 'zi', 'dimineata', '']) |
                        products['time'].isna()
                )
                filtered_products = filtered_products[mask]
            elif time in night_equivalents:
                mask = (
                        products['time'].str.lower().isin(['night', 'evening', 'both', 'noapte', 'seara', '']) |
                        products['time'].isna()
                )
                filtered_products = filtered_products[mask]

    if 'rating' in filtered_products.columns:
        filtered_products['score'] = filtered_products['rating'].fillna(3.0)
    else:
        np.random.seed(42)
        filtered_products['score'] = np.random.uniform(1, 5, len(filtered_products))

    filtered_products = filtered_products.sort_values('score', ascending=False)
    return filtered_products.head(nr_products)


def read_csv():
    file_path = "./products_with_id.csv"
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


def normalize_string(text):
    if isinstance(text, str):
        return text.lower().strip().replace(" ", "")
    return str(text).lower().strip().replace(" ", "")



def getProducts(user_data, product_type, nr_products, is_time, time, area):
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

    if area is not None and area.strip() != "":
        if 'area' in filtered_products.columns:
            area_normalized = normalize_string(area)

            filtered_products['area_normalized'] = filtered_products['area'].apply(
                lambda x: normalize_string(str(x)) if pd.notna(x) else ""
            )

            area_filtered = filtered_products[
                filtered_products['area_normalized'] == area_normalized
                ].copy()

            area_filtered.drop(columns=['area_normalized'], inplace=True)
            filtered_products.drop(columns=['area_normalized'], inplace=True)

            if area_filtered.empty:
                return None
            else:
                filtered_products = area_filtered

    products_recommended = apply_user_restrictions_rf(
        user_data, filtered_products, is_time, time, restrictions, nr_products
    )

    if not products_recommended.empty:
        return products_recommended
    else:
        return None




def getProducts2(user_data, product_type, nr_products, is_time, time,area):
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

    products_recommended = apply_user_restrictions_rf(
        user_data, filtered_products, is_time, time, restrictions, nr_products
    )

    if not products_recommended.empty:
        return products_recommended
    else:
        return None


def get_product_by_id(id_product):
    file_path = "./products_with_id.csv"
    try:
        df = pd.read_csv(file_path)
        df = df.fillna('')
        product_row = df[df['id'] == id_product]

        if product_row.empty:
            print(f"Produsul cu id {id_product} nu a fost găsit.")
            return None
        else:
            return product_row
    except Exception as e:
        print(f"Error reading CSV: {e}")
        return None


def recommend_products(user_id, product_type, nr_products=1, is_time=False, time="day",area="face"):

    ensure_system_initialized()

    user_data = get_user_details_for_recommendations(int(user_id))
    products = getProducts(user_data, str(product_type).lower().strip(), nr_products, is_time,
                           str(time).lower().strip(),area)

    if products is not None and not products.empty:
        if 'score' in products.columns:
            products_clean = products.drop(columns=['score'])
        else:
            products_clean = products

        products_dict = products_clean.head(nr_products).to_dict(orient='records')

        cleaned_products = []
        for product in products_dict:
            if pd.isna(product.get('irritating_ingredients')) or product.get('irritating_ingredients') is None:
                product['irritating_ingredients'] = ""
            elif str(product.get('irritating_ingredients')).lower() == 'nan':
                product['irritating_ingredients'] = ""

            if pd.isna(product.get('clean_ingreds')) or product.get('clean_ingreds') is None:
                product['clean_ingreds'] = "[]"
            elif str(product.get('clean_ingreds')).lower() == 'nan':
                product['clean_ingreds'] = "[]"

            if pd.isna(product.get('price')) or product.get('price') is None:
                product['price'] = "0"
            elif str(product.get('price')).lower() == 'nan':
                product['price'] = "0"

            if pd.isna(product.get('spf')) or product.get('spf') is None:
                product['spf'] = 0
            elif str(product.get('spf')).lower() == 'nan':
                product['spf'] = 0

            if pd.isna(product.get('product_name')) or product.get('product_name') is None:
                product['product_name'] = ""
            elif str(product.get('product_name')).lower() == 'nan':
                product['product_name'] = ""

            if pd.isna(product.get('time')) or product.get('time') is None:
                product['time'] = ""
            elif str(product.get('time')).lower() == 'nan':
                product['time'] = ""

            if 'score' in product:
                del product['score']

            cleaned_products.append(product)

        return cleaned_products
    else:
        return None

def initialize_rf_system(model_path='./skincare_rf_model2.pkl',
                         ingredients_path='./knowledge/ingredients_for_concerns.json'):
    global _system_initialized
    success = True


    if not load_rf_model(model_path):
        print("Nu s-a putut încărca modelul RF. Se va folosi algoritmul clasic.")
        success = False

    if not extract_important_ingredients(ingredients_path):
        print("Nu s-au putut încărca ingredientele. Se va folosi algoritmul clasic.")
        success = False

    _system_initialized = True
    return success


def reset_system():
    global rf_model, important_ingredients, _system_initialized
    rf_model = None
    important_ingredients = None
    _system_initialized = False
    return initialize_rf_system()


if __name__ == "__main__":
    if initialize_rf_system():
        print("Sistem Random Forest inițializat cu succes!")
    else:
        print("Sistem va folosi algoritmul clasic.")

    print("\n Test recomandări:")
    results = recommend_products(11, "mask", 1, True, "day","eye")

    if results:
        print(f"\n Găsite {len(results)} produse:")
        for i, product in enumerate(results):
            print(f"{i + 1}. {product.get("product_type","")} - Scor: {product.get('score', 0):.2f}")
    else:
        print("Nu s-au găsit recomandări.")