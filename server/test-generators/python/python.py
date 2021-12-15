import random
from pathlib import Path
import sys
import ctypes

seeds = [-1]#[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1634668549]
amount_per_seed_in_byte = 32#16777216
#base_path = Path("../../src/test/resources/random_data/python/").resolve()

print(ctypes.c_uint32(-1).value)
print((-1 + (1 << 32) ))
print("-1 in binary: " + str(bin(-1)))

#base_path.mkdir(parents=True, exist_ok=True)

for s in seeds:
    print(s)
    random.seed(s)
    #f = Path(base_path, str(s) + ".bin").absolute().open("wb")
    #f = open(base_path.absolute() + str(s) + ".txt", "w")
    b = bytes(random.randbytes(amount_per_seed_in_byte))
    print(b.hex())
    #f.write(b)
    #f.close()

    # unsupported: this advances the state after every byte
    #print(bytes([getrandbits(8) for _ in range(0, 32)]).hex())
    # supported:
    #print(getrandbits(32*8).to_bytes(32, "little").hex())
    # unsupported: this advances the state after every byte + often in randrange (internally calls randbelow -> calls randbits multiple times)
    #print(bytes([randrange(0, 256) for _ in range(0, 32)]).hex())
    #print(bytes([randint(0, 255) for _ in range(0, 32)]).hex())
