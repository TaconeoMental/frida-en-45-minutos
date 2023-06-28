from functools import wraps

from flask import request

from app.models import Session
from app.utils import basic_response

# Decorador para endpoints que requieran una sesión activa
def requires_token(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if "Authorization" not in request.headers:
            return basic_response(401)

        token = request.headers["Authorization"].split(" ")[1]
        uuid = Session.decode_token(token)
        if not uuid:
            return basic_response(401)
        return f(uuid, *args, **kwargs)
    return decorated
