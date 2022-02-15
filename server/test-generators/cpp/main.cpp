#include <iostream>
#include <algorithm>
#include <new>
#include <cstdint>
#include <bit>
#include <fstream>

using namespace std;

int64_t initial_seed_;
uint64_t state0_;
uint64_t state1_;

static inline void XorShift128(uint64_t* state0, uint64_t* state1) {
    uint64_t s1 = *state0;
    uint64_t s0 = *state1;
    *state0 = s0;
    s1 ^= s1 << 23;
    s1 ^= s1 >> 17;
    s1 ^= s0;
    s1 ^= s0 >> 26;
    *state1 = s1;
}


uint64_t MurmurHash3(uint64_t h) {
    h ^= h >> 33;
    h *= uint64_t{0xFF51AFD7ED558CCD};
    h ^= h >> 33;
    h *= uint64_t{0xC4CEB9FE1A85EC53};
    h ^= h >> 33;
    return h;
}

void SetSeed(int64_t seed) {
    initial_seed_ = seed;
    state0_ = MurmurHash3(std::bit_cast<uint64_t>(seed));
    state1_ = MurmurHash3(~state0_);
}

int Next(int bits) {
    XorShift128(&state0_, &state1_);
    return static_cast<int>((state0_ + state1_) >> (64 - bits));
}

int64_t NextInt64() {
    XorShift128(&state0_, &state1_);
    return std::bit_cast<int64_t>(state0_ + state1_);
}

void NextBytes(void* buffer, size_t buflen) {
    for (size_t n = 0; n < buflen; ++n) {
        static_cast<uint8_t*>(buffer)[n] = static_cast<uint8_t>(Next(8));
    }
}

bool to_hex(char* dest, size_t dest_len, const uint8_t* values, size_t val_len) {
    if(dest_len < (val_len*2+1)) /* check that dest is large enough */
        return false;
    *dest = '\0'; /* in case val_len==0 */
    while(val_len--) {
        /* sprintf directly to where dest points */
        sprintf(dest, "%02X", *values);
        dest += 2;
        ++values;
    }
    return true;
}

// first  arg: seed
// second arg: bytes
// third  arg: absolute path
int main(int argc, char *argv[]) {

    int64_t seed = atoi(argv[1]);
    int64_t bytes = atoi(argv[2]);
    string path = argv[3];

    SetSeed(seed);
    uint8_t array [bytes];
    NextBytes(array, sizeof(array));

    ofstream fout;
    fout.open(path + "/" + to_string(seed) + ".bin", ios::binary | ios::out);

    fout.write((char*) &array, sizeof(array));

    fout.close();
    return 0;
}
