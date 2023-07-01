from flask import request

class EndpointException(Exception):
    def __init__(self, code, msg=None):
        self.code = code
        self.msg = msg
        super().__init__()

# Flask tiene una forma nativa para hacer esto pero es demasiado overkill para
# este caso particular
def request_values(*keys):
    content = request.get_json()
    values = [content.get(k) for k in keys]
    if not all(values):
        raise EndpointException(400)
    if len(values) == 1:
        return values[0]
    return (*values,)
