from functools import wraps

from flask import request

from app.database.models import Session
from app.utils import basic_response

# Decorador para endpoints que requieran una sesión activa [y asociada a un usuario]
def requires_token(authenticated=False):
    def requires_token_decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            if "45MinuteToken" not in request.headers:
                return basic_response(401)

            token = request.headers["45MinuteToken"]
            uuid = Session.decode_token(token)
            if not uuid:
                return basic_response(401)

            session = Session.query.filter_by(uuid=uuid).first()
            if authenticated and not session.user: # aka. el token no está asociado a un usuario
                return basic_response(401)

            return func(uuid, *args, **kwargs)
        return wrapper
    return requires_token_decorator

# Decorador que maneja cualquier error con un 500 en nuestro formato :)
# TODO: loggear todo esto. Es el lugar perfecto para meter esa lógica.
def handles_server_error(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        try:
            return f(*args, **kwargs)
        except Exception as e:
            return basic_response(500, msg="Se ha producido un error desconocido")
    return decorated
