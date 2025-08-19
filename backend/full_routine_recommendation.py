from db_connect import get_new_connection
from routine_recommendation import recommend_routine
from product_recommendation2 import recommend_products

def recommend(user_id, routine_type, nr_products=1):
    routine = recommend_routine(user_id, routine_type)
    print(routine)
    result = []

    for typee in routine:
        area="eye" if str(typee).lower()=="eye care" else "face"
        products = recommend_products(
                user_id,
                str(typee),
                nr_products,
                True,
                "day" if routine_type == "morning" else "night",
            area
            )

        if products:
            for p in products:
                print("#",typee[0].upper()+typee[1:].lower(),p)
                elem = {
                    "step": typee[0].upper()+typee[1:].lower(),
                    "product": p
                }

                result.append(elem)

    final_result = result if routine_type == "morning" else result

    save_recommended_routine(user_id, routine_type, final_result)
    for x in final_result:
        print("step_type",type(x.get("step")))
        print(x)
    return final_result


def save_recommended_routine(user_id, routine_type, recommendations):
    con=get_new_connection()
    cursor=con.cursor()
    try:
        cursor.execute("""
            DELETE FROM recommended_routines 
            WHERE user_id = %s AND routine_type = %s
        """, (user_id, routine_type))

        for rec in recommendations:
            cursor.execute("""
                INSERT INTO recommended_routines (user_id, routine_type, step_name, product_id)
                VALUES (%s, %s, %s, %s)
            """, (
                user_id,
                routine_type,
                rec['step'],
                rec['product']['id']
            ))

        con.commit()
        return True

    except Exception as e:
        print(f"Eroare la salvarea recomandărilor: {e}")
        con.rollback()
        return False
    finally:
        cursor.close()
        con.close()

if __name__=="__main__":
    routine=recommend(7,"morning",1)
    for r in routine:
     print(r)