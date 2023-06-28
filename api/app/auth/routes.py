from flask import Flask, request, Response
from flask import Blueprint

from app import db
import app.crypto as crypto
import app.utils as utils
from .helpers import requires_token


main = Blueprint('main', __name__)

@main.route("/init", methods=["POST"])
def handle_init():
    content = request.get_json(silent=True)
    client_key = content.get("auth")

    if not client_key or not crypto.is_valid_key(client_key, partial=True):
        return utils.basic_response(400)

    server_key = crypto.create_partial_key()
    session = crypto.create_session(client_key, server_key)

    token = session.gen_token()

    status_code = 200
    return {
        "status": utils.status_code_text(status_code),
        "key": server_key,
        "token": token
    }, status_code


@main.route("/getuser", methods=["POST"])
@requires_token
def handle_getuser(uuid):
    """
    content = request.get_json(silent=True)

    client_uuid = content.get("uuid")
    enc_username = content.get("username")
    enc_role = content.get("role")

    if not all([client_uuid, enc_username, enc_role]):
        return utils.basic_response(400)

    shared_key = database.get_shared_key(client_uuid)
    if not shared_key:
        return Response(status=404)

    cipher = crypto.Cipher(shared_key)

    dec_username = cipher.decrypt(enc_username)
    dec_role = cipher.decrypt(enc_role)

    user_result = database.get_user(dec_username, dec_role)
    if not user_result:
        return utils.basic_response(400, "User not found")

    return cipher.build_response(200, user_results)
    """
    return "getuser"

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8080)
