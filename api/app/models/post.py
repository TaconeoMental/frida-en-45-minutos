from app import db
from .user import User

class Post(db.Model):
    __tablename__ = "posts"

    id         = db.Column(db.Integer, primary_key=True, autoincrement=True)
    contents   = db.Column(db.String(100), nullable=False)
    public     = db.Column(db.Boolean, nullable=False, unique=False, default=True)
    user_id    = db.Column(db.Integer, db.ForeignKey('users.id'))

    def __init__(self, user, text, is_public):
        self.contents = text
        self.public = is_public
        User.from_username(user).posts.append(self)
