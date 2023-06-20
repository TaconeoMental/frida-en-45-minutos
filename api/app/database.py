import sqlite3

SQLITE_DB_NAME = "frida.db"

class Database:
    connection = None
    cursor = None

    def __init__(self):
        if Database.connection is None:
            Database.connection = sqlite3.connect(SQLITE_DB_NAME,
                                                  check_same_thread=False)
            Database.cursor = Database.connection.cursor()
        self.connection = Database.connection
        self.cursor = Database.cursor

    def execute(self, *args, **kwargs):
        print(*args)
        res = self.cursor.execute(*args, **kwargs)
        self.connection.commit()
        return res


def init_db():
    db = Database()
    db.execute('''CREATE TABLE users(
                         ID        INTEGER PRIMARY KEY AUTOINCREMENT,
                         username  TEXT NOT NULL,
                         age       INT  NOT NULL,
                         lastlogin INT  NOT NULL,
                         bio       TEXT
                 );''')

    # Usuario prueba
    #db.execute("INSERT into users (username, age, lastlogin, bio) VALUES('mateo', 22, 23, 'sdgfsgsgff')")

    db.execute('''CREATE TABLE sessions(
                         uuid   TEXT PRIMARY KEY,
                         symkey TEXT NOT NULL
                 );''')

def get_shared_key(uuid):
    db = Database()
    res = db.execute("SELECT symkey FROM sessions WHERE uuid=?", (uuid,)).fetchone()
    if not res:
        # No existe el UUID
        return None
    return bytes.fromhex(res[0])

def save_session(uuid, symkey):
    db = Database()
    db.execute("INSERT into sessions (uuid, symkey) VALUES(?, ?)", (uuid, symkey))

def get_user(username, role):
    db = Database()
    res = db.execute("SELECT username, age, lastlogin, bio FROM users WHERE username=?", (username,))
    if not (res := res.fetchone()):
        # No existe
        return None
    user_attrs = ["username", "age", "lastlogin", "bio"]
    return dict(zip(user_attrs, [str(e) for e in res]))
