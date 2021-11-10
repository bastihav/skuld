from random import *
from pathlib import Path
import sys

seeds = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1634668549]
amount_per_seed_in_byte = 16777216
base_path = Path("../../src/test/resources/random_data/python/").resolve()

base_path.mkdir(parents=True, exist_ok=True)

for s in seeds:
    print(s)
    seed(s)
    print()
    
    f = Path(base_path, str(s) + ".bin").absolute().open("wb")
    #f = open(base_path.absolute() + str(s) + ".txt", "w")
    f.write(bytes(randbytes(amount_per_seed_in_byte)))
    f.close()

    # unsupported: this advances the state after every byte
    #print(bytes([getrandbits(8) for _ in range(0, 32)]).hex())
    # supported:
    #print(getrandbits(32*8).to_bytes(32, "little").hex())
    # unsupported: this advances the state after every byte + often in randrange (internally calls randbelow -> calls randbits multiple times)
    #print(bytes([randrange(0, 256) for _ in range(0, 32)]).hex())
    #print(bytes([randint(0, 255) for _ in range(0, 32)]).hex())
