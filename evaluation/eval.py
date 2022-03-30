import docker
import time
import subprocess


prngs = ["JAVA_RANDOM", "XORSHIFT128PLUS", "MERSENNE_TWISTER_PYTHON", "XOSHIRO128STARSTAR", "XOSHIRO256STARSTAR", "PCG32", "GOLANGLCG", "KNUTH", "GLIBCLCG", "CHA_CHA_12", "CHA_CHA_8", "CHA_CHA_20"]
#prngs = ["CHA_CHA_12", "CHA_CHA_8", "CHA_CHA_20"]
seed = 1

client = docker.from_env()

for prng in prngs:
    #start docker
    command = "-p " + prng + " -s " + str(seed)
    container = client.containers.run("tls_bad_prng:latest", command=command , detach=True, ports={"443/tcp":443})
    time.sleep(3) #3 seconds

    for _ in range(20):
        #p = subprocess.call(['java', '-jar', 'C:/Users/basti/Documents/WHB/tls_attacker_stuff/TLS-Scanner-Development/apps/TLS-Server-Scanner.jar -connect localhost:443 -threads 4 -noColor'])
        p = subprocess.Popen(['java', '-jar', 'C:/Users/basti/Documents/WHB/tls_attacker_stuff/TLS-Scanner-Development/apps/TLS-Server-Scanner.jar', '-connect', 'localhost:443', '-noColor'])
        time.sleep(10) #5 seconds
        p.kill()

    container.stop()