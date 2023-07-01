from collections import OrderedDict
import datetime
import uuid

import jwt
from flask import request

from app import app, db
import app.crypto as crypto


class Session(db.Model):
    __tablename__ = "sessions"

    id         = db.Column(db.Integer, primary_key=True, autoincrement=True)
    shared_key = db.Column(db.String(50), nullable=False)
    uuid       = db.Column(db.String(50), nullable=False)
    user_id    = db.Column(db.Integer, db.ForeignKey('users.id'))

    def __init__(self, client_key, server_key):
        self.shared_key = crypto.build_shared_key(client_key, server_key)
        self.uuid = str(uuid.uuid4())

    def get_cipher(self):
        return crypto.Cipher(bytes.fromhex(self.shared_key))

    def decrypt_request(self, *keys):
        content = request.get_json()
        cipher = self.get_cipher()

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
