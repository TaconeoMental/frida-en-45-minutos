from http.server import BaseHTTPRequestHandler, HTTPServer
import urllib.parse
import json
import os
import hashlib
import base64
from Crypto.Cipher import DES3
from Crypto.Util.Padding import pad, unpad
from binascii import unhexlify
from Crypto.Util.strxor import strxor


secretKey = b""

def generate_server_key():
    key_size = 192
    server_key = os.urandom(key_size // 8)
    combined_key = server_key
    hashed_key = hashlib.sha256(combined_key).digest()
    server_key = hashed_key[:12]
    server_key_hex = server_key.hex()
    return server_key_hex

def concat_arrays(a, b):
    result = bytearray(len(a) + len(b))
    result[:len(a)] = a
    result[len(a):] = b
    return result


def combine_key_parts(client_key_part, server_key_part):
    client_key_bytes = unhexlify(client_key_part)
    server_key_bytes = unhexlify(server_key_part)
    combined_key_bytes = concat_arrays(client_key_bytes, server_key_bytes)
    secret_key = DES3.adjust_key_parity(combined_key_bytes)
    return secret_key

def encrypt(plaintext, secret_key):
    cipher = DES3.new(secret_key, DES3.MODE_ECB)
    ciphertext_bytes = cipher.encrypt(pad(plaintext.encode(), DES3.block_size))
    ciphertext = base64.b64encode(ciphertext_bytes).decode()
    return ciphertext


def decrypt(ciphertext, secret_key):
    cipher = DES3.new(secret_key, DES3.MODE_ECB)
    ciphertext_bytes = base64.b64decode(ciphertext)
    plaintext_bytes = unpad(cipher.decrypt(ciphertext_bytes), DES3.block_size)
    plaintext = plaintext_bytes.decode()
    return plaintext


class MyRequestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        global secretKey
        
        if self.path == '/getinfo':
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length).decode('utf-8')
            json_data = json.loads(post_data)

            if 'username' in json_data and 'role' in json_data:
                username = json_data['username']
                role = json_data['role']

                bio = "lorem ipsum dolor sit amet"
                bio = encrypt(bio, secretKey)

                if decrypt(username, secretKey) == "admin" and decrypt(role, secretKey) != "admin":
                    username = encrypt("you are not admin", secretKey)
                    role = encrypt("you are not admin", secretKey)
                    bio = encrypt("you are not admin", secretKey)

                if decrypt(username, secretKey) == "admin" and decrypt(role, secretKey) == "admin":
                    username = encrypt("admin", secretKey)
                    role = encrypt("admin", secretKey)
                    bio = encrypt("you are the admin", secretKey)
                
                
                response_data = {
                    'username': username,
                    'role': role,
                    'bio': bio
                }
                response = json.dumps(response_data)

                self.send_response(200)
                self.send_header('Content-type', 'application/json')
                self.end_headers()
                self.wfile.write(response.encode('utf-8'))
            else:
                self.send_response(400)
                self.send_header('Content-type', 'text/plain')
                self.end_headers()
                self.wfile.write(b'Missing parameters')

        elif self.path == '/init':
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length).decode('utf-8')
            json_data = json.loads(post_data)

            if 'keyExchange' in json_data:
                clientKey = json_data['keyExchange']
                print("Client Key:", clientKey)
                print("Client Key Length:", len(clientKey))

                serverKey = generate_server_key()
                response_data = {
                    'keyExchange': serverKey
                }

                secretKey = combine_key_parts(clientKey, serverKey)
                response = json.dumps(response_data)

                self.send_response(200)
                self.send_header('Content-type', 'application/json')
                self.end_headers()
                self.wfile.write(response.encode('utf-8'))
            else:
                self.send_response(400)
                self.send_header('Content-type', 'text/plain')
                self.end_headers()
                self.wfile.write(b'Missing parameters')
        else:
            self.send_response(404)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(b'Not found')


def run_server():
    host = ''
    port = 8000
    server_address = (host, port)
    httpd = HTTPServer(server_address, MyRequestHandler)
    print(f"Server running on {host}:{port}")
    httpd.serve_forever()

if __name__ == '__main__':
    run_server()
