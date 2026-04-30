import json
import sys

for filename in sys.argv[1:]:
    with open(filename, 'r') as f:
        data = json.load(f)
    
    # Custom formatting to match 1.21.1 style
    def format_json(data, indent=0):
        sp = "  " * indent
        if isinstance(data, dict):
            if not data:
                return "{}"
            items = []
            for k, v in data.items():
                items.append(f'{sp}  "{k}": {format_json(v, indent+1)}')
            return "{\n" + ",\n".join(items) + f"\n{sp}}}"
        elif isinstance(data, list):
            if not data:
                return "[]"
            # Special handling for pattern arrays - keep compact
            if all(isinstance(x, str) for x in data):
                return "[" + ", ".join(f'"{x}"' for x in data) + "]"
            items = []
            for item in data:
                items.append(f"{sp}  {format_json(item, indent+1)}")
            return "[\n" + ",\n".join(items) + f"\n{sp}]"
        elif isinstance(data, str):
            return f'"{data}"'
        elif isinstance(data, (int, float)):
            return str(data)
        return str(data)
    
    result = format_json(data)
    with open(filename, 'w') as f:
        f.write(result + "\n")

import glob
files = glob.glob("*.json")
reformat.py(*files)
