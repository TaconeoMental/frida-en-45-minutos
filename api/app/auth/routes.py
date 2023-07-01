from flask import Flask, request, Response
from flask import Blueprint

from app import db, bcrypt
import app.crypto as crypto
import app.utils as utils
from app.database.models import Session, User, BlacklistToken

from .helpers import request_values
from .middleware import middleware, API

main = Blueprint('main', __name__)

@main.route("/init", methods=["POST"])
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
@main.route("/login", methods=["POST"])
@middleware(API.HandleError, API.CheckSession)
def handle_login(session):
    dec_request = session.decrypt_request("username", "password")
    dec_username, dec_password = dec_request.values()

    user = User.query.filter_by(
        username=dec_username
    ).first()

    if not (user and bcrypt.check_password_hash(user.password, dec_password)):
        return utils.basic_response(401, "Los datos ingresados no son correctos")

    # Asociamos la sesión al usuario
    user.sessions.append(session)
    db.session.commit()

    return utils.basic_response(200)

@main.route("/logout", methods=["POST"])
@middleware(API.HandleError, API.CheckAuth)
def handle_logout(session):
    session.destroy()
    db.session.add(
        BlacklistToken(request.headers["45MinuteToken"])
    )
    db.session.commit()

    return utils.basic_response(200, msg="Shao lo vimo")

@main.route("/changepass", methods=["POST"])
@middleware(API.HandleError, API.CheckAuth)
def handle_changepass(session):
    dec_request = session.decrypt_request("username", "password")
    dec_username, dec_password = dec_request.values()

    # Esta parte es vulnerable jeje. Lo correcto sería utilizar session.user,
    # pero qué fome la vida así tan segura.
    user = User.query.filter_by(
        username=dec_username
    ).first()
    user.set_password(dec_password)

    # Destruimos la sesión
    session.destroy()
    db.session.commit()

    return utils.basic_response(200, msg=f"Se han actualizado los datos de '{dec_username}'")

