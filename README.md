# pureskin-skincare-app
PureSkin is an Android app for personalized skincare. It combines a Kotlin frontend, Python Flask backend, and MySQL database with knowledge-based methods and Random Forest ML. Users get tailored product recommendations, routine management, progress tracking, plus a virtual assistant via Touch AI and Google Custom Search.

**Setup and Configuration**

**Backend**

1. Clone the repository:

```
git clone https://github.com/FloreaSanzaiana/pureskin-skincare-app.git
cd PureSkin/backend
```

2. Create the .env file in the backend/ folder with the following variables:
```
SEARCH_ENGINE=your_google_cx
GOOGLE_API_KEY=your_google_api_key
CHAT_API_KEY=your_chat_api_key
EMAIL_SENDER=your_email
EMAIL_PASSWORD=your_email_password
SECRET_KEY=your_secret_key
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=
DB_NAME=pureskin
SERVER_URL=http://localhost:5000
```

3. Create the database tables:

```
mysql -u root -p < create_tables.sql
```

4. If you want to recreate the Random Forest model (optional):

```
python train_model/random_forest.py
```

5. Start the server:

```
python app.py
```


**Mobile App**

6. In RetrofitInstance.kt or your app configuration file, update the server URL (BASE_URL) to point to your backend:

```
private const val BASE_URL = "http://localhost:5000"
```

7. Run the app on an emulator or a real device.


**Main Features**

- User registration and authentication

- Password reset via email

- Add and edit products in daily routines

- Personalized product recommendations

- Chatbot for skincare-related questions

- View routine history and daily logs
