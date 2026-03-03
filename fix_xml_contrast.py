import os
import xml.etree.ElementTree as ET

search_dir = r"c:\Users\suman\AndroidStudioProjects\AnxiSense\app\src\main\res\layout"

# Register namespaces so the output XML maintains standard prefixes
ET.register_namespace('android', 'http://schemas.android.com/apk/res/android')
ET.register_namespace('app', 'http://schemas.android.com/apk/res-auto')
ET.register_namespace('tools', 'http://schemas.android.com/tools')

NS_ANDROID = '{http://schemas.android.com/apk/res/android}'
NS_APP = '{http://schemas.android.com/apk/res-auto}'

MINT_BACKGROUNDS = ['#0FFCBE', '@drawable/bg_button_filled', '@drawable/bg_success_banner', '@color/mint_accent']

def has_mint_bg(node):
    bg = node.get(f'{NS_ANDROID}background', '')
    bgTint = node.get(f'{NS_ANDROID}backgroundTint', '')
    appBgTint = node.get(f'{NS_APP}backgroundTint', '')
    cardBg = node.get(f'{NS_APP}cardBackgroundColor', '')
    
    for mb in MINT_BACKGROUNDS:
        if bg == mb or bgTint == mb or appBgTint == mb or cardBg == mb:
            return True
    return False

def fix_contrast(node, in_mint_context):
    changed = False
    
    current_is_mint = has_mint_bg(node)
    
    if in_mint_context or current_is_mint:
        # Replace textColors
        tc = node.get(f'{NS_ANDROID}textColor')
        if tc in ['#FFF', '#FFFFFF', '@android:color/white', '@color/white']:
            node.set(f'{NS_ANDROID}textColor', '#106EBE') # Blue Primary
            changed = True
            
        # Replace tints
        tint1 = node.get(f'{NS_ANDROID}tint')
        tint2 = node.get(f'{NS_APP}tint')
        drawableTint = node.get(f'{NS_ANDROID}drawableTint')
        
        if tint1 in ['#FFF', '#FFFFFF', '@android:color/white', '@color/white']:
            node.set(f'{NS_ANDROID}tint', '#106EBE')
            changed = True
        if tint2 in ['#FFF', '#FFFFFF', '@android:color/white', '@color/white']:
            node.set(f'{NS_APP}tint', '#106EBE')
            changed = True
        if drawableTint in ['#FFF', '#FFFFFF', '@android:color/white', '@color/white']:
            node.set(f'{NS_ANDROID}drawableTint', '#106EBE')
            changed = True
            
        # progress bar indeterminateTint
        if node.get(f'{NS_ANDROID}indeterminateTint') in ['#FFF', '#FFFFFF']:
            node.set(f'{NS_ANDROID}indeterminateTint', '#106EBE')
            changed = True
            
        # src replacement for standard white checkmark
        src = node.get(f'{NS_ANDROID}src')
        if src == '@drawable/ic_check_circle_white':
            node.set(f'{NS_APP}tint', '#106EBE') # Tint the white icon blue
            changed = True

    for child in node:
        if fix_contrast(child, in_mint_context or current_is_mint):
            changed = True
            
    return changed

count = 0
for root, dirs, files in os.walk(search_dir):
    for f in files:
        if f.endswith('.xml'):
            filepath = os.path.join(root, f)
            try:
                tree = ET.parse(filepath)
                root_node = tree.getroot()
                if fix_contrast(root_node, False):
                    tree.write(filepath, encoding='utf-8', xml_declaration=True)
                    print(f'Fixed {f}')
                    count += 1
            except Exception as e:
                print(f"Error parsing {f}: {e}")

print(f"Total files fixed: {count}")
