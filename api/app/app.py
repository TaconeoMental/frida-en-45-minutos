import string

import crypto
import database

from flask import Flask, render_template, request, Response

app = Flask(__name__)
database.init_db()

def is_hex(s):
    return set(s).issubset(string.hexdigits)

@app.route("/init", methods=["POST"])
def handle_init():
    content = request.get_json(silent=True)

    client_uuid = content.get("uuid")
    client_key = content.get("auth")

    if not all([client_uuid, client_key]):
        # TODO: Manejear bien los errores
        return Response(status=400)

    if not is_hex(client_key):
        # Idem xd
        return Response(status=400)

    client_key_bytes = bytes.fromhex(client_key)
    if len(client_key_bytes) != crypto.PARTIAL_KEY_BYTES:
        return Response(status=400)

    shared_key = crypto.create_session_key(client_uuid, client_key_bytes)
    return {"status": "OK", "key": shared_key}, 200

@app.route("/getuser", methods=["POST"])
def handle_action():
    content = request.get_json(silent=True)

    client_uuid = content.get("uuid")
    enc_username = content.get("username")
    enc_role = content.get("role")

    if not all([client_uuid, enc_username, enc_role]):
        return Response(status=400)

    shared_key = database.get_shared_key(client_uuid)
    if not shared_key:
        return Response(status=404)

    cipher = crypto.Cipher(shared_key)

    dec_username = cipher.decrypt(enc_username)
    dec_role = cipher.decrypt(enc_role)

    # Obtenemos el usuario username con los campos censurados dependiendo del
    # valor de role
    user_result = database.get_user(dec_username, dec_role) # Revisar objeto?
    if not user_result:
        return {"status": "Error", "message": "User not found"}, 404

    enc_user = cipher.encrypt_dict(user_result)
    response = {"status": "OK"} | enc_user
    return response, 200

if __name__ == "__name__":
    app.run(host="127.0.0.1", port=8080)
