import string

# Se explica solo
def is_hex(s):
    return set(s).issubset(string.hexdigits)

# Código de estado => OK/Error
# Es medio fea la solución, pero es lo que hacen algunas librerías.
def status_code_text(status):
    if 200 <= status < 400:
        return "OK"
    return "NOK"

def basic_response(status_code, msg=None, values=None):
    if not values:
        values = {}
    message = {"msg": msg} if msg else {}
    base_response = {"status": status_code_text(status_code)}
    return base_response | message | values, status_code
