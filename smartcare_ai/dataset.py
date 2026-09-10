import numpy as np
import pandas as pd
import os

def generate_historical_queue_data(num_samples=2500, random_seed=42):
    """
    Generates synthetic operational hospital appointment and queue data
    for training the SmartCare AI waiting-time prediction model.
    """
    np.random.seed(random_seed)

    departments = ['Cardiology', 'Pediatrics', 'Orthopedics', 'Dermatology', 'General Medicine', 'Neurology']
    doc_ids = [f"DOC_{100 + i}" for i in range(15)]
    
    data = []
    for _ in range(num_samples):
        doc_id = np.random.choice(doc_ids)
        department = np.random.choice(departments)
        
        # Base consultation time per department (minutes)
        dept_base_time = {
            'Cardiology': 20,
            'Pediatrics': 15,
            'Orthopedics': 18,
            'Dermatology': 12,
            'General Medicine': 15,
            'Neurology': 25
        }
        avg_consultation_time = dept_base_time[department] + np.random.normal(0, 3)
        avg_consultation_time = max(8.0, round(avg_consultation_time, 1))
        
        queue_position = np.random.randint(1, 25)
        patients_ahead = queue_position - 1
        
        hour_of_day = np.random.choice([8, 9, 10, 11, 12, 14, 15, 16, 17], p=[0.1, 0.18, 0.18, 0.15, 0.09, 0.12, 0.1, 0.05, 0.03])
        day_of_week = np.random.randint(0, 6) # 0=Monday, 5=Saturday
        
        active_priority_count = np.random.choice([0, 1, 2, 3], p=[0.70, 0.20, 0.07, 0.03])
        historical_delay_factor = round(np.random.uniform(0.8, 1.5), 2)
        no_show_rate = round(np.random.uniform(0.05, 0.20), 2)
        
        # Calculate actual wait time based on realistic non-linear operational factors
        # Base wait = patients_ahead * avg_consultation_time
        base_wait = patients_ahead * avg_consultation_time
        
        # Extra delay from priority cases ahead (adds approx 1.2x consultation time per priority patient)
        priority_delay = active_priority_count * avg_consultation_time * 1.15
        
        # Hour of day penalty (peak hours 9-11 AM have higher backlogs)
        peak_multiplier = 1.25 if hour_of_day in [9, 10, 11] else (1.1 if hour_of_day in [14, 15] else 1.0)
        
        # Monday multiplier (Mondays have higher traffic)
        day_multiplier = 1.15 if day_of_week == 0 else 1.0
        
        # No-shows reduce waiting time (patients skipped)
        effective_patients_ahead = patients_ahead * (1.0 - (no_show_rate * 0.5))
        
        actual_wait = (effective_patients_ahead * avg_consultation_time + priority_delay) * peak_multiplier * day_multiplier * historical_delay_factor
        actual_wait += np.random.normal(0, 3.5) # Natural variance
        actual_wait = max(0.0, round(actual_wait, 1))
        
        data.append({
            'doc_id': doc_id,
            'department': department,
            'queue_position': queue_position,
            'patients_ahead': patients_ahead,
            'hour_of_day': hour_of_day,
            'day_of_week': day_of_week,
            'avg_consultation_time': avg_consultation_time,
            'active_priority_count': active_priority_count,
            'historical_delay_factor': historical_delay_factor,
            'no_show_rate': no_show_rate,
            'actual_wait_minutes': actual_wait
        })

    df = pd.DataFrame(data)
    out_dir = os.path.dirname(os.path.abspath(__file__))
    file_path = os.path.join(out_dir, "historical_queue_data.csv")
    df.to_csv(file_path, index=False)
    print(f"Generated {num_samples} historical queue samples at {file_path}")
    return df

if __name__ == "__main__":
    generate_historical_queue_data()
