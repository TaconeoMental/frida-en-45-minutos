from . import db
from app.models import Session

def get_shared_key(uuid):
    res = db.execute("SELECT symkey FROM sessions WHERE uuid=?", (uuid,)).fetchone()
    if not res:
        # No existe el UUID
        return None
    return bytes.fromhex(res[0])

def get_user(username, role):
    user_attrs = ["username", "age", "lastlogin", "bio"]
    if role != "ADMIN":
        values = [username] + ["***"] * 3
    else:
        res = db.execute("SELECT username, age, lastlogin, bio FROM users WHERE username=?", (username,))
        if not (values := res.fetchone()): # No existe
            return None
    return dict(zip(user_attrs, [str(e) for e in values]))
