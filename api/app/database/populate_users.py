from .models import User

def load_data(db):
    db.session.add(User("mateo", "frida2023", "admin"))
    db.session.commit()
