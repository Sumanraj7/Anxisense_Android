import urllib.request
import os

font_dir = r"c:\Users\suman\AndroidStudioProjects\AnxiSense\app\src\main\res\font"
filepath = os.path.join(font_dir, "inter_regular.ttf")

# Official Inter repo
url = "https://github.com/rsms/inter/raw/master/docs/font-files/Inter-Regular.ttf"

print("Downloading inter_regular.ttf...")
try:
    urllib.request.urlretrieve(url, filepath)
    print(f"Saved to {filepath}")
except Exception as e:
    print(f"Failed to download: {e}")
