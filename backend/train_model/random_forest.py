
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, r2_score
import joblib
import time


def train_and_save_model(dataset_path='skincare_dataset_normalized.csv',
                         model_save_path='../skincare_rf_model2.pkl'):

    try:
        df = pd.read_csv(dataset_path)
        print(f" {df.shape[0]} randuri    {df.shape[1]} coloane")
    except FileNotFoundError:

        return False


    X = df.drop('score_normalized', axis=1)
    y = df['score_normalized']

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )


    model = RandomForestRegressor(
        n_estimators=100,  # nr de arbori
        max_depth=20,  # adancime maxima
        min_samples_split=5,  # min samples pt split
        min_samples_leaf=2,  # min samples in frunza
        random_state=42,  #pt reproducibilitate
        n_jobs=-1,  # foloseste toate core-urile
        verbose=1  # arata progresul
    )

    start_time = time.time()
    model.fit(X_train, y_train)
    training_time = time.time() - start_time
    y_pred = model.predict(X_test)

    print("top 10 cele mai importante atribute:")
    feature_importance = pd.DataFrame({
        'feature': X.columns,
        'importance': model.feature_importances_
    }).sort_values('importance', ascending=False)

    for i, row in feature_importance.head(10).iterrows():
        print(f"   {i + 1}. {row['feature']}: {row['importance']:.4f}")


    joblib.dump(model, model_save_path)
    y_pred = model.predict(X_test)

    mse = mean_squared_error(y_test, y_pred)
    rmse = np.sqrt(mse)
    r2 = r2_score(y_test, y_pred)

    print(f"R^2 Score: {r2:.4f} ")
    print(f"RMSE: {rmse:.2f} puncte")
    print(f"Eroarea medie absoluta: {np.mean(np.abs(y_test - y_pred)):.2f} puncte")
    loaded_model = joblib.load(model_save_path)
    test_prediction = loaded_model.predict(X_test.iloc[:1])

    return True


if __name__ == "__main__":
        pd.read_csv('skincare_dataset_normalized.csv')
        train_and_save_model()

import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, r2_score
import joblib
import time


def train_and_save_model(dataset_path='skincare_dataset_normalized.csv',
                         model_save_path='../skincare_rf_model2.pkl'):

    try:
        df = pd.read_csv(dataset_path)
        print(f" {df.shape[0]} randuri    {df.shape[1]} coloane")
    except FileNotFoundError:

        return False


    X = df.drop('score_normalized', axis=1)
    y = df['score_normalized']

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )


    model = RandomForestRegressor(
        n_estimators=100,  # nr de arbori
        max_depth=20,  # adancime maxima
        min_samples_split=5,  # min samples pt split
        min_samples_leaf=2,  # min samples in frunza
        random_state=42,  #pt reproducibilitate
        n_jobs=-1,  # foloseste toate core-urile
        verbose=1  # arata progresul
    )

    start_time = time.time()
    model.fit(X_train, y_train)
    training_time = time.time() - start_time
    y_pred = model.predict(X_test)

    print("top 10 cele mai importante atribute:")
    feature_importance = pd.DataFrame({
        'feature': X.columns,
        'importance': model.feature_importances_
    }).sort_values('importance', ascending=False)

    for i, row in feature_importance.head(10).iterrows():
        print(f"   {i + 1}. {row['feature']}: {row['importance']:.4f}")


    joblib.dump(model, model_save_path)
    y_pred = model.predict(X_test)

    mse = mean_squared_error(y_test, y_pred)
    rmse = np.sqrt(mse)
    r2 = r2_score(y_test, y_pred)

    print(f"R^2 Score: {r2:.4f} ")
    print(f"RMSE: {rmse:.2f} puncte")
    print(f"Eroarea medie absoluta: {np.mean(np.abs(y_test - y_pred)):.2f} puncte")
    loaded_model = joblib.load(model_save_path)
    test_prediction = loaded_model.predict(X_test.iloc[:1])

    return True


if __name__ == "__main__":
        pd.read_csv('skincare_dataset_normalized.csv')
        train_and_save_model()
