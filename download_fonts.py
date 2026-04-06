import urllib.request
import os

font_dir = r"c:\Users\suman\AndroidStudioProjects\AnxiSense\app\src\main\res\font"
os.makedirs(font_dir, exist_ok=True)

fonts = {
    "playfair_display_bold.ttf": "https://github.com/google/fonts/raw/main/ofl/playfairdisplay/PlayfairDisplay%5Bwght%5D.ttf",
    "inter_regular.ttf": "https://github.com/google/fonts/raw/main/ofl/inter/Inter%5Bslnt%2Cwght%5D.ttf"
}

for filename, url in fonts.items():
    filepath = os.path.join(font_dir, filename)
    print(f"Downloading {filename}...")
    try:
        urllib.request.urlretrieve(url, filepath)
        print(f"Saved to {filepath}")
    except Exception as e:
        print(f"Failed to download {filename}: {e}")
