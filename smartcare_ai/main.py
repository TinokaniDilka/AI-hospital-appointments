from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional
from model import predict_wait_time, train_and_save_model, load_or_train_model

app = FastAPI(
    title="SmartCare AI Service",
    description="Machine Learning service predicting patient estimated waiting times in hospital queues.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class WaitTimePredictionRequest(BaseModel):
    doctor_id: Optional[str] = "DOC_101"
    department: str = Field(default="General Medicine", json_schema_extra={"example": "Cardiology"})
    queue_position: int = Field(default=5, ge=1, json_schema_extra={"example": 5})
    patients_ahead: int = Field(default=4, ge=0, json_schema_extra={"example": 4})
    hour_of_day: Optional[int] = Field(default=10, ge=0, le=23, json_schema_extra={"example": 10})
    day_of_week: Optional[int] = Field(default=1, ge=0, le=6, json_schema_extra={"example": 1})
    avg_consultation_time: Optional[float] = Field(default=15.0, ge=1.0, json_schema_extra={"example": 15.0})
    active_priority_count: Optional[int] = Field(default=0, ge=0, json_schema_extra={"example": 1})
    historical_delay_factor: Optional[float] = Field(default=1.0, ge=0.5, le=3.0, json_schema_extra={"example": 1.1})
    no_show_rate: Optional[float] = Field(default=0.1, ge=0.0, le=0.5, json_schema_extra={"example": 0.1})

class WaitTimePredictionResponse(BaseModel):
    predicted_wait_minutes: float
    min_wait_minutes: float
    max_wait_minutes: float
    confidence_score: float
    formatted_range: str
    status: str = "SUCCESS"

@app.get("/")
def read_root():
    return {
        "service": "SmartCare AI Engine",
        "status": "ONLINE",
        "docs": "/docs"
    }

@app.get("/health")
def health_check():
    return {"status": "HEALTHY", "service": "SmartCare AI"}

@app.post("/predict-wait-time", response_model=WaitTimePredictionResponse)
def predict(req: WaitTimePredictionRequest):
    try:
        res = predict_wait_time(
            department=req.department,
            queue_position=req.queue_position,
            patients_ahead=req.patients_ahead,
            hour_of_day=req.hour_of_day or 10,
            day_of_week=req.day_of_week or 1,
            avg_consultation_time=req.avg_consultation_time or 15.0,
            active_priority_count=req.active_priority_count or 0,
            historical_delay_factor=req.historical_delay_factor or 1.0,
            no_show_rate=req.no_show_rate or 0.1
        )
        return res
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")

@app.post("/retrain")
def retrain_model():
    try:
        res = train_and_save_model()
        return {
            "status": "SUCCESS",
            "message": "AI model retrained successfully",
            "metrics": res['metrics']
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Retraining failed: {str(e)}")

@app.get("/metrics")
def get_metrics():
    try:
        model_data = load_or_train_model()
        return {
            "status": "SUCCESS",
            "metrics": model_data.get('metrics', {})
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Metrics retrieval failed: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
