package com.example.myapplication2.data.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import android.content.Context
import android.util.Log

class DatabaseManager(context:Context):SQLiteOpenHelper(context,Database_Name,null,Database_Version) {

    companion object{
        private const val Database_Name = "pureskindb.db"
        private const val Database_Version=1

        const val Table_Routines ="routines"
        const val Table_Steps="steps"
        const val Table_Spf="spf_routines"
        const val Table_Products="products"
        const val Table_Messages="messages"
        const val Table_DailyLogs="daily_logs"
        const val Table_RoutineCompletions="routine_completions"
        const val Table_DailyLogContent="daily_log_content"
        const val Table_RoutineCompletionsDetails = "routine_completions_details"
        const val Table_Quotes="quotes"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createRoutines = """
            CREATE TABLE $Table_Routines (
                id INTEGER PRIMARY KEY ,
                routine_type TEXT NOT NULL,
                user_id INTEGER NOT NULL,
                notification_time TEXT NOT NULL,
                notification_days TEXT NOT NULL
            )
        """.trimIndent()

        val createSteps = """
            CREATE TABLE $Table_Steps (
                id INTEGER PRIMARY KEY,
                routine_id INTEGER NOT NULL,
                step_order INTEGER NOT NULL,
                step_name TEXT NOT NULL,
                description TEXT,
                product_id INTEGER ,
                FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE
            )
        """.trimIndent()

        val createSpf = """
            CREATE TABLE $Table_Spf (
                 id INTEGER PRIMARY KEY,
                 user_id INTEGER NOT NULL,
                 start_time TEXT NOT NULL,         
                 end_time TEXT NOT NULL,          
                 interval_minutes INTEGER NOT NULL,    
                 active_days TEXT NOT NULL,
                 product_id INTEGER 
            )
        """.trimIndent()

        val createProducts = """
            CREATE TABLE $Table_Products (
                  id INTEGER PRIMARY KEY,
                  name TEXT NOT NULL,
                  type TEXT NOT NULL,
                  ingredients TEXT NOT NULL,
                  area TEXT NOT NULL,
                  time TEXT NOT NULL,
                  spf INTEGER,
                  url TEXT NOT NULL,
                  price TEXT NOT NULL,
                  irritating_ingredients TEXT NOT NULL
            )
        """.trimIndent()

        val createMessages = """
            CREATE TABLE $Table_Messages (
                  message_id INTEGER PRIMARY KEY,
                 user_id INTEGER NOT NULL,
                 sender TEXT NOT NULL,
                  text TEXT NOT NULL,
                  timestamp TEXT NOT NULL
            )
        """.trimIndent()

        val createDailyLogs = """
    CREATE TABLE $Table_DailyLogs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        UNIQUE(user_id)
    )
""".trimIndent()

        val createDailyLogContent = """
    CREATE TABLE $Table_DailyLogContent (
        id INTEGER PRIMARY KEY ,
        daily_log_id INTEGER NOT NULL,
        skin_feeling_score INTEGER, 
        skin_condition TEXT ,
        notes TEXT,
        weather TEXT , 
        stress_level INTEGER,
        log_date DATE NOT NULL DEFAULT (DATE('now','localtime')),
        FOREIGN KEY (daily_log_id) REFERENCES daily_logs(id) ON DELETE CASCADE
    )
""".trimIndent()

        val createRoutineCompletions = """
    CREATE TABLE $Table_RoutineCompletions (
         id INTEGER PRIMARY KEY ,
        user_id INTEGER NOT NULL,
        completion_date DATE NOT NULL DEFAULT (DATE('now','localtime')),
        routine_id INTEGER, 
        step_id TEXT,
        spf_id INTEGER
    )
""".trimIndent()

        val createRoutineCompletionsDetails = """
    CREATE TABLE $Table_RoutineCompletionsDetails (
        id INTEGER PRIMARY KEY,
        completion_date DATE NOT NULL DEFAULT (DATE('now','localtime')),
        user_id INTEGER NOT NULL,
        routine_type TEXT NOT NULL,
        steps TEXT,
        max_steps INTEGER
    )
""".trimIndent()

        val createTableQoutes="""
             CREATE TABLE $Table_Quotes (
        id  INTEGER PRIMARY KEY AUTOINCREMENT,
        quote TEXT,
        autor TEXT
    )
            """.trimIndent()

        db.execSQL(createRoutines)
        db.execSQL(createSteps)
        db.execSQL(createSpf)
        db.execSQL(createProducts)
        db.execSQL(createMessages)
        db.execSQL(createRoutineCompletions)
        db.execSQL(createDailyLogContent)
        db.execSQL(createDailyLogs)
        db.execSQL(createRoutineCompletionsDetails)
        db.execSQL(createTableQoutes)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $Table_Routines")
        db.execSQL("DROP TABLE IF EXISTS $Table_Steps")
        db.execSQL("DROP TABLE IF EXISTS $Table_Spf")
        db.execSQL("DROP TABLE IF EXISTS $Table_Products")
        db.execSQL("DROP TABLE IF EXISTS $Table_Messages")
        db.execSQL("DROP TABLE IF EXISTS $Table_RoutineCompletions")
        db.execSQL("DROP TABLE IF EXISTS $Table_DailyLogContent")
        db.execSQL("DROP TABLE IF EXISTS $Table_DailyLogs")
        db.execSQL("DROP TABLE IF EXISTS $Table_RoutineCompletionsDetails")
        db.execSQL("DROP TABLE IF EXISTS $Table_Quotes")

        onCreate(db)
    }

    fun insertRoutine(routine: UserRoutines): Long{
        val db=writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("id", routine.id)
                put("routine_type", routine.routine_type)
                put("user_id", routine.user_id)
                put("notification_time", routine.notification_time)
                put("notification_days", routine.notification_days)
            }
            db.insertWithOnConflict(Table_Routines, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            for (s in routine.steps) {
                val value = ContentValues().apply {
                    put("id", s.id)
                    put("routine_id", s.routine_id)
                    put("step_order", s.step_order)
                    put("step_name", s.step_name)
                    put("description", s.description)
                    put("product_id", s.product_id)
                }
                db.insertWithOnConflict(Table_Steps, null, value, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        }finally {
            db.endTransaction()
        }
        return routine.id.toLong()
    }

    fun insertSpf(routine: SpfRoutine): Long{
        val db=writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("id", routine.id)
                put("user_id", routine.user_id)
                put("start_time", routine.start_time)
                put("end_time", routine.end_time)
                put("interval_minutes", routine.interval_minutes)
                put("active_days",routine.active_days)
                put("product_id",routine.product_id)
            }
            db.insertWithOnConflict(Table_Spf, null, values, SQLiteDatabase.CONFLICT_REPLACE)

            db.setTransactionSuccessful()
        }finally {
            db.endTransaction()
        }
        return routine.id.toLong()
    }
    fun getSpf(): SpfRoutine? {
        val db = readableDatabase

        val query = "SELECT * FROM spf_routines LIMIT 1;"
        val cursor = db.rawQuery(query, null)

        var routine: SpfRoutine? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
            val start_time = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
            val end_time = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))
            val interval_minutes = cursor.getInt(cursor.getColumnIndexOrThrow("interval_minutes"))
            val active_days = cursor.getString(cursor.getColumnIndexOrThrow("active_days"))
            val product_id = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"))

            routine = SpfRoutine(id, user_id, start_time, end_time, interval_minutes, active_days, product_id)
        }

        cursor.close()
        return routine
    }


    fun getRoutine(): List<UserRoutines>{
        val routines=mutableListOf<UserRoutines>()
        val db=readableDatabase

        val query="""
            Select * from routines;
        """.trimIndent()
        val cursor=db.rawQuery(query,null)
        if(cursor.moveToFirst()){
            do{
                val id=cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val routine_type=cursor.getString(cursor.getColumnIndexOrThrow("routine_type"))
                val user_id=cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                val notification_time=cursor.getString(cursor.getColumnIndexOrThrow("notification_time"))
                val notification_days=cursor.getString(cursor.getColumnIndexOrThrow("notification_days"))
                val step=Step(0,0,0,"","",0)
                val list: MutableList<Step> = mutableListOf()
                list.add(step)
                val routine= UserRoutines(id,routine_type,user_id,notification_time,notification_days,list)
                routines.add(routine)
            }while(cursor.moveToNext())
        }
        cursor.close()
        return routines
    }
    fun deleteAllRoutinesAndSteps() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(Table_Steps, null, null)
            db.delete(Table_Routines, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    fun deleteSpfRoutines() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(Table_Spf, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteRoutineById(routineId: Int) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(Table_Steps, "routine_id = ?", arrayOf(routineId.toString()))

            db.delete(Table_Routines, "id = ?", arrayOf(routineId.toString()))

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getRoutineByType(routineType: String): UserRoutines? {
        val db = readableDatabase
        var routine: UserRoutines? = null

        val query = """
        SELECT * FROM routines WHERE routine_type = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(routineType))

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val routine_type = cursor.getString(cursor.getColumnIndexOrThrow("routine_type"))
            val user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
            val notification_time = cursor.getString(cursor.getColumnIndexOrThrow("notification_time"))
            val notification_days = cursor.getString(cursor.getColumnIndexOrThrow("notification_days"))

            val steps = mutableListOf<Step>()
            val stepCursor = db.rawQuery(
                "SELECT * FROM steps WHERE routine_id = ? ORDER BY step_order ASC",
                arrayOf(id.toString())
            )

            if (stepCursor.moveToFirst()) {
                do {
                    val stepId = stepCursor.getInt(stepCursor.getColumnIndexOrThrow("id"))
                    val routineId = stepCursor.getInt(stepCursor.getColumnIndexOrThrow("routine_id"))
                    val order = stepCursor.getInt(stepCursor.getColumnIndexOrThrow("step_order"))
                    val name = stepCursor.getString(stepCursor.getColumnIndexOrThrow("step_name"))
                    val description = stepCursor.getString(stepCursor.getColumnIndexOrThrow("description"))
                    val productId = stepCursor.getInt(stepCursor.getColumnIndexOrThrow("product_id"))

                    val step = Step(stepId, routineId, order, name, description ?: "", productId)
                    steps.add(step)
                } while (stepCursor.moveToNext())
            }
            stepCursor.close()

            routine = UserRoutines(id, routine_type, user_id, notification_time, notification_days, steps)
        }

        cursor.close()
        return routine
    }

    fun deleteStepById(id: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val cursor = db.rawQuery("SELECT routine_id FROM steps WHERE id = ?", arrayOf(id.toString()))
            if (!cursor.moveToFirst()) {
                cursor.close()
                return false
            }
            val routineId = cursor.getInt(0)
            cursor.close()

            db.execSQL("DELETE FROM steps WHERE id = ?", arrayOf(id.toString()))

            val remainingSteps = db.rawQuery(
                "SELECT id FROM steps WHERE routine_id = ? ORDER BY step_order",
                arrayOf(routineId.toString())
            )

            var newOrder = 1
            while (remainingSteps.moveToNext()) {
                val stepId = remainingSteps.getInt(0)
                db.execSQL(
                    "UPDATE steps SET step_order = ? WHERE id = ?",
                    arrayOf(newOrder, stepId)
                )
                newOrder++
            }
            remainingSteps.close()

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun insertStepAtEnd(step: Step): Long {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            db.execSQL(
                """
            UPDATE $Table_Steps 
            SET step_order = step_order + 1 
            WHERE routine_id = ? AND step_order >= ?
            """.trimIndent(),
                arrayOf(step.routine_id.toString(), step.step_order)
            )

            val values = ContentValues().apply {
                put("id", step.id)
                put("routine_id", step.routine_id)
                put("step_order", step.step_order)
                put("step_name", step.step_name)
                put("description", step.description)
                put("product_id", step.product_id)
            }

            val insertedId = db.insertWithOnConflict(Table_Steps, null, values, SQLiteDatabase.CONFLICT_REPLACE)

            db.setTransactionSuccessful()
            insertedId
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        } finally {
            db.endTransaction()
        }
    }


    fun moveStepToPosition(stepId: Int, newPosition: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.rawQuery(
                "SELECT routine_id, step_order FROM $Table_Steps WHERE id = ?",
                arrayOf(stepId.toString())
            )
            if (!cursor.moveToFirst()) {
                cursor.close()
                return false
            }
            val routineId = cursor.getInt(cursor.getColumnIndexOrThrow("routine_id"))
            val currentPos = cursor.getInt(cursor.getColumnIndexOrThrow("step_order"))
            cursor.close()

            if (newPosition == currentPos) {
                return true
            }

            if (newPosition > currentPos) {
                db.execSQL(
                    """
                UPDATE $Table_Steps
                SET step_order = step_order - 1
                WHERE routine_id = ? AND step_order > ? AND step_order <= ?
                """.trimIndent(),
                    arrayOf(routineId.toString(), currentPos.toString(), newPosition.toString())
                )
            } else {
                db.execSQL(
                    """
                UPDATE $Table_Steps
                SET step_order = step_order + 1
                WHERE routine_id = ? AND step_order >= ? AND step_order < ?
                """.trimIndent(),
                    arrayOf(routineId.toString(), newPosition.toString(), currentPos.toString())
                )
            }

            db.execSQL(
                "UPDATE $Table_Steps SET step_order = ? WHERE id = ?",
                arrayOf(newPosition, stepId)
            )

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }
    fun updateRoutineNotifications(routineId: Int, notificationTime: String, notificationDays: String): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put("notification_time", notificationTime)
                put("notification_days", notificationDays)
            }

            val rowsAffected = db.update(
                Table_Routines,
                values,
                "id = ?",
                arrayOf(routineId.toString())
            )

            db.setTransactionSuccessful()
            rowsAffected > 0

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }
    fun updateSpfRoutine(spfRoutine: SpfRoutine): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put("start_time", spfRoutine.start_time)
                put("end_time", spfRoutine.end_time)
                put("interval_minutes", spfRoutine.interval_minutes)
                put("active_days", spfRoutine.active_days)
            }

            val rowsAffected = db.update(
                Table_Spf,
                values,
                "id = ?",
                arrayOf(spfRoutine.id.toString())
            )

            db.setTransactionSuccessful()
            rowsAffected > 0

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun insertProducts(products: List<Product>): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (product in products) {
                val values = ContentValues().apply {
                    put("id", product.id)
                    put("name", product.name)
                    put("type", product.type)
                    put("ingredients", product.ingredients)
                    put("area", product.area)
                    put("time", product.time)
                    put("spf", product.spf)
                    put("url", product.url)
                    put("price", product.price)
                    put("irritating_ingredients", product.irritating_ingredients)
                }

                val result = db.insertWithOnConflict(
                    Table_Products,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )

                if (result == -1L) {
                    return false
                }
            }

            db.setTransactionSuccessful()
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun insertProduct(product: Product): Boolean {
        return insertProducts(listOf(product))
    }

    fun deleteAllProducts() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(Table_Products, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getProductById(productId: Int): Product? {
        val db = readableDatabase
        var product: Product? = null

        val query = "SELECT * FROM $Table_Products WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf(productId.toString()))

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
            val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients"))
            val area = cursor.getString(cursor.getColumnIndexOrThrow("area"))
            val time = cursor.getString(cursor.getColumnIndexOrThrow("time"))
            val spf = cursor.getInt(cursor.getColumnIndexOrThrow("spf"))
            val url = cursor.getString(cursor.getColumnIndexOrThrow("url"))
            val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
            val irritatingIngredients = cursor.getString(cursor.getColumnIndexOrThrow("irritating_ingredients"))

            product = Product(
                id = id,
                name = name,
                type = type,
                ingredients = ingredients,
                area = area,
                time = time,
                spf = spf,
                url = url,
                price = price,
                irritating_ingredients = irritatingIngredients
            )
        }

        cursor.close()
        return product
    }
    fun removeProductFromStep(stepId: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                putNull("product_id")
            }

            val rowsAffected = db.update(
                Table_Steps,
                values,
                "id = ?",
                arrayOf(stepId.toString())
            )

            db.setTransactionSuccessful()
            rowsAffected > 0

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }}

    fun getStepByRoutineAndType(routineId: Int, stepName: String): Step? {
        val db = readableDatabase
        var step: Step? = null

        val query = "SELECT * FROM $Table_Steps WHERE routine_id = ? AND step_name = ?"
        val cursor = db.rawQuery(query, arrayOf(routineId.toString(), stepName))

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val routine_id = cursor.getInt(cursor.getColumnIndexOrThrow("routine_id"))
            val step_order = cursor.getInt(cursor.getColumnIndexOrThrow("step_order"))
            val step_name = cursor.getString(cursor.getColumnIndexOrThrow("step_name"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val product_id = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"))

            step = Step(
                id = id,
                routine_id = routine_id,
                step_order = step_order,
                step_name = step_name,
                description = description ?: "",
                product_id = product_id
            )
        }

        cursor.close()
        return step
    }

    fun assignProductToStep(stepId: Int, productId: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put("product_id", productId)
            }

            val rowsAffected = db.update(
                Table_Steps,
                values,
                "id = ?",
                arrayOf(stepId.toString())
            )

            db.setTransactionSuccessful()
            rowsAffected > 0

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun assignProductToSpfRoutine(spfId: Int, productId: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put("product_id", productId)
            }

            val rowsAffected = db.update(
                Table_Spf,
                values,
                "id = ?",
                arrayOf(spfId.toString())
            )

            db.setTransactionSuccessful()
            rowsAffected > 0

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }
    fun removeProductFromSpfRoutine(spfId: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                putNull("product_id")
            }

            val rowsAffected = db.update(
                Table_Spf,
                values,
                "id = ?",
                arrayOf(spfId.toString())
            )

            db.setTransactionSuccessful()
            rowsAffected > 0

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }


    fun insertMessages(messages: List<UserMessage>): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (message in messages) {
                val values = ContentValues().apply {
                    put("message_id", message.message_id)
                    put("user_id", message.user_id)
                    put("sender", message.sender)
                    put("text", message.text)
                    put("timestamp", message.timestamp)
                }

                val result = db.insertWithOnConflict(
                    Table_Messages,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )

                if (result == -1L) {
                    return false
                }
            }

            db.setTransactionSuccessful()
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun insertMessage(message: UserMessage): Boolean {
        return insertMessages(listOf(message))
    }

    fun deleteAllMessages() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(Table_Messages, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }



    fun getAllMessages(): List<UserMessage> {
        val messages = mutableListOf<UserMessage>()
        val db = readableDatabase

        val query = "SELECT * FROM $Table_Messages ORDER BY timestamp ASC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val messageId = cursor.getInt(cursor.getColumnIndexOrThrow("message_id"))
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                val sender = cursor.getString(cursor.getColumnIndexOrThrow("sender"))
                val text = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))

                val message = UserMessage(
                    message_id = messageId,
                    user_id = userId,
                    sender = sender,
                    text = text,
                    timestamp = timestamp
                )
                messages.add(message)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return messages
    }
    fun insertRoutineCompletion(completion: RoutineCompletion) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("id", completion.id)
                put("user_id", completion.user_id)

                if (completion.routine_id != null) put("routine_id", completion.routine_id)
                if (completion.steps.isNotEmpty()) {
                    put("step_id", completion.steps.joinToString(","))
                }
                if (completion.spf_id != null) put("spf_id", completion.spf_id)
            }

            Log.d("DEBUG", "Inserting values: $values")

            val result = db.insertWithOnConflict(
                Table_RoutineCompletions,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )

            if (result != -1L) {
                db.setTransactionSuccessful()
                Log.d("DEBUG", "Insert successful with ID: $result")
            } else {
                Log.d("DEBUG", "Insert failed")
            }
        } catch (e: Exception) {
            Log.d("DEBUG", "Insert error: ${e.message}")
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }


    fun getTodayCompletedStepIds(): List<Int> {
        val stepIds = mutableListOf<Int>()
        val db = readableDatabase

        val todayQuery = "SELECT  DATE('now', 'localtime')"
        val todayCursor = db.rawQuery(todayQuery, null)
        var today = ""
        if (todayCursor.moveToFirst()) {
            today = todayCursor.getString(0)
            Log.d("DEBUG", "Today is: $today")
        }
        todayCursor.close()

        val query = """
        SELECT step_id, completion_date FROM $Table_RoutineCompletions 
        WHERE DATE(completion_date) =  DATE('now', 'localtime') 
        AND step_id IS NOT NULL 
        AND step_id != ''
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val stepIdString = cursor.getString(cursor.getColumnIndexOrThrow("step_id"))
                val completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completion_date"))
                Log.d("DEBUG", "Found completion: $completionDate with steps: $stepIdString")

                stepIdString?.split(",")?.forEach { stepId ->
                    stepId.trim().toIntOrNull()?.let { id ->
                        stepIds.add(id)
                    }
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        Log.d("DEBUG", "Total step IDs found: ${stepIds.distinct()}")
        return stepIds.distinct()
    }
    fun getAllRoutineCompletions(): List<RoutineCompletion> {
        val completions = mutableListOf<RoutineCompletion>()
        val db = readableDatabase

        val query = "SELECT * FROM $Table_RoutineCompletions ORDER BY completion_date DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                val completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completion_date"))
                val routineId = if (cursor.isNull(cursor.getColumnIndexOrThrow("routine_id"))) null
                else cursor.getInt(cursor.getColumnIndexOrThrow("routine_id"))
                val stepIdString = cursor.getString(cursor.getColumnIndexOrThrow("step_id"))
                val spfId = if (cursor.isNull(cursor.getColumnIndexOrThrow("spf_id"))) null
                else cursor.getInt(cursor.getColumnIndexOrThrow("spf_id"))

                val stepsList = if (stepIdString.isNullOrEmpty()) {
                    mutableListOf()
                } else {
                    stepIdString.split(",").mapNotNull { it.trim().toIntOrNull() }.toMutableList()
                }

                val completion = RoutineCompletion(
                    id = id,
                    user_id = userId,
                    completion_date = completionDate,
                    routine_id = routineId,
                    steps = stepsList,
                    spf_id = spfId
                )

                completions.add(completion)

            } while (cursor.moveToNext())
        }

        cursor.close()
        return completions
    }

    fun insertRoutineCompletions(completions: List<RoutineCompletion>): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (completion in completions) {
                val values = ContentValues().apply {
                    put("id", completion.id)
                    put("user_id", completion.user_id)
                    put("completion_date", completion.completion_date)

                    if (completion.routine_id != null) {
                        put("routine_id", completion.routine_id)
                    }

                    if (completion.steps.isNotEmpty()) {
                        put("step_id", completion.steps.joinToString(","))
                    }

                    if (completion.spf_id != null) {
                        put("spf_id", completion.spf_id)
                    }
                }

                val result = db.insertWithOnConflict(
                    Table_RoutineCompletions,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )

                if (result == -1L) {
                    Log.d("DEBUG", "Insert failed for completion ID: ${completion.id}")
                    return false
                }
            }

            db.setTransactionSuccessful()
            Log.d("DEBUG", "Successfully inserted ${completions.size} routine completions")
            return true

        } catch (e: Exception) {
            Log.d("DEBUG", "Insert routine completions error: ${e.message}")
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun deleteAllRoutineCompletions() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val deletedRows = db.delete(Table_RoutineCompletions, null, null)
            db.setTransactionSuccessful()
            Log.d("DEBUG", "Deleted $deletedRows routine completions")
        } catch (e: Exception) {
            Log.d("DEBUG", "Delete all routine completions error: ${e.message}")
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }
    fun getTodayCompletedRoutineIds(): List<Int> {
        val routineIds = mutableListOf<Int>()
        val db = readableDatabase

        val todayQuery = "SELECT  DATE('now', 'localtime')"
        val todayCursor = db.rawQuery(todayQuery, null)
        var today = ""
        if (todayCursor.moveToFirst()) {
            today = todayCursor.getString(0)
            Log.d("DEBUG", "Today is: $today")
        }
        todayCursor.close()

        val query = """
        SELECT routine_id, completion_date FROM $Table_RoutineCompletions 
        WHERE DATE(completion_date) =  DATE('now', 'localtime')
        AND routine_id IS NOT NULL
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val routineId = cursor.getInt(cursor.getColumnIndexOrThrow("routine_id"))
                val completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completion_date"))
                Log.d("DEBUG", "Found routine completion: $completionDate with routine ID: $routineId")

                routineIds.add(routineId)
            } while (cursor.moveToNext())
        }

        cursor.close()
        Log.d("DEBUG", "Total routine IDs found: ${routineIds.distinct()}")
        return routineIds.distinct()
    }

    fun getTodayCompletedSpfId(): Int? {
        val db = readableDatabase
        var spfId: Int? = null

        val todayQuery = "SELECT  DATE('now', 'localtime')"
        val todayCursor = db.rawQuery(todayQuery, null)
        var today = ""
        if (todayCursor.moveToFirst()) {
            today = todayCursor.getString(0)
            Log.d("DEBUG", "Today is: $today")
        }
        todayCursor.close()

        val query = """
        SELECT spf_id, completion_date FROM $Table_RoutineCompletions 
        WHERE DATE(completion_date) =  DATE('now', 'localtime')
        AND spf_id IS NOT NULL
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            spfId = cursor.getInt(cursor.getColumnIndexOrThrow("spf_id"))
            val completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completion_date"))
            Log.d("DEBUG", "Found SPF completion: $completionDate with SPF ID: $spfId")
        } else {
            Log.d("DEBUG", "No SPF completion found for today")
        }

        cursor.close()
        Log.d("DEBUG", "SPF ID found: $spfId")
        return spfId
    }

    fun insertDailyLog(userId: Int, id: Int): Long {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put("id", id)
                put("user_id", userId)
            }

            val result = db.insertWithOnConflict(
                Table_DailyLogs,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            db.setTransactionSuccessful()

            if (result != -1L) {
                Log.d("DEBUG", "Daily log created for user $userId with ID: $result")
            } else {
                Log.d("DEBUG", "Daily log already exists for user $userId")
            }

            result

        } catch (e: Exception) {
            Log.d("DEBUG", "Insert daily log error: ${e.message}")
            e.printStackTrace()
            -1L
        } finally {
            db.endTransaction()
        }
    }
    fun deleteDailyLogByUserId(userId: Int): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val deletedRows = db.delete(
                Table_DailyLogs,
                "user_id = ?",
                arrayOf(userId.toString())
            )

            db.setTransactionSuccessful()
            Log.d("DEBUG", "Deleted daily log for user $userId: $deletedRows rows")

            deletedRows > 0

        } catch (e: Exception) {
            Log.d("DEBUG", "Delete daily log error: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }
    fun getDailyLogByUserId(userId: Int): DailyLogClass? {
        val db = readableDatabase
        var dailyLog: DailyLogClass? = null

        val query = "SELECT * FROM $Table_DailyLogs WHERE user_id = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))

            dailyLog = DailyLogClass(
                id = id,
                user_id = user_id
            )
        }

        cursor.close()
        return dailyLog
    }
    fun insertDailyLogClass(dailyLogClass: DailyLogClass): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val dailyLogValues = ContentValues().apply {
                put("id", dailyLogClass.id)
                put("user_id", dailyLogClass.user_id)
            }

            val dailyLogResult = db.insertWithOnConflict(
                Table_DailyLogs,
                null,
                dailyLogValues,
                SQLiteDatabase.CONFLICT_REPLACE
            )

            if (dailyLogResult == -1L) {
                Log.d("DEBUGx", "Failed to insert daily log")
                return false
            }

            Log.d("DEBUGx", "Daily log inserted with ID: ${dailyLogClass.id}")

            if (dailyLogClass.contents.isNotEmpty()) {
                for (content in dailyLogClass.contents) {
                    val contentValues = ContentValues().apply {
                        put("id", content.id)
                        put("daily_log_id", content.daily_log_id)
                        put("skin_feeling_score", content.skin_feeling_score)
                        put("skin_condition", content.skin_condition)
                        put("notes", content.notes)
                        put("weather", content.weather)
                        put("stress_level", content.stress_level)
                        put("log_date", content.log_date)
                    }

                    val contentResult = db.insertWithOnConflict(
                        Table_DailyLogContent,
                        null,
                        contentValues,
                        SQLiteDatabase.CONFLICT_REPLACE
                    )

                    if (contentResult == -1L) {
                        Log.d("DEBUGx", "Failed to insert content ID: ${content.id}")
                        return false
                    }
                }
                Log.d("DEBUGx", "Inserted ${dailyLogClass.contents.size} content entries")
            } else {
                Log.d("DEBUGx", "No contents to insert (empty list)")
            }

            db.setTransactionSuccessful()
            true

        } catch (e: Exception) {
            Log.d("DEBUGx", "Insert DailyLogClass error: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun deleteAllDailyLogs(): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val contentDeleted = db.delete(Table_DailyLogContent, null, null)

            val dailyLogsDeleted = db.delete(Table_DailyLogs, null, null)

            db.setTransactionSuccessful()
            Log.d("DEBUG", "Deleted $contentDeleted content entries and $dailyLogsDeleted daily logs")

            true

        } catch (e: Exception) {
            Log.d("DEBUG", "Delete all daily logs error: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun getTodayDailyLogs(userId: Int): DailyLogClass? {
        val db = readableDatabase
        var dailyLogClass: DailyLogClass? = null

        val todayQuery = "SELECT DATE('now', 'localtime')"
        val todayCursor = db.rawQuery(todayQuery, null)
        var today = ""
        if (todayCursor.moveToFirst()) {
            today = todayCursor.getString(0)
            Log.d("DEBUG", "Today is: $today")
        }
        todayCursor.close()

        val dailyLogQuery = "SELECT * FROM $Table_DailyLogs WHERE user_id = ?"
        val dailyLogCursor = db.rawQuery(dailyLogQuery, arrayOf(userId.toString()))

        if (dailyLogCursor.moveToFirst()) {
            val id = dailyLogCursor.getInt(dailyLogCursor.getColumnIndexOrThrow("id"))
            val user_id = dailyLogCursor.getInt(dailyLogCursor.getColumnIndexOrThrow("user_id"))

            val contentQuery = """
            SELECT * FROM $Table_DailyLogContent 
            WHERE daily_log_id = ? AND DATE(log_date) = DATE('now', 'localtime')
            ORDER BY log_date DESC
        """.trimIndent()

            val contentCursor = db.rawQuery(contentQuery, arrayOf(id.toString()))
            val contents = mutableListOf<DailyLogContent>()

            if (contentCursor.moveToFirst()) {
                do {
                    val contentId = contentCursor.getInt(contentCursor.getColumnIndexOrThrow("id"))
                    val dailyLogId = contentCursor.getInt(contentCursor.getColumnIndexOrThrow("daily_log_id"))
                    val skinFeelingScore = if (contentCursor.isNull(contentCursor.getColumnIndexOrThrow("skin_feeling_score"))) null
                    else contentCursor.getInt(contentCursor.getColumnIndexOrThrow("skin_feeling_score"))
                    val skinCondition = contentCursor.getString(contentCursor.getColumnIndexOrThrow("skin_condition"))
                    val notes = contentCursor.getString(contentCursor.getColumnIndexOrThrow("notes"))
                    val weather = contentCursor.getString(contentCursor.getColumnIndexOrThrow("weather"))
                    val stressLevel = if (contentCursor.isNull(contentCursor.getColumnIndexOrThrow("stress_level"))) null
                    else contentCursor.getInt(contentCursor.getColumnIndexOrThrow("stress_level"))
                    val logDate = contentCursor.getString(contentCursor.getColumnIndexOrThrow("log_date"))

                    val dailyLogContent = DailyLogContent(
                        id = contentId,
                        daily_log_id = dailyLogId,
                        skin_feeling_score = skinFeelingScore,
                        skin_condition = skinCondition ?: "normal",
                        notes = notes ?: "",
                        weather = weather,
                        stress_level = stressLevel,
                        log_date = logDate
                    )

                    contents.add(dailyLogContent)
                    Log.d("DEBUG", "Found today's content: ID=$contentId, Date=$logDate")

                } while (contentCursor.moveToNext())
            }

            contentCursor.close()

            dailyLogClass = DailyLogClass(
                id = id,
                user_id = user_id,
                contents = contents
            )

            Log.d("DEBUG", "DailyLogClass for user $userId with ${contents.size} today's entries")
        } else {
            Log.d("DEBUG", "No daily log found for user $userId")
        }

        dailyLogCursor.close()
        return dailyLogClass
    }

    fun insertDailyLogContent(content: DailyLogContent): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val dailyLogIdQuery = "SELECT id FROM $Table_DailyLogs LIMIT 1"
            val cursor = db.rawQuery(dailyLogIdQuery, null)

            var dailyLogId: Int? = null
            if (cursor.moveToFirst()) {
                dailyLogId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                Log.d("DEBUGw", "Found daily_log_id: $dailyLogId")
            }
            cursor.close()

            if (dailyLogId == null) {
                Log.d("DEBUGw", "No daily_log found in database")
                return false
            }

            val values = ContentValues().apply {
                put("id", content.id)
                put("daily_log_id",content.daily_log_id)
                put("skin_feeling_score", content.skin_feeling_score)
                put("skin_condition", content.skin_condition)
                put("notes", content.notes)
                put("weather", content.weather)
                put("stress_level", content.stress_level)
                put("log_date", content.log_date)
            }

            val result = db.insertWithOnConflict(
                Table_DailyLogContent,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )

            db.setTransactionSuccessful()

            if (result != -1L) {
                Log.d("DEBUGw", "Daily log content inserted successfully with ID: $result, daily_log_id: $dailyLogId")
                true
            } else {
                Log.d("DEBUGw", "Failed to insert daily log content")
                false
            }

        } catch (e: Exception) {
            Log.d("DEBUGw", "Insert daily log content error: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }
    fun getTodayDailyLogContent(): DailyLogContent? {
        val db = readableDatabase
        var todayLog: DailyLogContent? = null

        val todayQuery = "SELECT DATE('now', 'localtime')"
        Log.d("DEBUGs", "Today is: $todayQuery")
        val todayCursor = db.rawQuery(todayQuery, null)
        var today = ""
        if (todayCursor.moveToFirst()) {
            today = todayCursor.getString(0)
            Log.d("DEBUGs", "Today is: $today")
        }
        todayCursor.close()

        val query = """
        SELECT * FROM $Table_DailyLogContent 
        WHERE DATE(log_date) = DATE('now', 'localtime')
        ORDER BY log_date DESC
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val dailyLogId = cursor.getInt(cursor.getColumnIndexOrThrow("daily_log_id"))
            val skinFeelingScore = if (cursor.isNull(cursor.getColumnIndexOrThrow("skin_feeling_score"))) null
            else cursor.getInt(cursor.getColumnIndexOrThrow("skin_feeling_score"))
            val skinCondition = cursor.getString(cursor.getColumnIndexOrThrow("skin_condition"))
            val notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"))
            val weather = cursor.getString(cursor.getColumnIndexOrThrow("weather"))
            val stressLevel = if (cursor.isNull(cursor.getColumnIndexOrThrow("stress_level"))) null
            else cursor.getInt(cursor.getColumnIndexOrThrow("stress_level"))
            val logDate = cursor.getString(cursor.getColumnIndexOrThrow("log_date"))

            todayLog = DailyLogContent(
                id = id,
                daily_log_id = dailyLogId,
                skin_feeling_score = skinFeelingScore,
                skin_condition = skinCondition ?: "normal",
                notes = notes,
                weather = weather,
                stress_level = stressLevel,
                log_date = logDate
            )

            Log.d("DEBUGs", "Found today's daily log: ID=$id, Date=$logDate")
        } else {
            Log.d("DEBUGs", "No daily log found for today")
        }

        cursor.close()
        return todayLog
    }
    fun getDailyLogContent(): DailyLogContent? {
        val db = readableDatabase
        var todayLog: DailyLogContent? = null

        val todayQuery = "SELECT DATE('now', 'localtime')"
        Log.d("DEBUGs", "Today is: $todayQuery")
        val todayCursor = db.rawQuery(todayQuery, null)
        var today = ""
        if (todayCursor.moveToFirst()) {
            today = todayCursor.getString(0)
            Log.d("DEBUGs", "Today is: $today")
        }
        todayCursor.close()

        val query = """
        SELECT * FROM $Table_DailyLogContent 
       
        ORDER BY log_date DESC
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val dailyLogId = cursor.getInt(cursor.getColumnIndexOrThrow("daily_log_id"))
            val skinFeelingScore = if (cursor.isNull(cursor.getColumnIndexOrThrow("skin_feeling_score"))) null
            else cursor.getInt(cursor.getColumnIndexOrThrow("skin_feeling_score"))
            val skinCondition = cursor.getString(cursor.getColumnIndexOrThrow("skin_condition"))
            val notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"))
            val weather = cursor.getString(cursor.getColumnIndexOrThrow("weather"))
            val stressLevel = if (cursor.isNull(cursor.getColumnIndexOrThrow("stress_level"))) null
            else cursor.getInt(cursor.getColumnIndexOrThrow("stress_level"))
            val logDate = cursor.getString(cursor.getColumnIndexOrThrow("log_date"))

            todayLog = DailyLogContent(
                id = id,
                daily_log_id = dailyLogId,
                skin_feeling_score = skinFeelingScore,
                skin_condition = skinCondition ?: "normal",
                notes = notes,
                weather = weather,
                stress_level = stressLevel,
                log_date = logDate
            )

            Log.d("DEBUGs", "Found today's daily log: ID=$id, Date=$logDate")
        } else {
            Log.d("DEBUGs", "No daily log found for today")
        }

        cursor.close()
        return todayLog
    }

    fun insertRoutineCompletionDetails(completion: RoutineCompletionDetails): Long {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val validTypes = listOf("morning", "evening", "exfoliation", "face mask",
                "eye mask", "lip mask", "spf")
            if (completion.routine_type != null && completion.routine_type !in validTypes) {
                Log.d("DEBUG", "Invalid routine type: ${completion.routine_type}")
                return -1L
            }

            val values = ContentValues().apply {
                put("id", completion.id)

                if (completion.completion_date != null) {
                    put("completion_date", completion.completion_date)
                }

                put("user_id", completion.user_id)
                put("routine_type", completion.routine_type)

                if (completion.steps != null && completion.steps!!.isNotEmpty()) {
                    val stepsJson = completion.steps!!.joinToString(",") { "\"$it\"" }
                    put("steps", "[$stepsJson]")
                }

                put("max_steps", completion.max_steps)
            }

            val result = db.insertWithOnConflict(
                Table_RoutineCompletionsDetails,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )

            if (result != -1L) {
                db.setTransactionSuccessful()
                Log.d("DEBUG", "Routine completion details inserted with ID: ${completion.id}")
            } else {
                Log.d("DEBUG", "Failed to insert routine completion details")
            }

            completion.id.toLong()

        } catch (e: Exception) {
            Log.d("DEBUG", "Insert routine completion details error: ${e.message}")
            e.printStackTrace()
            -1L
        } finally {
            db.endTransaction()
        }
    }
    fun getRoutineCompletionDetailsByDate(date: String): List<RoutineCompletionDetails> {
        val completions = mutableListOf<RoutineCompletionDetails>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $Table_RoutineCompletionsDetails 
        WHERE DATE(completion_date) = DATE(?)
        ORDER BY completion_date DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(date))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completion_date"))
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                val routineType = cursor.getString(cursor.getColumnIndexOrThrow("routine_type"))
                val stepsJson = cursor.getString(cursor.getColumnIndexOrThrow("steps"))
                val maxSteps = cursor.getInt(cursor.getColumnIndexOrThrow("max_steps"))

                val stepsList = if (stepsJson.isNullOrEmpty()) {
                    mutableListOf()
                } else {
                    try {
                        stepsJson.removeSurrounding("[", "]")
                            .split(",")
                            .map { it.trim().removeSurrounding("\"") }
                            .toMutableList()
                    } catch (e: Exception) {
                        Log.d("DEBUG", "Error parsing steps JSON: ${e.message}")
                        mutableListOf()
                    }
                }

                val completion = RoutineCompletionDetails(
                    id = id,
                    completion_date = completionDate,
                    user_id = userId,
                    routine_type = routineType,
                    steps = stepsList,
                    max_steps = maxSteps
                )

                completions.add(completion)

            } while (cursor.moveToNext())
        }

        cursor.close()
        Log.d("DEBUG", "Found ${completions.size} routine completion details for date: $date")
        return completions
    }
    fun getAllRoutineCompletionDetails(): List<RoutineCompletionDetails> {
        val completions = mutableListOf<RoutineCompletionDetails>()
        val db = readableDatabase

        val query = """
        SELECT * FROM $Table_RoutineCompletionsDetails 
        ORDER BY completion_date DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completion_date"))
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                val routineType = cursor.getString(cursor.getColumnIndexOrThrow("routine_type"))
                val stepsJson = cursor.getString(cursor.getColumnIndexOrThrow("steps"))
                val maxSteps = cursor.getInt(cursor.getColumnIndexOrThrow("max_steps"))

                val stepsList = if (stepsJson.isNullOrEmpty()) {
                    mutableListOf()
                } else {
                    try {
                        stepsJson.removeSurrounding("[", "]")
                            .split(",")
                            .map { it.trim().removeSurrounding("\"") }
                            .toMutableList()
                    } catch (e: Exception) {
                        Log.d("DEBUG", "Error parsing steps JSON: ${e.message}")
                        mutableListOf()
                    }
                }

                val completion = RoutineCompletionDetails(
                    id = id,
                    completion_date = completionDate,
                    user_id = userId,
                    routine_type = routineType,
                    steps = stepsList,
                    max_steps = maxSteps
                )

                completions.add(completion)

            } while (cursor.moveToNext())
        }

        cursor.close()
        Log.d("DEBUG", "Found ${completions.size} total routine completion details")
        return completions
    }
    fun getStepNamesByIds(stepIds: List<Int>): MutableList<String> {
        val stepNames = mutableListOf<String>()
        val db = readableDatabase

        if (stepIds.isEmpty()) {
            return stepNames
        }

        try {
            val placeholders = stepIds.joinToString(",") { "?" }

            val query = """
            SELECT step_name FROM $Table_Steps 
            WHERE id IN ($placeholders)
            ORDER BY step_order ASC
        """.trimIndent()

            val selectionArgs = stepIds.map { it.toString() }.toTypedArray()

            val cursor = db.rawQuery(query, selectionArgs)

            if (cursor.moveToFirst()) {
                do {
                    val stepName = cursor.getString(cursor.getColumnIndexOrThrow("step_name"))
                    stepNames.add(stepName)
                } while (cursor.moveToNext())
            }

            cursor.close()
            Log.d("DEBUG", "Found ${stepNames.size} step names for ${stepIds.size} IDs")

        } catch (e: Exception) {
            Log.d("DEBUG", "Error getting step names: ${e.message}")
            e.printStackTrace()
        }

        return stepNames
    }
    fun deleteAllRoutineCompletionDetails() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val deletedRows = db.delete(Table_RoutineCompletionsDetails, null, null)
            db.setTransactionSuccessful()
            Log.d("DEBUG", "Deleted $deletedRows routine completion details")
        } catch (e: Exception) {
            Log.d("DEBUG", "Delete all routine completion details error: ${e.message}")
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }
    fun insertRoutineCompletionDetailsList(completions: List<RoutineCompletionDetails>): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (completion in completions) {
                val validTypes = listOf("morning", "evening", "exfoliation", "face mask",
                    "eye mask", "lip mask", "spf")
                if (completion.routine_type != null && completion.routine_type !in validTypes) {
                    Log.d("DEBUG", "Invalid routine type: ${completion.routine_type} for ID: ${completion.id}")
                    return false
                }

                val values = ContentValues().apply {
                    put("id", completion.id)

                    if (completion.completion_date != null) {
                        put("completion_date", completion.completion_date)
                    }

                    put("user_id", completion.user_id)
                    put("routine_type", completion.routine_type)

                    if (completion.steps != null && completion.steps!!.isNotEmpty()) {
                        val stepsJson = completion.steps!!.joinToString(",") { "\"$it\"" }
                        put("steps", "[$stepsJson]")
                    } else {
                        putNull("steps")
                    }

                    put("max_steps", completion.max_steps)
                }

                val result = db.insertWithOnConflict(
                    Table_RoutineCompletionsDetails,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )

                if (result == -1L) {
                    Log.d("DEBUG", "Insert failed for routine completion details ID: ${completion.id}")
                    return false
                }
            }

            db.setTransactionSuccessful()
            Log.d("DEBUG", "Successfully inserted ${completions.size} routine completion details")
            return true

        } catch (e: Exception) {
            Log.d("DEBUG", "Insert routine completion details list error: ${e.message}")
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }
    fun getDailyLogContentByDate(date: String): DailyLogContent? {
        val db = readableDatabase
        var dailyLog: DailyLogContent? = null

        try {
            val query = """
        SELECT * FROM $Table_DailyLogContent 
        WHERE DATE(log_date) = DATE(?)
        ORDER BY log_date DESC
        LIMIT 1
    """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(date))

            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val dailyLogId = cursor.getInt(cursor.getColumnIndexOrThrow("daily_log_id"))
                val skinFeelingScore = if (cursor.isNull(cursor.getColumnIndexOrThrow("skin_feeling_score"))) null
                else cursor.getInt(cursor.getColumnIndexOrThrow("skin_feeling_score"))
                val skinCondition = cursor.getString(cursor.getColumnIndexOrThrow("skin_condition"))
                val notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"))
                val weather = cursor.getString(cursor.getColumnIndexOrThrow("weather"))
                val stressLevel = if (cursor.isNull(cursor.getColumnIndexOrThrow("stress_level"))) null
                else cursor.getInt(cursor.getColumnIndexOrThrow("stress_level"))
                val logDate = cursor.getString(cursor.getColumnIndexOrThrow("log_date"))

                dailyLog = DailyLogContent(
                    id = id,
                    daily_log_id = dailyLogId,
                    skin_feeling_score = skinFeelingScore,
                    skin_condition = skinCondition ?: "normal",
                    notes = notes,
                    weather = weather,
                    stress_level = stressLevel,
                    log_date = logDate
                )

                Log.d("DEBUG", "Found daily log for date $date: ID=$id")
            } else {
                Log.d("DEBUG", "No daily log found for date: $date")
            }

            cursor.close()

        } catch (e: Exception) {
            Log.d("DEBUG", "Error getting daily log content by date: ${e.message}")
            e.printStackTrace()
        }

        return dailyLog
    }

    fun getRoutineCompletionsByDateRange(startDate: String, endDate: String): List<RoutineCompletionDetails> {
        val routineCompletions = mutableListOf<RoutineCompletionDetails>()
        val db = this.readableDatabase

        val query = """
        SELECT * FROM routine_completions_details 
        WHERE completion_date BETWEEN ? AND ?
        ORDER BY completion_date
    """

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        cursor.use { c ->
            while (c.moveToNext()) {
                val stepsString = c.getString(c.getColumnIndexOrThrow("steps")) ?: ""
                val stepsList = if (stepsString.isNotEmpty()) {
                    stepsString.split(",").map { it.trim() }.toMutableList()
                } else {
                    mutableListOf()
                }

                val completion = RoutineCompletionDetails(
                    id = c.getInt(c.getColumnIndexOrThrow("id")),
                    completion_date = c.getString(c.getColumnIndexOrThrow("completion_date")),
                    user_id = c.getInt(c.getColumnIndexOrThrow("user_id")),
                    routine_type = c.getString(c.getColumnIndexOrThrow("routine_type")),
                    steps = stepsList,
                    max_steps = c.getInt(c.getColumnIndexOrThrow("max_steps"))
                )
                routineCompletions.add(completion)
            }
        }

        return routineCompletions
    }


    fun insertQuote(quote: Quote): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            db.delete(Table_Quotes, null, null)

            val values = ContentValues().apply {
                put("quote", quote.quote)
                put("autor", quote.author)
            }

            val result = db.insert(Table_Quotes, null, values)

            if (result != -1L) {
                db.setTransactionSuccessful()
                Log.d("DEBUG", "Quote inserted successfully: ${quote.quote}")
                true
            } else {
                Log.d("DEBUG", "Failed to insert quote")
                false
            }

        } catch (e: Exception) {
            Log.d("DEBUG", "Insert quote error: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun getQuote(): Quote? {
        val db = readableDatabase
        var quote: Quote? = null

        val query = "SELECT quote, autor FROM $Table_Quotes LIMIT 1"
        val cursor = db.rawQuery(query, null)

        try {
            if (cursor.moveToFirst()) {
                val quoteText = cursor.getString(cursor.getColumnIndexOrThrow("quote"))
                val author = cursor.getString(cursor.getColumnIndexOrThrow("autor"))

                quote = Quote(
                    quote = quoteText ?: "",
                    author = author ?: ""
                )

                Log.d("DEBUG", "Quote found: $quoteText by $author")
            } else {
                Log.d("DEBUG", "No quote found in database")
            }
        } catch (e: Exception) {
            Log.d("DEBUG", "Get quote error: ${e.message}")
            e.printStackTrace()
        } finally {
            cursor.close()
        }

        return quote
    }

    fun deleteQuote(): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val deletedRows = db.delete(Table_Quotes, null, null)
            db.setTransactionSuccessful()

            Log.d("DEBUG", "Deleted $deletedRows quote(s)")
            deletedRows > 0

        } catch (e: Exception) {
            Log.d("DEBUG", "Delete quote error: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }
    fun getAllProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase

        val query = "SELECT * FROM $Table_Products ORDER BY name ASC"
        val cursor = db.rawQuery(query, null)

        try {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
                    val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients"))
                    val area = cursor.getString(cursor.getColumnIndexOrThrow("area"))
                    val time = cursor.getString(cursor.getColumnIndexOrThrow("time"))
                    val spf = cursor.getInt(cursor.getColumnIndexOrThrow("spf"))
                    val url = cursor.getString(cursor.getColumnIndexOrThrow("url"))
                    val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
                    val irritatingIngredients = cursor.getString(cursor.getColumnIndexOrThrow("irritating_ingredients"))

                    val product = Product(
                        id = id,
                        name = name ?: "",
                        type = type ?: "",
                        ingredients = ingredients ?: "",
                        area = area ?: "",
                        time = time ?: "",
                        spf = spf,
                        url = url ?: "",
                        price = price ?: "",
                        irritating_ingredients = irritatingIngredients ?: ""
                    )

                    products.add(product)

                } while (cursor.moveToNext())
            }

            Log.d("DEBUG", "Found ${products.size} products in database")

        } catch (e: Exception) {
            Log.d("DEBUG", "Get all products error: ${e.message}")
            e.printStackTrace()
        } finally {
            cursor.close()
        }

        return products
    }

}