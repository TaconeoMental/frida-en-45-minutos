from app.models import User, Ticket

def load_data(db):
    t1 = Ticket("TICKET 1 OWO")
    t2 = Ticket("TICKET 2 AWA")
    t3 = Ticket("TICKET 3 EWE")
    t4 = Ticket("TICKET 4 UWU")
    user = User("mateo", "frida2023", "admin", tickets=[t1,t2,t3,t4])
    db.session.add(user)
    db.session.commit()
