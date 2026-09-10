import os
import joblib
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.metrics import mean_absolute_error, r2_score
from dataset import generate_historical_queue_data

MODEL_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_FILE = os.path.join(MODEL_DIR, "wait_time_model.joblib")
CSV_FILE = os.path.join(MODEL_DIR, "historical_queue_data.csv")

def train_and_save_model():
    if not os.path.exists(CSV_FILE):
        df = generate_historical_queue_data()
    else:
        df = pd.read_csv(CSV_FILE)

    X = df[[
        'department',
        'queue_position',
        'patients_ahead',
        'hour_of_day',
        'day_of_week',
        'avg_consultation_time',
        'active_priority_count',
        'historical_delay_factor',
        'no_show_rate'
    ]]
    y = df['actual_wait_minutes']

    categorical_features = ['department']
    numerical_features = [
        'queue_position',
        'patients_ahead',
        'hour_of_day',
        'day_of_week',
        'avg_consultation_time',
        'active_priority_count',
        'historical_delay_factor',
        'no_show_rate'
    ]

    preprocessor = ColumnTransformer(
        transformers=[
            ('cat', OneHotEncoder(handle_unknown='ignore'), categorical_features),
            ('num', 'passthrough', numerical_features)
        ]
    )

    pipeline = Pipeline(steps=[
        ('preprocessor', preprocessor),
        ('regressor', RandomForestRegressor(n_estimators=100, max_depth=12, random_state=42))
    ])

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    pipeline.fit(X_train, y_train)

    y_pred = pipeline.predict(X_test)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)

    print(f"Model Training Complete. MAE: {mae:.2f} mins, R2 Score: {r2:.4f}")

    metrics = {
        'mae': round(float(mae), 2),
        'r2': round(float(r2), 4),
        'test_samples': len(y_test)
    }

    payload = {
        'pipeline': pipeline,
        'metrics': metrics
    }

    joblib.dump(payload, MODEL_FILE)
    return payload

def load_or_train_model():
    if os.path.exists(MODEL_FILE):
        try:
            return joblib.load(MODEL_FILE)
        except Exception as e:
            print(f"Failed loading existing model: {e}. Retraining...")
            return train_and_save_model()
    else:
        return train_and_save_model()

def predict_wait_time(
    department: str,
    queue_position: int,
    patients_ahead: int,
    hour_of_day: int = 10,
    day_of_week: int = 1,
    avg_consultation_time: float = 15.0,
    active_priority_count: int = 0,
    historical_delay_factor: float = 1.0,
    no_show_rate: float = 0.1
):
    model_data = load_or_train_model()
    pipeline = model_data['pipeline']

    input_df = pd.DataFrame([{
        'department': department,
        'queue_position': queue_position,
        'patients_ahead': max(0, patients_ahead),
        'hour_of_day': hour_of_day,
        'day_of_week': day_of_week,
        'avg_consultation_time': avg_consultation_time,
        'active_priority_count': active_priority_count,
        'historical_delay_factor': historical_delay_factor,
        'no_show_rate': no_show_rate
    }])

    predicted_val = float(pipeline.predict(input_df)[0])
    predicted_val = max(0.0, round(predicted_val, 1))

    # Calculate range bound (e.g. ±15% or min 5 min margin)
    margin = max(3.0, round(predicted_val * 0.12, 1))
    min_wait = max(0.0, round(predicted_val - margin, 1))
    max_wait = round(predicted_val + margin, 1)

    # Estimate confidence score
    confidence = min(0.98, max(0.80, round(1.0 - (margin / (predicted_val + 10.0)), 2)))

    return {
        'predicted_wait_minutes': predicted_val,
        'min_wait_minutes': min_wait,
        'max_wait_minutes': max_wait,
        'confidence_score': confidence,
        'formatted_range': f"{int(min_wait)}–{int(max_wait)} mins" if min_wait > 0 else f"{int(max_wait)} mins"
    }

if __name__ == "__main__":
    train_and_save_model()
