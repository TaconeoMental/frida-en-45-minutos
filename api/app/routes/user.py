from flask import Flask, request
from flask import Blueprint

from app.models import Session, User, BlacklistToken, Ticket
from app.middleware import middleware, API
from app.helpers import request_values


user_blueprint = Blueprint('user', __name__)

@user_blueprint.route("/getuser", methods=["POST"])
@middleware(API.HandleError, API.CheckAuth)
def handle_getuser(session):
    dec_username = session.decrypt_request("username")["username"]
    user = User.from_username(dec_username)

    tickets = Ticket.query.filter_by(user=user) \
                          .limit(3) \
                          .all()
    t_dict = {f"ticket{n+1}": t.contents for n, t in enumerate(tickets)}
    dec_response = {
        "username": user.username,
        "role": user.role,
        "tickets": t_dict
    }

    cipher = session.get_cipher()
    return cipher.build_response(200, dec_response)
