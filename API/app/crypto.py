import hashlib
import secrets
import base64
from Crypto.Cipher import DES3
from Crypto.Util.Padding import pad, unpad

from . import db
import app.utils as utils

MAIN_KEY_BYTES = 24
PARTIAL_KEY_BYTES = MAIN_KEY_BYTES // 2

# Helpers...

def create_partial_key():
    return secrets.token_hex(PARTIAL_KEY_BYTES)

def is_valid_key(key, partial=False):
    length = PARTIAL_KEY_BYTES if partial else MAIN_KEY_BYTES
    return utils.is_hex(key) and len(bytes.fromhex(key)) == length

def build_shared_key(client_key, server_key):
    # Generamos la llave de la forma más insegura posible
    shared_bytes = bytes.fromhex(client_key + server_key)
    shared_key = DES3.adjust_key_parity(shared_bytes).hex()
    return shared_key

class Cipher:
    def __init__(self, secret_key):
        self.key = secret_key
        self.cipher = DES3.new(secret_key, DES3.MODE_ECB)

    def _encrypt(self, plaintext):
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

    def encrypt(self, v):
        enc_val = None
        if type(v) == str:
            enc_val = self._encrypt(v)
        elif type(v) == dict:
            enc_val = dict()
            for key, val in v.items():
                enc_val[key] = self.encrypt(val)
        elif type(v) == list:
            enc_val = [self.encrypt(i) for i in v]
        return enc_val

    # TODO
    def decrypt_dict(self, d):
        return {k: self.decrypt(v) for k, v in d.items()}
