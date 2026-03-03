import os
import re

search_dir = r"c:\Users\suman\AndroidStudioProjects\AnxiSense\app\src\main\res\layout"

def fix_contrast(content):
    # Regex to capture a full XML tag from < to >
    pattern = re.compile(r'<([A-Za-z0-9_.]+)([^>]*?)>')
    
    def replace_in_tag(match):
        tag_name = match.group(1)
        attrs = match.group(2)
        
        # Check if background is mint or mint-associated
        mint_bgs = ['#0FFCBE', '@drawable/bg_button_filled', '@drawable/bg_success_banner']
        has_mint_bg = any(bg in attrs for bg in mint_bgs)
        
        if has_mint_bg:
            # Replace white text colors with dark blue (#1A365D)
            attrs = re.sub(r'android:textColor=\"#[fF]{3,6}\"', 'android:textColor="#1A365D"', attrs)
            attrs = re.sub(r'android:textColor=\"@android:color/white\"', 'android:textColor="#1A365D"', attrs)
            attrs = re.sub(r'android:textColor=\"@color/white\"', 'android:textColor="#1A365D"', attrs)
            
            # Also replace any white icons/tints with dark blue
            attrs = re.sub(r'app:tint=\"#[fF]{3,6}\"', 'app:tint="#1A365D"', attrs)
            attrs = re.sub(r'app:tint=\"@android:color/white\"', 'app:tint="#1A365D"', attrs)
            attrs = re.sub(r'app:tint=\"@color/white\"', 'app:tint="#1A365D"', attrs)
            
        return f'<{tag_name}{attrs}>'
    
    return pattern.sub(replace_in_tag, content)

count = 0
for root, dirs, files in os.walk(search_dir):
    for f in files:
        if f.endswith('.xml'):
            filepath = os.path.join(root, f)
            with open(filepath, 'r', encoding='utf-8') as f_in:
                content = f_in.read()
            
            new_content = fix_contrast(content)
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f_out:
                    f_out.write(new_content)
                print(f'Fixed text contrast in {f}')
                count += 1

print(f'Total files fixed: {count}')
