from flask import Flask, request
from flask import Blueprint
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

from app import db, bcrypt, app
import app.crypto as crypto
import app.utils as utils
from app.models import Session, User, BlacklistToken
from app.helpers import request_values
from app.middleware import middleware, API


auth_blueprint = Blueprint('auth', __name__)
rate_limiter = Limiter(app=app, key_func=get_remote_address)

@auth_blueprint.route("/init", methods=["POST"])
@rate_limiter.limit("10/minute")
@middleware(API.HandleError)
def handle_init():
    client_key = request_values("auth")

    if not client_key or not crypto.is_valid_key(client_key, partial=True):
        return utils.basic_response(400, "Invalid partial key")

    server_key = crypto.create_partial_key()
    session = Session(client_key, server_key)

    db.session.add(session)
    db.session.commit()

    return utils.basic_response(200, values={
        "key": server_key,
        "token": session.create_token()
    })

# Osi muchos decoradores gracias gracias
@auth_blueprint.route("/login", methods=["POST"])
@middleware(API.HandleError, API.CheckSession)
def handle_login(session):
    dec_request = session.decrypt_request("username", "password")
    dec_username, dec_password = dec_request.values()

    user = User.from_username(dec_username)
    if not (user and bcrypt.check_password_hash(user.password, dec_password)):
        return utils.basic_response(401, "Los datos ingresados no son correctos")

    # Asociamos la sesión al usuario
    user.sessions.append(session)
    db.session.commit()

    return utils.basic_response(200)

@auth_blueprint.route("/logout", methods=["POST"])
@middleware(API.HandleError, API.CheckAuth)
def handle_logout(session):
    session.destroy()
    db.session.add(
        BlacklistToken(request.headers["45MinuteToken"])
    )
    db.session.commit()

    return utils.basic_response(200, msg="Shao lo vimo")

@auth_blueprint.route("/changepass", methods=["POST"])
@middleware(API.HandleError, API.CheckAuth)
def handle_changepass(session):
    dec_request = session.decrypt_request("username", "password")
    dec_username, dec_password = dec_request.values()

    s_user = session.user

    # Solo un admin puede cambiar la contraseña de otro usuario
    user = User.from_username(dec_username)
    if dec_username != s_user.username and not s_user.is_admin():
        return utils.basic_response(401)
    user.set_password(dec_password)

    # Destruimos la sesión
    session.destroy()
    db.session.commit()

    return utils.basic_response(200, msg=f"Se han actualizado los datos de '{dec_username}'")

