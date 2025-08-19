from db_connect import *
import json

from manage_daily_logs import insertDailyLog

with open("./knowledge/descriptions.json", "r") as f:
    descriptions = json.load(f)


def addRoutine(data):
    con=get_new_connection()
    user_id = data["user_id"]
    routine_type = data["routine_type"]
    notify_time = data["notify_time"]
    notify_days = data["notify_days"]
    insertDailyLog(user_id)

    cursor = con.cursor()

    query = """
        INSERT INTO routines (
            routine_type, user_id, notification_time, notification_days
        )
        VALUES (%s, %s, %s, %s)
    """
    cursor.execute(query, (routine_type, user_id, notify_time, notify_days))
    routine_id = cursor.lastrowid

    if routine_type == "morning":
        steps = ["Cleanser", "Toner", "Spray", "Serum", "Eye Care", "Moisturiser", "Sunscreen"]
    elif routine_type == "evening":
        steps = ["Makeup remover", "Cleanser", "Toner", "Spray", "Serum", "Eye Care", "Oil", "Moisturiser"]
    elif routine_type == "exfoliation":
        steps=["Exfoliator"]
    elif routine_type == "face mask":
        steps = ["Mask"]
    elif routine_type == "eye mask":
        steps=["Mask"]
    elif routine_type == "lip mask":
        steps = ["Mask"]
    else:
        steps=[]
    query = """
        INSERT INTO steps (routine_id, step_order, step_name, description)
        VALUES (%s, %s, %s, %s)
    """

    for i, step in enumerate(steps, start=1):
        desc = descriptions.get(step, "")
        cursor.execute(query, (routine_id, i, step, desc))
    con.commit()
    cursor.close()
    con.close()
    return routine_id


def deleteRoutine(data):
    con=get_new_connection()
    id_routine=data["routine_id"]
    cursor = con.cursor()

    query = """
           Delete from steps where routine_id=%s;
       """
    cursor.execute(query, (id_routine,))
    query = """
              Delete from routines where id=%s;
          """
    cursor.execute(query, (id_routine,))
    con.commit()
    cursor.close()
    con.close()

def addSpfRoutine(data):
    con=get_new_connection()
    user_id = data["user_id"]
    start_time = data["start_time"]
    end_time = data["end_time"]
    interval_minutes = data["interval_minutes"]
    active_days = data["active_days"]

    cursor = con.cursor()
    query = """
           INSERT INTO spf_routines (
               user_id,start_time, end_time, interval_minutes,active_days
           )
           VALUES (%s, %s, %s, %s,%s)
       """
    cursor.execute(query, ( user_id, start_time,end_time,interval_minutes, active_days))
    routine_id = cursor.lastrowid
    con.commit()
    cursor.close()
    con.close()
    return getSpfByUserId(user_id)


def deleteSpfRoutine(data):
    id_routine = data["id"]
    con=get_new_connection()
    cursor = con.cursor()

    query = """
               Delete from spf_routines where id=%s;
           """
    cursor.execute(query, (id_routine,))
    con.commit()
    cursor.close()
    con.close()

def getSpfByUserId(user_id):

    con=get_new_connection()
    cursor = con.cursor(dictionary=True)

    query = """
               select * from spf_routines where user_id=%s;
           """
    cursor.execute(query, (int(user_id),))
    results = cursor.fetchall()
    cursor.close()
    if not results:
        return None

    routine = results[0]
    routine["start_time"]=str(routine['start_time'])
    routine["end_time"] = str(routine['end_time'])
    routine["active_days"] = ", ".join(routine['active_days']) if isinstance(routine['active_days'], set) else str(routine['active_days'])

    return routine


def get_routines_by_user_id___(user_id):
    con=get_new_connection()
    cursor = con.cursor()

    query = """
            select * from routines join steps on routines.id=steps.routine_id where user_id=%s;
        """
    cursor.execute(query, (int(user_id),))
    results = cursor.fetchall()
    cursor.close()
    con.close()
    return results

def get_routines_by_user_id(user_id):
    con=get_new_connection()
    cursor = con.cursor(dictionary=True)
    query_routines = "SELECT * FROM routines WHERE user_id = %s"
    cursor.execute(query_routines, (user_id,))
    routines = cursor.fetchall()

    routines_dict = {}
    for r in routines:
        routines_dict[r['id']] = {
            'id': r['id'],
            'routine_type': r['routine_type'],
            'user_id': r['user_id'],
            'notification_time':  str(r['notification_time']),
            'notification_days': str(r['notification_days']),
            'steps': []
        }

    routine_ids = tuple(routines_dict.keys())
    if not routine_ids:
        cursor.close()
        return []
    if len(routine_ids) == 1:
        query_steps = "SELECT * FROM steps WHERE routine_id = %s"
        cursor.execute(query_steps, (routine_ids[0],))
    else:
        placeholders = ','.join(['%s'] * len(routine_ids))
        query_steps = f"SELECT * FROM steps WHERE routine_id IN ({placeholders})"
        cursor.execute(query_steps, routine_ids)

    steps = cursor.fetchall()

    for s in steps:
        step_obj = {
            'id': s['id'],
            'routine_id': s['routine_id'],
            'step_order': s['step_order'],
            'step_name': s['step_name'],
            'description': s['description'],
            'product_id': s['product_id']
        }
        routines_dict[s['routine_id']]['steps'].append(step_obj)

    cursor.close()
    con.close()
    for r in routines_dict.values():
        r['steps'].sort(key=lambda x: x['step_order'])

    return list(routines_dict.values())


def get_routineid_by_userid_and_type(user_id,routine_type):
    con = get_new_connection()
    cursor = con.cursor(dictionary=True)
    query_routines = "SELECT id FROM routines WHERE user_id = %s and routine_type=%s"
    cursor.execute(query_routines, (user_id,routine_type))
    routine_id = cursor.fetchone()["id"]
    cursor.close()
    con.close()
    if(routine_id):
        routines=get_routine_by_routine_id(routine_id)
        if(routines):
         return routines
        else:
         return -1

def get_routine_by_routine_id(routine_id):
    con=get_new_connection()
    cursor = con.cursor(dictionary=True)
    query_routines = "SELECT * FROM routines WHERE id = %s"
    cursor.execute(query_routines, (routine_id,))
    routines = cursor.fetchall()

    routines_dict = {}
    for r in routines:
        routines_dict[r['id']] = {
            'id': r['id'],
            'routine_type': r['routine_type'],
            'user_id': r['user_id'],
            'notification_time':  str(r['notification_time']),
            'notification_days': str(r['notification_days']),
            'steps': []
        }

    routine_ids = tuple(routines_dict.keys())
    if not routine_ids:
        cursor.close()
        return []

    query_steps = f"SELECT * FROM steps WHERE routine_id =%s"
    cursor.execute(query_steps,(routine_id,))
    steps = cursor.fetchall()

    for s in steps:
        step_obj = {
            'id': s['id'],
            'routine_id': s['routine_id'],
            'step_order': s['step_order'],
            'step_name': s['step_name'],
            'description': s['description'],
            'product_id': s['product_id']
        }
        routines_dict[s['routine_id']]['steps'].append(step_obj)

    cursor.close()
    con.close()
    for r in routines_dict.values():
        r['steps'].sort(key=lambda x: x['step_order'])

    return list(routines_dict.values())[0]


def deleteStepById(step_id):
    con = get_new_connection()
    try:
        cursor = con.cursor()

        cursor.execute("SELECT routine_id FROM steps WHERE id = %s", (step_id,))
        result = cursor.fetchone()
        if not result:
            print(f"Step with id {step_id} not found.")
            return False

        routine_id = result[0]

        cursor.execute("DELETE FROM steps WHERE id = %s", (step_id,))
        con.commit()

        cursor.execute("""
            SELECT id FROM steps
            WHERE routine_id = %s
            ORDER BY step_order
        """, (routine_id,))
        steps = cursor.fetchall()

        for new_order, (step_id,) in enumerate(steps, start=1):
            cursor.execute("""
                UPDATE steps
                SET step_order = %s
                WHERE id = %s
            """, (new_order, step_id))

        con.commit()
        return True

    except Exception as e:
        print(f"Error deleting step {step_id}: {e}")
        con.rollback()
    finally:
        cursor.close()
        con.close()

    return False


def addStep(routine_id, step_name, product_id=None):
    con = get_new_connection()
    try:
        cursor = con.cursor(dictionary=True)

        desc = descriptions.get(step_name, "")

        cursor.execute("SELECT COALESCE(MAX(step_order), 0) + 1 AS next_order FROM steps WHERE routine_id = %s", (routine_id,))
        next_order = cursor.fetchone()["next_order"]

        cursor.execute("""
            INSERT INTO steps (routine_id, step_order, step_name, description, product_id)
            VALUES (%s, %s, %s, %s, %s)
        """, (routine_id, next_order, step_name, desc, product_id))

        inserted_id = cursor.lastrowid

        cursor.execute("SELECT * FROM steps WHERE id = %s", (inserted_id,))
        step_row = cursor.fetchone()

        con.commit()
        return step_row
    except Exception as e:
        print(f"Error adding step: {e}")
        con.rollback()
        return None
    finally:
        cursor.close()
        con.close()


def modifyStepPosition(step_id, new_poz, routine_id):
    try:
        con = get_new_connection()
        cursor = con.cursor(dictionary=True)

        cursor.execute("SELECT step_order FROM steps WHERE id = %s;", (step_id,))
        current = cursor.fetchone()
        if not current:
            raise Exception("Step not found.")
        current_poz = current['step_order']

        if current_poz < new_poz:
            cursor.execute("""
                UPDATE steps 
                SET step_order = step_order - 1 
                WHERE routine_id = %s AND step_order > %s AND step_order <= %s;
            """, (routine_id, current_poz, new_poz))
        elif current_poz > new_poz:
            cursor.execute("""
                UPDATE steps 
                SET step_order = step_order + 1 
                WHERE routine_id = %s AND step_order >= %s AND step_order < %s;
            """, (routine_id, new_poz, current_poz))

        cursor.execute("UPDATE steps SET step_order = %s WHERE id = %s;", (new_poz, step_id))

        con.commit()
        return True

    except Exception as e:
        print(f"Error updating step position: {e}")
        return False

    finally:
        cursor.close()
        con.close()



def modifyNotifyTime(routine_id,time,days):

        try:
            clean_days = days.replace("{", "").replace("}", "").replace("'", "")
            days_list = [day.strip() for day in clean_days.split(",") if day.strip()]
            days_set = ",".join(days_list)
            query = """
            UPDATE routines 
            SET notification_time = %s, notification_days = %s 
            WHERE id = %s
            """
            con=get_new_connection()
            cursor = con.cursor()
            cursor.execute(query, (time, days_set, routine_id))

            con.commit()

            if cursor.rowcount > 0:
                return True
            else:
                return True

        except Exception as e:
            con.rollback()
            return False
        finally:
            if 'cursor' in locals():
                cursor.close()
                con.close()


def modifySpfNotifyTime(routine_id, start_time,end_time,interval_minutes,days):
    try:
        con = get_new_connection()
        cursor = con.cursor()

        if days:
            days_list = [day.strip().lower() for day in days.split(',')]
            valid_days = ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']
            clean_days = [day for day in days_list if day in valid_days]
            active_days = ','.join(clean_days)
        else:
            active_days = ''

        query = """
               UPDATE spf_routines 
               SET start_time = %s, end_time = %s, interval_minutes = %s, active_days = %s
               WHERE id = %s
           """

        cursor.execute(query, (start_time, end_time, interval_minutes, active_days, routine_id))
        con.commit()
        cursor.close()
        con.close()
        return True

    except Exception as e:
        return False

def addProductToStep(step_id,product_id):
    con=get_new_connection()
    cursor=con.cursor()
    if(product_id<0):
     query = """
               UPDATE steps
               SET product_id = %s
               WHERE id = %s
           """

     cursor.execute(query, (None,step_id))
    else:
        query = """
                       UPDATE steps
                       SET product_id = %s
                       WHERE id = %s
                   """

        cursor.execute(query, (product_id, step_id))
    con.commit()
    cursor.close()
    con.close()
    return True

def addProductToSpf(id,product_id):
    con=get_new_connection()
    cursor=con.cursor()
    if(product_id<0):
     query = """
               UPDATE spf_routines
               SET product_id = %s
               WHERE id = %s
           """

     cursor.execute(query, (None,id))
    else:
        query = """
                       UPDATE spf_routines
                       SET product_id = %s
                       WHERE id = %s
                   """

        cursor.execute(query, (product_id, id))
    con.commit()
    cursor.close()
    con.close()
    return True

def deleteRecommendedRoutine(user_id ,routine_type):
    con = get_new_connection()
    cursor = con.cursor()
    try:
        cursor.execute("""
                DELETE FROM recommended_routines 
                WHERE user_id = %s AND routine_type = %s
            """, (user_id, routine_type))

        con.commit()
        return True

    except Exception as e:
        con.rollback()
        return False
    finally:
        cursor.close()
        con.close()


def addRecommendedRoutine(user_id, routine_type):
    con = get_new_connection()
    cursor = con.cursor()

    with open("./knowledge/descriptions.json", "r") as f:
        descriptions = json.load(f)

    try:
        query = """
            SELECT id FROM routines WHERE user_id=%s AND routine_type=%s
        """
        cursor.execute(query, (user_id, routine_type))
        result = cursor.fetchone()

        if result:
            routine_id = result[0]

            query = """
                DELETE FROM steps WHERE routine_id=%s
            """
            cursor.execute(query, (routine_id,))

            query = """
                SELECT * FROM recommended_routines WHERE user_id=%s AND routine_type=%s
            """
            cursor.execute(query, (user_id, routine_type))
            result2 = cursor.fetchall()

            if result2:
                step_order = 1

                for row in result2:
                    step_name = row[3]
                    product_id = row[4]

                    description = descriptions.get(step_name, "")

                    insert_query = """
                        INSERT INTO steps (routine_id, step_order, step_name, description, product_id)
                        VALUES (%s, %s, %s, %s, %s)
                    """
                    cursor.execute(insert_query, (routine_id, step_order, step_name, description, product_id))

                    step_order += 1

                con.commit()
                return True
            else:
                print("Nu s-au găsit rutine recomandate")
                return False
        else:
            print("Nu s-a găsit rutina")
            return False

    except Exception as e:
        print(f"Eroare: {e}")
        con.rollback()
        return False
    finally:
        cursor.close()
        con.close()



if __name__ == "__main__":
   print(getSpfByUserId(14))


