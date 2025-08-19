import json

from db_connect import *
from datetime import datetime


def format_date_for_sqlite(date_obj):
    if date_obj is None:
        return None
    if isinstance(date_obj, str):
        return date_obj
    return date_obj.strftime("%Y-%m-%d")


def saveRoutineCompletion(routine_id, steps, spf_id, user_id):
    con = get_new_connection()
    cursor = con.cursor()

    try:
        if routine_id != -1:
            steps_string = ",".join(map(str, steps))
            query = "INSERT INTO routine_completions (user_id, routine_id, steps) VALUES (%s, %s, %s)"
            cursor.execute(query, (user_id, routine_id, steps_string))
        else:
            query = "INSERT INTO routine_completions (user_id, spf_id) VALUES (%s, %s)"
            cursor.execute(query, (user_id, spf_id))

        inserted_id = cursor.lastrowid

        cursor.close()

        dict_cursor = con.cursor(dictionary=True)
        select_query = "SELECT * FROM routine_completions WHERE id = %s"
        dict_cursor.execute(select_query, (inserted_id,))

        result = dict_cursor.fetchone()
        dict_cursor.close()

        con.commit()

        if result:
            if result.get('completion_date'):
                result['completion_date'] = format_date_for_sqlite(result['completion_date'])

            if result.get('steps'):
                steps_string = result['steps']
                result['steps'] = [int(step_id) for step_id in steps_string.split(',') if step_id.strip()]
            else:
                result['steps'] = []

        return result

    except Exception as e:
        con.rollback()
        print(f"Error: {e}")
        return None

    finally:
        con.close()


def getUserRoutineCompletions(user_id):
    con = get_new_connection()

    try:
        dict_cursor = con.cursor(dictionary=True)

        query = """
            SELECT * FROM routine_completions 
            WHERE user_id = %s 
            ORDER BY completion_date DESC, id DESC
        """

        dict_cursor.execute(query, (user_id,))
        results = dict_cursor.fetchall()
        dict_cursor.close()

        for result in results:
            if result.get('completion_date'):
                result['completion_date'] = format_date_for_sqlite(result['completion_date'])

            if result.get('steps'):
                steps_string = result['steps']
                result['steps'] = [int(step_id) for step_id in steps_string.split(',') if step_id.strip()]
            else:
                result['steps'] = []

        return results

    except Exception as e:
        print(f"Error getting user routine completions: {e}")
        return []

    finally:
        con.close()


def getUserTodayCompletions(user_id):
    con = get_new_connection()

    try:
        dict_cursor = con.cursor(dictionary=True)

        query = """
            SELECT * FROM routine_completions 
            WHERE user_id = %s AND completion_date = CURDATE()
            ORDER BY id DESC
        """

        dict_cursor.execute(query, (user_id,))
        results = dict_cursor.fetchall()
        dict_cursor.close()

        for result in results:
            if result.get('completion_date'):
                result['completion_date'] = format_date_for_sqlite(result['completion_date'])

            if result.get('steps'):
                steps_string = result['steps']
                result['steps'] = [int(step_id) for step_id in steps_string.split(',') if step_id.strip()]
            else:
                result['steps'] = []

        return results

    except Exception as e:
        print(f"Error getting today's completions: {e}")
        return []

    finally:
        con.close()

def saveRoutineCompletionDetails(user_id,routine_type,steps,max_steps):

    connection = get_new_connection()
    cursor = connection.cursor()
    print(routine_type)
    if("exfoliation" in str(routine_type).lower()):
        routine_type="exfoliation"
        print("cf")
    try:

        valid_types = ['morning', 'evening', 'exfoliation', 'face mask',
                       'eye mask', 'lip mask', 'spf']
        routine_type = str(routine_type).lower()
        if routine_type not in valid_types:
            raise ValueError(f"Tip rutină invalid: {routine_type}")

        if isinstance(steps, list):
            steps_json = json.dumps(steps)
        else:
            steps_json = steps

        query = """
           INSERT INTO routine_completions_details 
           (user_id, routine_type, steps, max_steps) 
           VALUES (%s, %s, %s, %s)
           """

        values = (user_id, routine_type, steps_json, max_steps)

        cursor.execute(query, values)

        connection.commit()

        record_id = cursor.lastrowid

        select_query = """
           SELECT id, completion_date, user_id, routine_type, steps, max_steps 
           FROM routine_completions_details 
           WHERE id = %s
           """
        cursor.execute(select_query, (record_id,))
        result = cursor.fetchone()

        if result:
            record_dict = {
                'id': result[0],
                'completion_date': result[1].strftime('%Y-%m-%d') if result[1] else None,
                'user_id': result[2],
                'routine_type': result[3],
                'steps': json.loads(result[4]) if result[4] else None,
                'max_steps': result[5]
            }

            print(f"Rutina a fost salvată cu succes. ID: {record_id}")
            return  record_dict
        else:
            print("Eroare la citirea înregistrării salvate")
            return  None

    except mysql.connector.Error as e:
        print(f"Eroare MySQL: {e}")
        if connection:
            connection.rollback()
        return None

    except ValueError as e:
        print(f"Eroare validare: {e}")
        return  None

    except Exception as e:
        print(f"Eroare neașteptată: {e}")
        if connection:
            connection.rollback()
        return  None

    finally:
        if cursor:
            cursor.close()
        if connection and connection.is_connected():
            connection.close()


def getRoutineCompletionsByUser(user_id):

    connection = get_new_connection()
    cursor = connection.cursor()

    try:
        query = """
        SELECT id, completion_date, user_id, routine_type, steps, max_steps 
        FROM routine_completions_details 
        WHERE user_id = %s
        ORDER BY completion_date DESC
        """

        cursor.execute(query, (user_id,))
        results = cursor.fetchall()

        routines = []
        for result in results:
            routine_dict = {
                'id': result[0],
                'completion_date': result[1].strftime('%Y-%m-%d') if result[1] else None,
                'user_id': result[2],
                'routine_type': result[3],
                'steps': json.loads(result[4]) if result[4] else None,
                'max_steps': result[5]
            }
            routines.append(routine_dict)

        return  routines

    except mysql.connector.Error as e:
        print(f"Eroare MySQL: {e}")
        return  None

    except Exception as e:
        print(f"Eroare neașteptată: {e}")
        return  None

    finally:
        if cursor:
            cursor.close()
        if connection and connection.is_connected():
            connection.close()