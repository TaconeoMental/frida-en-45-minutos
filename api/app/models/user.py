from app import app, db, bcrypt

class User(db.Model):
    __tablename__ = "users"

    id        = db.Column(db.Integer, primary_key=True, autoincrement=True)
    username  = db.Column(db.String(50), unique=True)
    password  = db.Column(db.String(100), nullable=False)
    role      = db.Column(db.Integer, nullable=False)
    biography = db.Column(db.String(100), nullable=False)
    pass_hint = db.Column(db.String(300), nullable=False)
    sessions  = db.relationship("Session",
                               backref="user",
                               cascade="all, delete-orphan",
                               lazy=True)

    posts     = db.relationship("Post",
                               backref="user",
                               #cascade="all, delete-orphan",
                               lazy='dynamic')

    ROLE_TABLE = ["admin", "user"]

    def __init__(self, username, password, role, biography, hint):
        self.username = username
        self.role = role
        self.biography = biography
        self.pass_hint = hint
        self.set_password(password)

    def set_password(self, password):
        self.password = bcrypt.generate_password_hash(
            password,
            app.config.get('BCRYPT_LOG_ROUNDS')
        ).decode()

    @staticmethod
    def str_role(role_id):
        return User.ROLE_TABLE[role_id]

    @staticmethod
    def number_role(role_str):
        return User.ROLE_TABLE.index(role_str.lower())

    def get_role(self):
        return User.str_role(self.role)

    def set_role(self, role):
        self.role = User.number_role(role.lower())

    def is_admin(self):
        return self.get_role() == "admin"

    @staticmethod
    def from_username(username):
        user = User.query.filter_by(
            username=username
        ).first()
        return user
