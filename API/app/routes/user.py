from flask import Flask, request
from flask import Blueprint
from sqlalchemy.sql.expression import func, or_

from app import db
from app.models import Session, User, BlacklistToken, Post
from app.middleware import middleware, API
from app.helpers import request_values


user_blueprint = Blueprint('user', __name__)

@user_blueprint.route("/getuser", methods=["POST"])
@middleware(API.HandleError, API.CheckAuth)
def handle_getuser(session):
    dec_username = session.decrypt_request("username")["username"]
    user = User.from_username(dec_username)

    dec_response = {
        "username": user.username,
        "role": user.get_role(),
        "bio": user.biography,
        "password_hint": user.pass_hint
    }
    return session.build_response(200, dec_response)

@user_blueprint.route("/getfeed", methods=["POST"])
@middleware(API.HandleError, API.CheckAuth)
def handle_getfeed(session):
    dec_username = session.decrypt_request("username")["username"]
    user = User.from_username(dec_username)

    s_user = session.user

    filters = []
    filters.append(Post.public == True)

    # Admins tienen acceso a posts de otros admins
    if user.get_role() == "admin":
        filters.append(Post.public == False)
    posts = Post.query.filter(or_(*filters)).order_by(Post.id.desc()).all()

    dec_response = {"username": user.username, "posts": list()}
    for post in posts:
        dec_response["posts"].append({
            "author": post.user.username,
            "contents": post.contents
        })

    return session.build_response(200, dec_response)

@user_blueprint.route("/sendpost", methods=["POST"])
@middleware(API.HandleError, API.CheckAuth)
def handle_sendpost(session):
    dec_request = session.decrypt_request("username", "contents")
    dec_username, dec_contents = dec_request.values()
    user = User.from_username(dec_username)

    # público por defecto (por ahora)
    Post(user.username, dec_contents, 1)
    db.session.commit()

    return session.build_response(200)
