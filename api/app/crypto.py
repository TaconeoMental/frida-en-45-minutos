import hashlib
import secrets
import base64
from Crypto.Cipher import DES3
from Crypto.Util.Padding import pad, unpad

from database import Database

MAIN_KEY_BYTES = 24
PARTIAL_KEY_BYTES = MAIN_KEY_BYTES // 2

def gen_partial_key():
    key_bytes = secrets.token_bytes(PARTIAL_KEY_BYTES)
    return key_bytes

def create_session_key(uuid, client_key):
    # Generamos la llave de la forma más insegura posible :P
    partial_key = gen_partial_key()
    shared_bytes = client_key + partial_key
    shared_key = DES3.adjust_key_parity(shared_bytes).hex()
    #shared_key = hashlib.sha1(shared_bytes).hexdigest()

    # la guardamos en la db
    db = Database()
    db.execute("REPLACE into sessions (uuid, symkey) VALUES(?, ?)", (uuid, shared_key))

    return shared_key

class Cipher:
    def __init__(self, secret_key):
        self.key = secret_key
        self.cipher = DES3.new(secret_key, DES3.MODE_ECB)

    def encrypt(self, plaintext):
        ciphertext_bytes = self.cipher.encrypt(
            pad(plaintext.encode(), DES3.block_size)
        )
        ciphertext = base64.b64encode(ciphertext_bytes).decode()
        return ciphertext

    def decrypt(self, ciphertext):
        ciphertext_bytes = base64.b64decode(ciphertext)
        plaintext_bytes = unpad(
            self.cipher.decrypt(ciphertext_bytes),
            DES3.block_size
        )
        plaintext = plaintext_bytes.decode()
        return plaintext

    def encrypt_dict(self, d):
        for key in d:
            d[key] = self.encrypt(d[key])
        return d

    def decrypt_dict(self, d):
        for key in d:
            d[key] = self.decrypt(d[key])
        return d
