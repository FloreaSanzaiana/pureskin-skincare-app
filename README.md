# pureskin-skincare-app
PureSkin is an Android app for personalized skincare. It combines a Kotlin frontend, Python Flask backend, and MySQL database with knowledge-based methods and Random Forest ML. Users get tailored product recommendations, routine management, progress tracking, plus a virtual assistant via Touch AI and Google Custom Search.
Setup și configurare
Backend

Clonează repository-ul:

git clone https://github.com/FloreaSanzaiana/pureskin-skincare-app.git
cd PureSkin/backend

Creează fișierul .env în folderul backend/ cu următoarele variabile:

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

Acest fișier nu trebuie urcat pe GitHub pentru a proteja datele sensibile.

Crează tabelele în baza de date:

mysql -u root -p < create_tables.sql

Dacă vrei să recreezi modelul Random Forest (opțional):

python models/train_model.py

Pornește serverul:

python app.py
Mobile App

În RetrofitInstance.kt sau fișierul de configurare al aplicației, modifică URL-ul serverului (BASE_URL) pentru a indica backend-ul tău:

private const val BASE_URL = "http://localhost:5000"

Rulează aplicația pe emulator sau pe device real.

Funcționalități principale

Înregistrare și autentificare utilizatori

Resetare parolă prin email

Adăugare și editare produse în rutina zilnică

Recomandări personalizate de produse

Chatbot pentru întrebări legate de skincare

Vizualizare istoricul rutinei și al logurilor zilnice
