from app import db

class Ticket(db.Model):
    __tablename__ = "tickets"

    id         = db.Column(db.Integer, primary_key=True, autoincrement=True)
    contents   = db.Column(db.String(100), nullable=False)
    user_id    = db.Column(db.Integer, db.ForeignKey('users.id'))

    def __init__(self, text):
        self.contents = text
