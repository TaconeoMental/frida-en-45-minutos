from flask import Flask, request, Response
from flask import Blueprint

from app import db, bcrypt
import app.crypto as crypto
import app.utils as utils
from app.database.models import Session, User
from .helpers import requires_token, handles_server_error


main = Blueprint('main', __name__)

@main.route("/init", methods=["POST"])
@handles_server_error
def handle_init():
    content = request.get_json(silent=True)
    client_key = content.get("auth")

    if not client_key or not crypto.is_valid_key(client_key, partial=True):
        return utils.basic_response(400, "Invalid partial key")

    server_key = crypto.create_partial_key()
    session = crypto.create_session(client_key, server_key)

    token = session.gen_token()

    return utils.basic_response(200, values={
        "key": server_key,
        "token": token
    })

# Osi muchos decoradores gracias gracias
@main.route("/login", methods=["POST"])
@requires_token(authenticated=False)
@handles_server_error
def handle_login(uuid):
    content = request.get_json(silent=True)

    enc_username = content.get("username")
    enc_password = content.get("password")

    session = Session.query.filter_by(
        uuid=uuid
    ).first()

    if not all([enc_username, enc_password, session]):
        return utils.basic_response(400)

    shared_key = session.shared_key
    print(shared_key)
    cipher = crypto.Cipher(bytes.fromhex(shared_key))

    dec_username = cipher.decrypt(enc_username)
    dec_password = cipher.decrypt(enc_password)

    user = User.query.filter_by(
        username=dec_username
    ).first()

    if not (user and bcrypt.check_password_hash(user.password, dec_password)):
        return utils.basic_response(401, "Los datos ingresados no son correctos")

    # Asociamos la sesión al usuario
    user.sessions.append(session)
    db.session.commit()

    return utils.basic_response(200)

@main.route("/changepass", methods=["POST"])
@requires_token(authenticated=True)
@handles_server_error
def handle_changepass(uuid):
    content = request.get_json(silent=True)

    enc_username = content.get("username")
    enc_password = content.get("password")

    session = Session.query.filter_by(
        uuid=uuid
    ).first()

    if not all([enc_username, enc_password, session]):
        return utils.basic_response(400)

    shared_key = session.shared_key
    cipher = crypto.Cipher(bytes.fromhex(shared_key))

    dec_username = cipher.decrypt(enc_username)
    dec_password = cipher.decrypt(enc_password)

    session.user.set_password(dec_password)
    db.session.commit()

    return utils.basic_response(200, msg=f"Se han actualizado los datos de '{dec_username}'")

