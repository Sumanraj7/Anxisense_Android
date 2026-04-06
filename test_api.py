import requests

BASE_URL = "http://127.0.0.1:5000/api"

# 1. Create Patient
res = requests.post(f"{BASE_URL}/patients", json={
    "doctorid": 1,
    "fullname": "Test Quick Scan Patient",
    "patientid": "P-001",
    "age": "30",
    "gender": "Male",
    "proceduretype": "Checkup",
    "healthissue": "None",
    "previousanxietyhistory": "None"
})

print("CREATE PATIENT:", res.status_code, res.json())

if res.status_code == 201:
    internal_id = res.json()["data"]["id"]
    
    # 2. Save Assessment
    res2 = requests.post(f"{BASE_URL}/assessments", json={
        "patient_id": internal_id,
        "doctor_id": 1,
        "anxiety_score": 54.5,
        "anxiety_level": "Moderate Anxiety",
        "dominant_emotion": "fear"
    })
    
    print("SAVE ASSESSMENT:", res2.status_code, res2.json())
