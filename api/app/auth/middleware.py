from functools import wraps

from flask import request

from app.database.models import Session
from app.utils import basic_response
from .helpers import EndpointException

def middleware(*checks):
    def decorator(func):
        for check in reversed(checks):
            func = check(func)
        return func
    return decorator

class API:
    @staticmethod
    def get_session():
        if not (token := request.headers["45MinuteToken"]):
            return None

        if not (uuid := Session.decode_token(token)):
            return None

        # En teoría no podemos llegar a esta parte con un session None. Es
        # imposible recibir un UUID inexistente y mientras no eliminemos
        # entradas en la DB todo debería estar bien. De todas formas,
        # cualquier cosa lo maneja handles_server_error.
        if not (session := Session.query.filter_by(uuid=uuid).first()):
            return None
        return session

    @staticmethod
    def HandleError(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            try:
                return func(*args, **kwargs)
            except EndpointException as e:
                return basic_response(e.code, e.msg)
            except Exception as e:
                return basic_response(500, msg="Se ha producido un error desconocido")
        return wrapper

    @staticmethod
    def CheckSession(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            if not (session := API.get_session()):
                return basic_response(401)
            return func(session, *args, **kwargs)
        return wrapper

    @staticmethod
    def CheckAuth(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            if not (session := API.get_session()):
                return basic_response(401)

            if not session.user:
                return basic_response(401)

            return func(session, *args, **kwargs)
        return wrapper

