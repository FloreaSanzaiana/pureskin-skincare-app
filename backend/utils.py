import ast
import csv
import re

import pandas as pd

from db_connect import *





def convert_image_to_blob(image_path):
    with open(image_path, 'rb') as file:
        binary_data = file.read()
    return binary_data

def insert_to_db(image_path, user_id, varsta, sex, skin_type,
    skin_sensitivity, skin_phototype, concerns):
    image_bytes = convert_image_to_blob(image_path)
    cursor = conn.cursor()
    query = """
           INSERT INTO user_details (
               id_user, varsta, sex, skin_type, skin_sensitivity,
               skin_phototype, concerns, poza_profil
           )
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
       """

    values = (
        user_id,
        varsta,
        sex,
        skin_type,
        skin_sensitivity,
        skin_phototype,
        concerns,
        image_bytes
    )

    cursor.execute(query, values)
    conn.commit()
    cursor.close()
    print("Imagine salvată în baza de date.")

def get_user_details(id):
    con=get_new_connection()
    cursor = con.cursor(dictionary=True)
    cursor.execute("SELECT username,email,varsta,sex,skin_type,skin_sensitivity,skin_phototype,concerns,poza_profil FROM users join user_details on users.id=user_details.id_user where id = %s", (id,))
    user = cursor.fetchall()
    print(user)
    cursor.close()
    con.close()
    return (user[0])


def deleteUserWithoutDetails(email):
    con = get_new_connection()
    cursor = con.cursor(dictionary=True)

    try:
        cursor.execute(
            "SELECT id FROM users WHERE email = %s",
            (email,)
        )
        user = cursor.fetchall()

        if user:
            user_id = user[0]["id"]

            cursor.execute(
                "SELECT * FROM user_details WHERE id_user = %s",
                (user_id,)
            )
            user_details = cursor.fetchall()

            if not user_details:
                cursor.execute(
                    "DELETE FROM users WHERE id = %s",
                    (user_id,)
                )
                con.commit()
                print(f"User with email {email} deleted successfully")
            else:
                print(f"User with email {email} has details, not deleted")
        else:
            print(f"User with email {email} not found")

    except Exception as e:
        print(f"Error: {e}")
        con.rollback()
    finally:
        cursor.close()
        con.close()

def get_user_details_for_recommendations(id):
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT varsta,sex,skin_type,skin_sensitivity,skin_phototype,concerns FROM users join user_details on users.id=user_details.id_user where id = %s", (id,))
    user = cursor.fetchall()
    cursor.close()
    return (user[0])


def detect_area_and_time(name: str, type_: str) -> dict:
    text = f"{name} {type_}".lower()

    area = "face"
    time = "day"

    if "lip" in text:
        area = "lip"
    elif "eye" in text:
        area = "eye"

    if "night" in text:
        time = "night"

    return {"area": area, "time": time}

def update_csv(input_file, output_file):
    with open(input_file, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        fieldnames = reader.fieldnames + ['area', 'time']

        rows = []
        for row in reader:
            name = row["product_name"]
            type_ = row["product_type"]
            area_and_time = detect_area_and_time(name, type_)

            row.update(area_and_time)
            rows.append(row)

    with open(output_file, mode='w', newline='', encoding='utf-8') as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)



def detect_spf(name: str, type_: str, ingredients: str) -> str:
    text = f"{name} {type_} {ingredients}".lower()

    if "no spf" in text or "without spf" in text:
        return "-1"

    match = re.search(r'spf[\s\-:]?(\d{1,3})\+?', text)
    if match:
        value = match.group(1)

        if not re.search(rf'{value}\s*(ml|g|gram|grams)', text):
            return value

    if "spf" in text:
        return "0"

    return "-1"


def add_spf_column(input_file, output_file):
    with open(input_file, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        fieldnames = reader.fieldnames + ['spf']

        updated_rows = []
        for row in reader:
            name = row.get("product_name", "")
            type_ = row.get("product_type", "")
            ingredients = row.get("ingredients", "")

            spf_value = detect_spf(name, type_, ingredients)
            row["spf"] = spf_value
            updated_rows.append(row)

    with open(output_file, mode='w', newline='', encoding='utf-8') as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(updated_rows)






products = [
    {
        "product_name": "La Roche-Posay Anthelios UVMune 400 Invisible Fluid SPF 50+",
        "product_url": "https://www.laroche-posay.sg/anthelios/anthelios-uvmune-400-invisible-fluid-spf50-plus-non-perfumed",
        "product_type": "Sunscreen",
        "clean_ingreds": "ethylhexyl salicylate, bis-ethylhexyloxyphenol methoxyphenyl triazine, butyl methoxydibenzoylmethane",
        "price": 18.00,
        "id": 763,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Vichy Capital Soleil Mattifying Face Fluid SPF 50",
        "product_url": "https://www.vichy.eg/en/Sun-care/SPF-50-Mattifying-Face-Fluid-Dry-Touch-CAPITAL-SOLEIL/p25459.aspx",
        "product_type": "Sunscreen",
        "clean_ingreds": "aqua/water, glycerin, ethylhexyl salicylate, titanium dioxide",
        "price": 14.00,
        "id": 764,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Bioderma Photoderm Max Spray SPF 50+",
        "product_url": "https://incidecoder.com/products/bioderma-photoderm-max-very-high-protection-spray-spf50",
        "product_type": "Sunscreen",
        "clean_ingreds": "ectoin, ethylhexyl triazone, bis-ethylhexyloxyphenol methoxyphenyl triazine",
        "price": 23.00,
        "id": 765,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Eucerin Sensitive Protect Sun Fluid SPF 50+",
        "product_url": "https://www.eucerin.co.uk/products/sun-protection/sun-fluid-sensitive-protect-spf-50plus",
        "product_type": "Sunscreen",
        "clean_ingreds": "licochalcone a, glycyrrhetinic acid, uv filters",
        "price": 15.00,
        "id": 766,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Garnier Ambre Solaire Super UV Invisible Face Serum SPF 50+",
        "product_url": "https://incidecoder.com/products/garnier-ambre-solaire-spf-50-super-uv-invisible-face-serum",
        "product_type": "Sunscreen",
        "clean_ingreds": "tocopherol, glycerin, ceramide np, uv filters",
        "price": 9.00,
        "id": 767,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "SkinCeuticals Physical Fusion UV Defense SPF 50",
        "product_url": "https://www.skinceuticals.com/skincare/sunscreens/physical-fusion-uv-defense-sunscreen-spf-50/S54.html",
        "product_type": "Sunscreen",
        "clean_ingreds": "zinc oxide (5%), titanium dioxide (6%)",
        "price": 87.00,
        "id": 768,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Clinique Superdefense City Block SPF 50",
        "product_url": "https://www.clinique.com/product/1661/72241/sun-protection-self-tanners/sun-protection/superdefensetm-city-block-broad-spectrum-spf-50-daily-energy-face-protector",
        "product_type": "Sunscreen",
        "clean_ingreds": "titanium dioxide, zinc oxide, octinoxate",
        "price": 32.00,
        "id": 769,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Neutrogena Ultra Sheer Dry-Touch SPF 55",
        "product_url": "https://www.neutrogena.com/products/sun/ultra-sheer-dry-touch-sunscreen-broad-spectrum-spf-55/6868790",
        "product_type": "Sunscreen",
        "clean_ingreds": "avobenzone, octocrylene, oxybenzone",
        "price": 27.00,
        "id": 770,
        "area": "face",
        "time": "day",
        "spf": 55
    },
    {
        "product_name": "Augustinus Bader The Sunscreen SPF 50",
        "product_url": "https://augustinusbader.com/us/en/the-mineral-sunscreen-spf-50",
        "product_type": "Sunscreen",
        "clean_ingreds": "zinc oxide, tfc8®, raspberry seed oil",
        "price": 136.00,
        "id": 771,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Avène Very High Protection Spray SPF 50+",
        "product_url": "https://incidecoder.com/products/avene-avene-very-high-protection-spray-sun-cream-spf50",
        "product_type": "Sunscreen",
        "clean_ingreds": "avene thermal spring water, uv filters, tocopherol",
        "price": 17.00,
        "id": 772,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Neutrogena Beach Defense Sunscreen SPF 70",
        "product_url": "https://www.neutrogena.com/products/sun/beach-defense-sunscreen-broad-spectrum-spf-70/6880830",
        "product_type": "Sunscreen",
        "clean_ingreds": "avobenzone, homosalate, octocrylene, octinoxate",
        "price": 15.00,
        "id": 773,
        "area": "body",
        "time": "day",
        "spf": 70
    },
    {
        "product_name": "Banana Boat Ultra Sport Sunscreen Lotion SPF 50",
        "product_url": "https://www.bananaboat.com/products/ultra-sport-sunscreen-lotion-spf-50",
        "product_type": "Sunscreen",
        "clean_ingreds": "avobenzone, octocrylene, homosalate",
        "price": 9.00,
        "id": 774,
        "area": "body",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Coppertone Sport Sunscreen SPF 50",
        "product_url": "https://www.coppertone.com/products/sport-sunscreen-lotion-spf-50",
        "product_type": "Sunscreen",
        "clean_ingreds": "octinoxate, octocrylene, avobenzone",
        "price": 10.00,
        "id": 775,
        "area": "body",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Hawaiian Tropic Silk Hydration Sunscreen SPF 30",
        "product_url": "https://www.hawaiiantropic.com/products/silk-hydration-sunscreen-lotion-spf-30",
        "product_type": "Sunscreen",
        "clean_ingreds": "octinoxate, oxybenzone, avobenzone",
        "price": 12.00,
        "id": 776,
        "area": "body",
        "time": "day",
        "spf": 30
    },
    {
        "product_name": "Supergoop! Unseen Sunscreen SPF 40",
        "product_url": "https://supergoop.com/products/unseen-sunscreen-spf-40",
        "product_type": "Sunscreen",
        "clean_ingreds": "avobenzone, homosalate, octocrylene",
        "price": 34.00,
        "id": 777,
        "area": "face",
        "time": "day",
        "spf": 40
    },
    {
        "product_name": "Shiseido Ultimate Sun Protection Lotion SPF 50+",
        "product_url": "https://www.shiseido.com/us/en/sun-protection/sun-protection-lotion-spf-50-plus",
        "product_type": "Sunscreen",
        "clean_ingreds": "octinoxate, avobenzone, homosalate",
        "price": 47.00,
        "id": 778,
        "area": "body",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Kiehl's Ultra Light Daily UV Defense Sunscreen SPF 50",
        "product_url": "https://www.kiehls.com/sun-care/ultra-light-daily-uv-defense-spf-50/KHL463.html",
        "product_type": "Sunscreen",
        "clean_ingreds": "avobenzone, homosalate, octinoxate",
        "price": 38.00,
        "id": 779,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "Dr. Dennis Gross Sheer Mineral Sunscreen SPF 30",
        "product_url": "https://drdennisgross.com/collections/sunscreen/products/sheer-mineral-sunscreen-spf-30",
        "product_type": "Sunscreen",
        "clean_ingreds": "zinc oxide, titanium dioxide",
        "price": 42.00,
        "id": 780,
        "area": "face",
        "time": "day",
        "spf": 30
    },
    {
        "product_name": "Tizo 3 Facial Mineral Sunscreen SPF 40",
        "product_url": "https://www.tizo.com/products/tizo-3-facial-sunscreen-spf-40",
        "product_type": "Sunscreen",
        "clean_ingreds": "zinc oxide, titanium dioxide",
        "price": 36.00,
        "id": 781,
        "area": "face",
        "time": "day",
        "spf": 40
    },
    {
        "product_name": "Alastin Skincare Restorative Skin Complex SPF 50",
        "product_url": "https://www.alastin.com/products/restorative-skin-complex",
        "product_type": "Sunscreen",
        "clean_ingreds": "octinoxate, homosalate, octocrylene",
        "price": 110.00,
        "id": 782,
        "area": "face",
        "time": "day",
        "spf": 50
    },
    {
        "product_name": "EltaMD UV Clear Broad-Spectrum SPF 46",
        "product_url": "https://eltamd.com/products/uv-clear-broad-spectrum-spf-46",
        "product_type": "Sunscreen",
        "clean_ingreds": "niacinamide, zinc oxide, octinoxate",
        "price": 36.00,
        "id": 783,
        "area": "face",
        "time": "day",
        "spf": 46
    }
]

def addSpf():
    existing_df = pd.read_csv('old_datasets/spf_updated_products.csv')
    new_products_df = pd.DataFrame(products)

    updated_df = pd.concat([existing_df, new_products_df], ignore_index=True)

    updated_df.to_csv('products___.csv', index=False)

import csv

def remove_rows_by_type(input_file, output_file, type_to_remove):
    with open(input_file, newline='', encoding='utf-8') as infile:
        reader = csv.DictReader(infile)
        fieldnames = reader.fieldnames

        filtered_rows = [row for row in reader if row['product_type'].lower() != type_to_remove.lower()]

    with open(output_file, mode='w', newline='', encoding='utf-8') as outfile:
        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(filtered_rows)



def getTypes():
    df = pd.read_csv("knowledge/products.csv")

    print(df.head())

    valori_unice = df['product_type'].unique()

    print(valori_unice)

import pandas as pd
import ast

import pandas as pd
import ast
import re

import pandas as pd
import ast
import re

def getIngredients():
    df = pd.read_csv('knowledge/products.csv')

    def parse_ingrediente(val):
        if pd.isna(val):
            return []
        try:
            return ast.literal_eval(val)
        except:
            return [i.strip() for i in val.split(',') if i.strip()]

    def normalize_text(text):
        return re.sub(r'[^\w\s]', '', text.lower()).strip()

    df['clean_ingreds'] = df['clean_ingreds'].apply(parse_ingrediente)

    ingrediente_unice = set()
    ingredient_map = {}
    for lista in df['clean_ingreds']:
        for ingredient in lista:
            normalized_ingredient = normalize_text(ingredient)
            if normalized_ingredient not in ingrediente_unice:
                ingrediente_unice.add(normalized_ingredient)
                ingredient_map[normalized_ingredient] = ingredient  # păstrează varianta originală

    lista_finala = sorted(list(ingrediente_unice))

    with open('knowledge/ingredients.txt', 'w', encoding='utf-8') as f:
        for ingredient in lista_finala:
            f.write(ingredient_map[ingredient] + '\n')

    print(f"Ingrediente unice găsite: {len(lista_finala)}")





if __name__=="__main__":
    print(get_user_details_for_recommendations(11))
    #getIngredients()
    #remove_rows_by_type("final_products_without_new_products.csv", "old_datasets/testing_products.csv", "Bath Oil")
    #getTypes()
    #getTypes()
    #addSpf()
    #add_spf_column('updated_products.csv', 'spf_updated_products.csv')
    #update_csv('original_products____.csv', 'updated_products.csv')
 #insert_to_db("./static/moon.png",14,20,"Female","Normal Skin",'Somewhat sensitive','Pale white skin','Dullness')