from collections import OrderedDict
import datetime
import uuid

import jwt
from flask import request

import app.crypto as crypto
from app import app, db, bcrypt

class User(db.Model):
    __tablename__ = "users"

    id       = db.Column(db.Integer, primary_key=True, autoincrement=True)
    username = db.Column(db.String(100), unique=True)
    password = db.Column(db.String(100), nullable=False)
    role     = db.Column(db.String(50), nullable=False)

    sessions = db.relationship("Session",
                               backref="user",
                               cascade="all, delete-orphan",
                               lazy=True)

    def __init__(self, username, password, role):
        self.username = username
        self.role = role
        self.set_password(password)

    def set_password(self, password):
        self.password = bcrypt.generate_password_hash(
            password,
            app.config.get('BCRYPT_LOG_ROUNDS')
        ).decode()


class Session(db.Model):
    __tablename__ = "sessions"

    id         = db.Column(db.Integer, primary_key=True, autoincrement=True)
    shared_key = db.Column(db.String(50), nullable=False)
    uuid       = db.Column(db.String(50), nullable=False)

    user_id    = db.Column(db.Integer, db.ForeignKey('users.id'))

    def __init__(self, client_key, server_key):
        self.shared_key = crypto.build_shared_key(client_key, server_key)
        self.uuid = str(uuid.uuid4())

    def decrypt_request(self, *keys):
        content = request.get_json()
        cipher = crypto.Cipher(bytes.fromhex(self.shared_key))

        dec_values = OrderedDict()
        for k in keys:
            if k not in content:
                raise EndpointException(400)
            dec_values[k] = cipher.decrypt(content[k])

        return dec_values

    def destroy(self):
        self.user = None

    def create_token(self):
        now = datetime.datetime.utcnow()
        payload = {
            'exp': now + datetime.timedelta(days=0, minutes=30),
            'iat': now,
            'sub': self.uuid

        }
        return jwt.encode(
            payload,
            app.config.get('SECRET_KEY'),
            algorithm='HS256'
        )

    @staticmethod
    def decode_token(token):
        try:
            payload = jwt.decode(
                token,
                app.config.get('SECRET_KEY'),
                algorithms=["HS256"]
            )
            return payload["sub"]
        except (jwt.ExpiredSignatureError, jwt.InvalidTokenError):
            return None


class BlacklistToken(db.Model):
    __tablename__ = 'blacklisted_tokens'

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    token = db.Column(db.String(500), unique=True, nullable=False)

    def __init__(self, token):
        self.token = token

    @staticmethod
    def is_blacklisted(token):
        res = BlacklistToken.query.filter_by(token=token).first()
        if res:
            return True
        return False
