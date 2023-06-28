import uuid
import datetime

import jwt

from . import app, db, bcrypt

class User(db.Model):
    __tablename__ = "users"

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(100), unique=True)
    password = db.Column(db.String(100), nullable=False)
    role = db.Column(db.String(50), nullable=False)

    def __init__(self, email, password, role):
        self.email = email
        self.role = role
        self.password = bcrypt.generate_password_hash(
            password,
            app.config.get('BCRYPT_LOG_ROUNDS')
        ).decode()


class Session(db.Model):
    __tablename__ = "sessions"

    id = db.Column(db.Integer, primary_key=True)
    shared_key = db.Column(db.String(50), nullable=False)
    shared_key = db.Column(db.String(50), nullable=False)

    def __init__(self, shared_key):
        self.shared_key = shared_key
        self.uuid = str(uuid.uuid4())

    def gen_token(self):
        payload = {
            'exp': datetime.datetime.utcnow() + datetime.timedelta(days=0,
                                                                   minutes=30),
            'iat': datetime.datetime.utcnow(),
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
