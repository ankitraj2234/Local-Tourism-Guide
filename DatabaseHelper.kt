package com.example.localtourismguide

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        const val DATABASE_NAME = "LocalTourismGuide.db"
        const val DATABASE_VERSION = 1
        const val TABLE_USERS = "users"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_NAME = "name"
        const val COLUMN_AGE = "age"
        const val COLUMN_MOBILE = "mobile"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_IMAGE = "image"

        const val TABLE_FAVORITES = "favorites"
        const val COLUMN_FAVORITE_ID = "id"
        const val COLUMN_LOCATION_NAME = "location_name"
        const val COLUMN_FAVORITE_IMAGE = "image"

        const val TABLE_FEEDBACKS = "feedbacks"
        const val COLUMN_FEEDBACK_ID = "feedback_id"
        const val COLUMN_FEEDBACK_TEXT = "feedback_text"
        const val COLUMN_FEEDBACK_IMAGE = "feedback_image"
        const val COLUMN_FEEDBACK_RATING = "rating"
        const val COLUMN_FEEDBACK_AVERAGE_RATING = "average_rating"


        const val TABLE_MEMORIES = "memories"
        const val COLUMN_MEMORY_ID = "memory_id"
        const val COLUMN_MEMORY_IMAGE = "image"
        const val COLUMN_MEMORY_DESCRIPTION = "description"


    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUserTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USERNAME TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT,
                $COLUMN_AGE INTEGER,
                $COLUMN_MOBILE TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_IMAGE BLOB
            )
        """
        db?.execSQL(createUserTable)


        val createFavoritesTable = """
        CREATE TABLE $TABLE_FAVORITES (
            $COLUMN_FAVORITE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USERNAME TEXT,
            $COLUMN_LOCATION_NAME TEXT,
            $COLUMN_FAVORITE_IMAGE BLOB,
            FOREIGN KEY ($COLUMN_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
        )
    """
        db?.execSQL(createFavoritesTable)


        val createFeedbackTable = """
        CREATE TABLE $TABLE_FEEDBACKS (
            $COLUMN_FEEDBACK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USERNAME TEXT,
            $COLUMN_LOCATION_NAME TEXT,
            $COLUMN_FEEDBACK_TEXT TEXT,
            $COLUMN_FEEDBACK_IMAGE BLOB,
            $COLUMN_FEEDBACK_RATING REAL,
            $COLUMN_FEEDBACK_AVERAGE_RATING REAL DEFAULT 0.0,
            FOREIGN KEY ($COLUMN_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
        )
    """
        db?.execSQL(createFeedbackTable)


        val createMemoriesTable = """
            CREATE TABLE $TABLE_MEMORIES (
                $COLUMN_MEMORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT,
                $COLUMN_LOCATION_NAME TEXT,
                $COLUMN_MEMORY_IMAGE BLOB,
                $COLUMN_MEMORY_DESCRIPTION TEXT,
                FOREIGN KEY ($COLUMN_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
            )
        """
        db?.execSQL(createMemoriesTable)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FEEDBACKS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MEMORIES")



        onCreate(db)
    }


    fun registerUser(
        username: String, name: String, age: Int, mobile: String, email: String, password: String,
         image: ByteArray
    ): Boolean {
        val db = writableDatabase

        // Check uniqueness
        if (!isFieldUnique(COLUMN_USERNAME, username)) {
            return false
        }
        if (!isFieldUnique(COLUMN_EMAIL, email)) {
            return false
        }
        if (!isFieldUnique(COLUMN_MOBILE, mobile)) {
            return false
        }

        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_NAME, name)
            put(COLUMN_AGE, age)
            put(COLUMN_MOBILE, mobile)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, encrypt(password))
            put(COLUMN_IMAGE, image)
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun isFieldUnique(column: String, value: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $column = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(value))
        val isUnique = !cursor.moveToFirst()
        cursor.close()
        return isUnique
    }

    fun loginUser(usernameOrEmail: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USERNAME),
            "($COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?) AND $COLUMN_PASSWORD = ?",
            arrayOf(usernameOrEmail, usernameOrEmail, encrypt(password)),
            null, null, null
        )
        val isLoggedIn = cursor.count > 0
        cursor.close()
        return isLoggedIn
    }



    fun updatePassword(usernameOrEmail: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD, encrypt(newPassword))
        }
        val result = db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?",
            arrayOf(usernameOrEmail, usernameOrEmail)
        )
        return result > 0
    }

    fun isUserExists(usernameOrEmail: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(usernameOrEmail, usernameOrEmail))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserEmail(usernameOrEmail: String): String? {
        val db = readableDatabase
        val query = "SELECT $COLUMN_EMAIL FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(usernameOrEmail, usernameOrEmail))
        var email: String? = null
        if (cursor.moveToFirst()) {
            email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
        }
        cursor.close()
        return email
    }

    private fun encrypt(data: String): String {
        return MessageDigest.getInstance("SHA-256").digest(data.toByteArray()).fold("") { str, it -> str + "%02x".format(it) }
    }

//favorite logic
fun addFavoritePlace(username: String, locationName: String, image: ByteArray): Boolean {
    val db = writableDatabase
    val values = ContentValues().apply {
        put(COLUMN_USERNAME, username)
        put(COLUMN_LOCATION_NAME, locationName)
        put(COLUMN_FAVORITE_IMAGE, image)
    }
    return db.insert(TABLE_FAVORITES, null, values) != -1L
}

    fun deleteFeedback(feedbackId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_FEEDBACKS, "$COLUMN_FEEDBACK_ID = ?", arrayOf(feedbackId.toString()))
        return result > 0
    }


    fun getAllFavoritePlaces(username: String): List<FavoritePlace> {
        val favoritePlaces = mutableListOf<FavoritePlace>()
        val db = readableDatabase

        val cursor: Cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(COLUMN_FAVORITE_ID, COLUMN_LOCATION_NAME, COLUMN_FAVORITE_IMAGE),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE_ID))
                val locationName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION_NAME))
                val image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE_IMAGE))

                favoritePlaces.add(FavoritePlace(id, locationName, image))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return favoritePlaces
    }
    fun isFavoritePlaceExists(username: String, locationName: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM favorites WHERE username = ? AND location_name = ?", arrayOf(username, locationName))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun deleteFavoritePlace(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_FAVORITES, "$COLUMN_FAVORITE_ID = ?", arrayOf(id.toString()))
    }


//feedback function

    fun addFeedback(feedback: Feedback): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, feedback.username)
            put(COLUMN_LOCATION_NAME, feedback.locationName)
            put(COLUMN_FEEDBACK_TEXT, feedback.text)
            put(COLUMN_FEEDBACK_IMAGE, feedback.image)
            put(COLUMN_FEEDBACK_RATING, feedback.rating)
        }
        val newRowId = db.insert(TABLE_FEEDBACKS, null, values)
        db.close()

        if (newRowId != -1L) {
            feedback.id = newRowId.toInt()
            return true
        }
        return false
    }

    fun getFeedbacks(username: String, locationName: String): List<Feedback> {
        val feedbackList = mutableListOf<Feedback>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_FEEDBACKS,
            arrayOf(
                COLUMN_FEEDBACK_ID,
                COLUMN_LOCATION_NAME,
                COLUMN_FEEDBACK_TEXT,
                COLUMN_FEEDBACK_IMAGE,
                COLUMN_FEEDBACK_RATING
            ),
            "$COLUMN_USERNAME = ? AND $COLUMN_LOCATION_NAME = ?",
            arrayOf(username, locationName),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val feedback = Feedback(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_ID)),
                    username = username,
                    locationName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION_NAME)),
                    text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_TEXT)),
                    image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_IMAGE)),
                    rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_RATING))
                )
                feedbackList.add(feedback)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return feedbackList
    }

    
    fun updateFeedbackRating(feedbackId: Int, newRating: Float): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_FEEDBACK_RATING, newRating)
        }
        val updateResult = db.update(
            TABLE_FEEDBACKS,
            values,
            "$COLUMN_FEEDBACK_ID = ?",
            arrayOf(feedbackId.toString())
        )

        if (updateResult > 0) {
            return recalculateAndUpdateAverageRating(feedbackId)
        }
        return false
    }
    fun getFeedbacksForLocationWithAverage(locationName: String): List<Feedback> {
        val feedbackList = mutableListOf<Feedback>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_FEEDBACKS,
            arrayOf(
                COLUMN_FEEDBACK_ID,
                COLUMN_USERNAME,
                COLUMN_LOCATION_NAME,
                COLUMN_FEEDBACK_TEXT,
                COLUMN_FEEDBACK_IMAGE,
                COLUMN_FEEDBACK_RATING,
                COLUMN_FEEDBACK_AVERAGE_RATING
            ),
            "$COLUMN_LOCATION_NAME = ?",
            arrayOf(locationName),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val feedback = Feedback(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_ID)),
                    username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    locationName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION_NAME)),
                    text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_TEXT)),
                    image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_IMAGE)),
                    rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_RATING)),
                    averageRating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_AVERAGE_RATING))
                )
                feedbackList.add(feedback)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return feedbackList
    }




    fun recalculateAndUpdateAverageRating(feedbackId: Int): Boolean {
        val db = writableDatabase

        val query = "SELECT $COLUMN_FEEDBACK_RATING FROM $TABLE_FEEDBACKS WHERE $COLUMN_FEEDBACK_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(feedbackId.toString()))

        var totalRatings = 0.0
        var ratingCount = 0

        if (cursor.moveToFirst()) {
            do {
                val rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_RATING))
                totalRatings += rating
                ratingCount++
            } while (cursor.moveToNext())
        }
        cursor.close()

        val averageRating = if (ratingCount > 0) totalRatings / ratingCount else 0.0

        val values = ContentValues().apply {
            put(COLUMN_FEEDBACK_AVERAGE_RATING, averageRating)
        }
        val result = db.update(
            TABLE_FEEDBACKS,
            values,
            "$COLUMN_FEEDBACK_ID = ?",
            arrayOf(feedbackId.toString())
        )
        return result > 0
    }

    fun getAverageRating(locationName: String): Float {
        val db = readableDatabase
        val query = "SELECT AVG(rating) as averageRating FROM feedbacks WHERE location_name = ?"
        val cursor = db.rawQuery(query, arrayOf(locationName))
        return if (cursor.moveToFirst()) cursor.getFloat(cursor.getColumnIndexOrThrow("averageRating")) else 0.0f
    }

    fun updateFeedback(feedbackId: Int, text: String?, image: ByteArray?): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("text", text)
            put("image", image)
        }
        val rowsAffected = db.update("feedbacks", values, "id=?", arrayOf(feedbackId.toString()))
        db.close()
        return rowsAffected > 0
    }


    fun getUserImage(username: String): ByteArray? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_IMAGE),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        var image: ByteArray? = null
        if (cursor.moveToFirst()) {
            image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))
        }
        cursor.close()
        return image
    }


    fun addMemory(username: String, locationName: String, image: ByteArray, description: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_LOCATION_NAME, locationName)
            put(COLUMN_MEMORY_IMAGE, image)
            put(COLUMN_MEMORY_DESCRIPTION, description)
        }
        val result = db.insert(TABLE_MEMORIES, null, values)
        db.close()
        return result
    }

    fun getAllMemories(username: String, locationName: String): List<Memory> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_MEMORIES, null, "$COLUMN_USERNAME = ? AND $COLUMN_LOCATION_NAME = ?",
            arrayOf(username, locationName), null, null, null
        )
        val memories = mutableListOf<Memory>()
        while (cursor.moveToNext()) {
            memories.add(
                Memory(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MEMORY_ID)),
                    username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    locationName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION_NAME)),
                    image = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_MEMORY_IMAGE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMORY_DESCRIPTION))
                )
            )
        }
        cursor.close()
        db.close()
        return memories
    }

    fun deleteMemory(memoryId: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_MEMORIES, "$COLUMN_MEMORY_ID = ?", arrayOf(memoryId.toString()))
        db.close()
        return result
    }

}
