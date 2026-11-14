import json
import trimesh
import os

glb_path = r"c:\Users\Nick\Desktop\Works\Libri\new_anvil_files\anvil_alt.glb"
output_path = r"c:\Users\Nick\Desktop\Works\Libri\src\main\resources\assets\libri\models\block\advanced_anvil.json"

if not os.path.exists(glb_path):
    print("GLB not found!")
    exit(1)

scene = trimesh.load(glb_path, force='scene')

elements = []

for node_name, geom in scene.geometry.items():
    bounds = geom.bounds
    min_b = bounds[0]
    max_b = bounds[1]
    
    # Scale to Minecraft 16x16x16 format roughly.
    # We will compute the global bounding box first to scale.
    
# Compute global bounds
global_bounds = scene.bounds
global_min = global_bounds[0]
global_max = global_bounds[1]

global_size = global_max - global_min
max_dim = max(global_size)
scale = 16.0 / max_dim

for geom in scene.dump():
    geom_t = geom.copy()
    
    bounds = geom_t.bounds
    min_b = (bounds[0] - global_min) * scale
    max_b = (bounds[1] - global_min) * scale
    
    # ensure it fits in [0, 16] but don't restrict too much if it slightly overflows
    elements.append({
        "from": [round(min_b[0], 2), round(min_b[1], 2), round(min_b[2], 2)],
        "to": [round(max_b[0], 2), round(max_b[1], 2), round(max_b[2], 2)],
        "faces": {
            "north": {"uv": [0, 0, 16, 16], "texture": "#0"},
            "east": {"uv": [0, 0, 16, 16], "texture": "#0"},
            "south": {"uv": [0, 0, 16, 16], "texture": "#0"},
            "west": {"uv": [0, 0, 16, 16], "texture": "#0"},
            "up": {"uv": [0, 0, 16, 16], "texture": "#0"},
            "down": {"uv": [0, 0, 16, 16], "texture": "#0"}
        }
    })

model = {
    "parent": "minecraft:block/block",
    "textures": {
        "0": "minecraft:block/iron_block",
        "particle": "minecraft:block/iron_block"
    },
    "elements": elements,
    "display": {
        "gui": {
            "rotation": [30, 225, 0],
            "translation": [0, 0, 0],
            "scale": [0.625, 0.625, 0.625]
        }
    }
}

with open(output_path, 'w') as f:
    json.dump(model, f, indent=2)

print("Converted GLB to JSON with " + str(len(elements)) + " elements.")
