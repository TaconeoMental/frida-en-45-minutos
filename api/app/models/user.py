from app import app, db, bcrypt

class User(db.Model):
    __tablename__ = "users"

    id       = db.Column(db.Integer, primary_key=True, autoincrement=True)
    username = db.Column(db.String(100), unique=True)
    password = db.Column(db.String(100), nullable=False)
    role     = db.Column(db.String(50), nullable=False)
    sessions = db.relationship("Session",
                               backref="user",
                               cascade="all, delete-orphan",
                               lazy=True)

    tickets  = db.relationship("Ticket",
                               backref="user",
                               #cascade="all, delete-orphan",
                               lazy='dynamic')

    def __init__(self, username, password, role, tickets=tuple()):
        self.username = username
        self.role = role
        self.set_password(password)
        self.tickets = tickets

    def set_password(self, password):
        self.password = bcrypt.generate_password_hash(
            password,
            app.config.get('BCRYPT_LOG_ROUNDS')
        ).decode()

    @staticmethod
    def from_username(username):
        user = User.query.filter_by(
            username=username
        ).first()
        return user
