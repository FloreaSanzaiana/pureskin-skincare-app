from db_connect import *


def deleteDailyLogByUserId(user_id):
    con = get_new_connection()
    cursor = con.cursor()
    query = """DELETE FROM daily_logs WHERE user_id = %s"""
    cursor.execute(query, (user_id,))
    con.commit()
    cursor.close()
    con.close()
    return True


def insertDailyLog(user_id):
    con = get_new_connection()
    cursor = con.cursor()

    try:
        query = """INSERT IGNORE INTO daily_logs (user_id) VALUES (%s)"""
        cursor.execute(query, (user_id,))

        con.commit()

        cursor.execute("SELECT * FROM daily_logs WHERE user_id = %s", (user_id,))
        result = cursor.fetchone()

        if result:
            return {
                'id': result[0],
                'user_id': result[1]
            }
        else:
            return None

    except Exception as e:
        con.rollback()
        print(f"Error: {e}")
        return None
    finally:
        cursor.close()
        con.close()


def getDailyLogWithContentsByUserId(user_id):
    con = get_new_connection()
    cursor = con.cursor(dictionary=True)

    try:
        cursor.execute("SELECT * FROM daily_logs WHERE user_id = %s", (user_id,))
        daily_log = cursor.fetchone()

        if not daily_log:
            return None

        query = """
            SELECT * FROM daily_log_content 
            WHERE daily_log_id = %s
            ORDER BY log_date DESC
        """

        cursor.execute(query, (daily_log['id'],))
        contents = cursor.fetchall()

        for content in contents:
            if content['log_date']:
                content['log_date'] = content['log_date'].strftime('%Y-%m-%d')

        daily_log['contents'] = contents

        return daily_log

    except Exception as e:
        print(f"Error getting daily log with contents: {e}")
        return None
    finally:
        cursor.close()
        con.close()




def insertDailyLogWithContent(daily_log_id, skin_feeling_score, skin_condition, notes, weather, stress_level):
    try:

        connection = get_new_connection()
        cursor = connection.cursor(dictionary=True)

        insert_query = """
        INSERT INTO daily_log_content 
        (daily_log_id, skin_feeling_score, skin_condition, notes, weather, stress_level) 
        VALUES (%s, %s, %s, %s, %s, %s)
        """

        notes_value = notes if notes and notes.strip() else None
        weather_value = weather if weather else None
        skin_condition = skin_condition if skin_condition else None

        cursor.execute(insert_query, (
            daily_log_id,
            skin_feeling_score,
            skin_condition,
            notes_value,
            weather_value,
            stress_level
        ))
        connection.commit()

        cursor.execute("""
        SELECT id, daily_log_id, skin_feeling_score, skin_condition, 
               notes, weather, stress_level, log_date 
        FROM daily_log_content 
        WHERE id = %s
        """, (cursor.lastrowid,))
        x=cursor.fetchone()
        x['log_date'] = x['log_date'].strftime('%Y-%m-%d')
        print(x)
        return  x

    except mysql.connector.Error as error:
        return None

    except Exception as error:
        return None

    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()


if __name__=="__main__":
    print("hei")
    pass