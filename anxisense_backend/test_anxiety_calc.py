import sys
import os

# Add backend directory to path so we can import app
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from app import calculate_anxiety

def test_scenario(name, emotions):
    score, level = calculate_anxiety(emotions)
    print(f"[{name}]")
    print(f"Emotions: {emotions}")
    print(f"Result: Score = {score}/100, Level = {level}")
    print("-" * 40)

def run_tests():
    print("Running Anxiety Score Calculation Tests...\n")
    
    # Scenario 1: Pure Fear (should be High, near 100)
    test_scenario("Pure Fear", {"fear": 99.9, "happy": 0.1})
    
    # Scenario 2: High Stress (mix of fear, sad, angry)
    test_scenario("High Stress (Mixed)", {"fear": 40.0, "sad": 30.0, "angry": 20.0, "neutral": 10.0})
    
    # Scenario 3: Moderate Stress
    test_scenario("Moderate Stress", {"sad": 50.0, "surprise": 20.0, "neutral": 30.0})
    
    # Scenario 4: Calm / Happy
    test_scenario("Calm/Happy", {"happy": 90.0, "neutral": 10.0})
    
    # Scenario 5: Edge case - all negative maxed (should cap at 100)
    # DeepFace won't output 100 for all, but good to test the cap
    test_scenario("Mathematical Cap Test", {"fear": 100.0, "sad": 100.0})

if __name__ == "__main__":
    run_tests()
