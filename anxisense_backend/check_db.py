import MySQLdb
import os
from dotenv import load_dotenv

load_dotenv()

try:
    db = MySQLdb.connect(
        host=os.getenv("DB_HOST"),
        user=os.getenv("DB_USER"),
        passwd=os.getenv("DB_PASSWORD"),
        db=os.getenv("DB_NAME")
    )
    cursor = db.cursor()
    
    print("\n--- Doctors Columns ---")
    try:
        cursor.execute("DESCRIBE doctors")
        columns = cursor.fetchall()
        for col in columns:
            print(f"{col[0]} - {col[1]}")
    except Exception as e:
        print(f"Error describing doctors: {e}")

    cursor.close()
    db.close()

except Exception as e:
    print(f"Connection failed: {e}")
